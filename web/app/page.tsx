"use client";

import { useEffect, useRef, useState } from "react";
import { PanoramaViewer } from "@/components/panorama-viewer";

type PanoramaWorld = {
  id: string;
  name: string;
  imageSrc: string;
};

const baseWorlds: PanoramaWorld[] = [
  {
    id: "world-01",
    name: "01",
    imageSrc: "/panorama/skybound-world.png"
  },
  {
    id: "world-02",
    name: "02",
    imageSrc: "/panorama/world-02.png"
  },
  {
    id: "world-03",
    name: "03",
    imageSrc: "/panorama/world-03.png"
  },
  {
    id: "world-04",
    name: "04",
    imageSrc: "/panorama/world-04.png"
  },
  {
    id: "world-05",
    name: "05",
    imageSrc: "/panorama/world-05.png"
  }
];

export default function Page() {
  const [worlds, setWorlds] = useState<PanoramaWorld[]>(baseWorlds);
  const uploadUrlsRef = useRef<Set<string>>(new Set());

  const handleUpload = (files: File[]) => {
    const valid = files.filter((file) => file.type.startsWith("image/"));
    if (!valid.length) {
      return;
    }

    const next = valid.map((file, index) => {
      const imageSrc = URL.createObjectURL(file);
      uploadUrlsRef.current.add(imageSrc);

      return {
        id: `upload-${Date.now()}-${index}-${file.name}`,
        name: file.name,
        imageSrc
      };
    });

    setWorlds((current) => [...current, ...next]);
  };

  useEffect(() => {
    return () => {
      uploadUrlsRef.current.forEach((url) => {
        URL.revokeObjectURL(url);
      });
      uploadUrlsRef.current.clear();
    };
  }, []);

  return <PanoramaViewer worlds={worlds} onUpload={handleUpload} />;
}
