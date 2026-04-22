"use client";

import { ChangeEvent, startTransition, useEffect, useRef, useState } from "react";
import * as THREE from "three";

type PanoramaWorld = {
  id: string;
  name: string;
  imageSrc: string;
};

type PanoramaViewerProps = {
  worlds: PanoramaWorld[];
  onUpload: (files: File[]) => void;
};

export function PanoramaViewer({ worlds, onUpload }: PanoramaViewerProps) {
  const mountRef = useRef<HTMLDivElement | null>(null);
  const uploadInputRef = useRef<HTMLInputElement | null>(null);
  const [selectedWorldId, setSelectedWorldId] = useState(worlds[0]?.id ?? "");
  const [loadState, setLoadState] = useState<"loading" | "ready" | "error">("loading");
  const [reloadToken, setReloadToken] = useState(0);
  const activeWorld = worlds.find((world) => world.id === selectedWorldId) ?? worlds[0];

  useEffect(() => {
    if (!activeWorld && worlds[0]) {
      setSelectedWorldId(worlds[0].id);
    }
  }, [activeWorld, worlds]);

  const handleUploadClick = () => {
    uploadInputRef.current?.click();
  };

  const handleUploadChange = (event: ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(event.target.files ?? []);
    if (!files.length) {
      return;
    }

    onUpload(files);
    event.target.value = "";
  };

  useEffect(() => {
    const mount = mountRef.current;
    if (!mount || !activeWorld) {
      return;
    }

    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(72, 1, 0.1, 1100);
    const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
    const textureLoader = new THREE.TextureLoader();
    const sphereGeometry = new THREE.SphereGeometry(500, 72, 48);
    sphereGeometry.scale(-1, 1, 1);
    let panoramaTexture: THREE.Texture | null = null;
    let panoramaMaterial: THREE.MeshBasicMaterial | null = null;
    let panoramaMesh: THREE.Mesh | null = null;

    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    renderer.setSize(mount.clientWidth, mount.clientHeight);
    renderer.outputColorSpace = THREE.SRGBColorSpace;
    mount.appendChild(renderer.domElement);

    let lon = -90;
    let lat = 4;
    let animationFrame = 0;
    let disposed = false;
    let isPointerDown = false;
    let pointerX = 0;
    let pointerY = 0;
    let startLon = 0;
    let startLat = 0;
    let keyLatVelocity = 0;
    let keyLonVelocity = 0;

    const updateCamera = () => {
      lat = THREE.MathUtils.clamp(lat, -85, 85);

      const phi = THREE.MathUtils.degToRad(90 - lat);
      const theta = THREE.MathUtils.degToRad(lon);

      camera.lookAt(
        500 * Math.sin(phi) * Math.cos(theta),
        500 * Math.cos(phi),
        500 * Math.sin(phi) * Math.sin(theta)
      );
    };

    const renderFrame = () => {
      if (!isPointerDown) {
        lon += keyLonVelocity;
        lat += keyLatVelocity;
        keyLonVelocity *= 0.88;
        keyLatVelocity *= 0.88;
      }

      updateCamera();
      renderer.render(scene, camera);
      animationFrame = window.requestAnimationFrame(renderFrame);
    };

    const handleResize = () => {
      if (disposed) {
        return;
      }

      const { clientWidth, clientHeight } = mount;
      camera.aspect = clientWidth / clientHeight;
      camera.updateProjectionMatrix();
      renderer.setSize(clientWidth, clientHeight);
    };

    const resetView = () => {
      lon = -90;
      lat = 4;
      camera.fov = 72;
      camera.updateProjectionMatrix();
      updateCamera();
    };

    const handlePointerDown = (event: PointerEvent) => {
      isPointerDown = true;
      pointerX = event.clientX;
      pointerY = event.clientY;
      startLon = lon;
      startLat = lat;
      mount.setPointerCapture(event.pointerId);
    };

    const handlePointerMove = (event: PointerEvent) => {
      if (!isPointerDown) {
        return;
      }

      const deltaX = event.clientX - pointerX;
      const deltaY = event.clientY - pointerY;

      lon = startLon - deltaX * 0.13;
      lat = startLat + deltaY * 0.13;
    };

    const handlePointerUp = (event: PointerEvent) => {
      isPointerDown = false;

      if (mount.hasPointerCapture(event.pointerId)) {
        mount.releasePointerCapture(event.pointerId);
      }
    };

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "ArrowLeft") {
        keyLonVelocity = -1.2;
      } else if (event.key === "ArrowRight") {
        keyLonVelocity = 1.2;
      } else if (event.key === "ArrowUp") {
        keyLatVelocity = -0.9;
      } else if (event.key === "ArrowDown") {
        keyLatVelocity = 0.9;
      } else if (event.key === "+" || event.key === "=") {
        camera.fov = THREE.MathUtils.clamp(camera.fov - 2, 42, 88);
        camera.updateProjectionMatrix();
      } else if (event.key === "-" || event.key === "_") {
        camera.fov = THREE.MathUtils.clamp(camera.fov + 2, 42, 88);
        camera.updateProjectionMatrix();
      } else if (event.key.toLowerCase() === "r") {
        resetView();
      }
    };

    const handleWheel = (event: WheelEvent) => {
      event.preventDefault();
      camera.fov = THREE.MathUtils.clamp(camera.fov + event.deltaY * 0.03, 42, 88);
      camera.updateProjectionMatrix();
    };

    startTransition(() => {
      setLoadState("loading");
    });

    textureLoader.load(
      activeWorld.imageSrc,
      (texture) => {
        if (disposed) {
          texture.dispose();
          return;
        }

        texture.colorSpace = THREE.SRGBColorSpace;
        panoramaTexture = texture;
        panoramaMaterial = new THREE.MeshBasicMaterial({ map: texture });
        panoramaMesh = new THREE.Mesh(sphereGeometry, panoramaMaterial);
        scene.add(panoramaMesh);
        startTransition(() => {
          setLoadState("ready");
        });
        renderFrame();
      },
      () => undefined,
      () => {
        if (disposed) {
          return;
        }

        startTransition(() => {
          setLoadState("error");
        });
      }
    );

    camera.position.set(0, 0, 0.1);
    updateCamera();
    handleResize();

    mount.style.touchAction = "none";
    mount.addEventListener("pointerdown", handlePointerDown);
    mount.addEventListener("pointermove", handlePointerMove);
    mount.addEventListener("pointerup", handlePointerUp);
    mount.addEventListener("pointercancel", handlePointerUp);
    mount.addEventListener("wheel", handleWheel, { passive: false });
    window.addEventListener("resize", handleResize);
    window.addEventListener("keydown", handleKeyDown);

    return () => {
      disposed = true;
      window.cancelAnimationFrame(animationFrame);
      window.removeEventListener("resize", handleResize);
      window.removeEventListener("keydown", handleKeyDown);
      mount.removeEventListener("pointerdown", handlePointerDown);
      mount.removeEventListener("pointermove", handlePointerMove);
      mount.removeEventListener("pointerup", handlePointerUp);
      mount.removeEventListener("pointercancel", handlePointerUp);
      mount.removeEventListener("wheel", handleWheel);
      panoramaTexture?.dispose();
      panoramaMaterial?.dispose();
      sphereGeometry.dispose();
      renderer.dispose();
      if (mount.contains(renderer.domElement)) {
        mount.removeChild(renderer.domElement);
      }
    };
  }, [activeWorld, reloadToken]);

  return (
    <main className="panorama-shell">
      <div className="panorama-stage" ref={mountRef} aria-label="360 world viewer" />
      <div className="panorama-vignette" aria-hidden="true" />

      <aside className="world-switcher" aria-label="Worlds">
        <div className="upload-toolbar">
          <input
            ref={uploadInputRef}
            className="upload-input sr-only"
            type="file"
            accept="image/*"
            multiple
            onChange={handleUploadChange}
          />
          <button
            className="upload-button"
            type="button"
            onClick={handleUploadClick}
          >
            Upload Images
          </button>
        </div>
        <div className="world-switcher-list">
          {worlds.map((world) => (
            <button
              key={world.id}
              className={`world-switcher-item${activeWorld?.id === world.id ? " is-active" : ""}`}
              type="button"
              aria-label={`World ${world.name}`}
              onClick={() => setSelectedWorldId(world.id)}
            >
              <span className="world-switcher-thumb" style={{ backgroundImage: `url(${world.imageSrc})` }} />
            </button>
          ))}
        </div>
      </aside>
      <section className="panorama-copy" />

      {loadState !== "ready" ? (
        <div className="panorama-status" role="status" aria-live="polite">
          <div className="panorama-status-panel">
            <div className="panorama-spinner" aria-hidden="true" />
            {loadState === "error" ? (
              <>
                <p className="panorama-status-copy sr-only">reload</p>
                <button
                  className="panorama-button"
                  type="button"
                  aria-label="Retry world"
                  onClick={() => setReloadToken((value) => value + 1)}
                />
              </>
            ) : (
              <></>
            )}
          </div>
        </div>
      ) : null}
    </main>
  );
}
