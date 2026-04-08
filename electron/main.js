const { app, BrowserWindow, ipcMain, shell } = require("electron");
const { spawn } = require("node:child_process");
const fs = require("node:fs/promises");
const path = require("node:path");
const { pathToFileURL } = require("node:url");

const MAIN_WINDOW_BOUNDS = {
  width: 1340,
  height: 960,
  minWidth: 1120,
  minHeight: 780,
};

const DEFAULT_SETTINGS = {
  theme: "dark",
  autoRefreshMinutes: 30,
};

let mainWindow = null;

function getAssetPath(...segments) {
  return path.join(__dirname, "..", "assets", ...segments);
}

function getOutputPaths() {
  const downloadsDir = app.getPath("downloads");

  return {
    downloadsDir,
    imagePath: path.join(downloadsDir, "mycodextokens-codex-heatmap.png"),
    jsonPath: path.join(downloadsDir, "mycodextokens-codex-usage.json"),
  };
}

function getCliScriptPath() {
  const packageRoot = app.isPackaged
    ? path.join(process.resourcesPath, "app.asar.unpacked", "node_modules", "slopmeter")
    : path.join(__dirname, "..", "node_modules", "slopmeter");

  return path.join(packageRoot, "dist", "cli.js");
}

async function fileExists(targetPath) {
  try {
    await fs.access(targetPath);
    return true;
  } catch {
    return false;
  }
}

function getSettingsPath() {
  return path.join(app.getPath("userData"), "settings.json");
}

async function readSettings() {
  try {
    const raw = await fs.readFile(getSettingsPath(), "utf8");
    return {
      ...DEFAULT_SETTINGS,
      ...JSON.parse(raw),
    };
  } catch {
    return { ...DEFAULT_SETTINGS };
  }
}

async function writeSettings(partialSettings) {
  const nextSettings = {
    ...(await readSettings()),
    ...partialSettings,
  };

  await fs.writeFile(getSettingsPath(), JSON.stringify(nextSettings, null, 2), "utf8");
  return nextSettings;
}

function shiftIsoDate(dateString, offsetDays) {
  const [year, month, day] = dateString.split("-").map(Number);
  const shifted = new Date(Date.UTC(year, month - 1, day + offsetDays));
  return shifted.toISOString().slice(0, 10);
}

function createEmptyDay(date) {
  return {
    date,
    input: 0,
    output: 0,
    cache: {
      input: 0,
      output: 0,
    },
    total: 0,
  };
}

function toSnapshot(payload) {
  const provider = payload.providers?.find((entry) => entry.provider === "codex") ?? payload.providers?.[0];
  const daily = provider?.daily ?? [];
  const todayDate = payload.end;
  const yesterdayDate = shiftIsoDate(todayDate, -1);
  const today = daily.find((entry) => entry.date === todayDate) ?? createEmptyDay(todayDate);
  const yesterday = daily.find((entry) => entry.date === yesterdayDate) ?? createEmptyDay(yesterdayDate);
  const insights = provider?.insights ?? {};
  const { downloadsDir, imagePath, jsonPath } = getOutputPaths();

  return {
    provider: provider?.provider ?? "codex",
    range: {
      start: payload.start,
      end: payload.end,
    },
    downloadsDir,
    imagePath,
    imageUrl: `${pathToFileURL(imagePath).href}?v=${Date.now()}`,
    jsonPath,
    generatedAt: new Date().toISOString(),
    today,
    yesterday,
    insights: {
      mostUsedModel: insights.mostUsedModel?.name ?? "Unknown",
      recentMostUsedModel: insights.recentMostUsedModel?.name ?? "Unknown",
      currentStreak: insights.streaks?.current ?? 0,
      longestStreak: insights.streaks?.longest ?? 0,
    },
  };
}

async function readExistingSnapshot() {
  const { jsonPath } = getOutputPaths();

  if (!(await fileExists(jsonPath))) {
    return null;
  }

  const raw = await fs.readFile(jsonPath, "utf8");
  const payload = JSON.parse(raw);

  return toSnapshot(payload);
}

function runBundledSlopmeter(args) {
  return new Promise((resolve, reject) => {
    const child = spawn(process.execPath, [getCliScriptPath(), ...args], {
      cwd: app.getPath("home"),
      env: {
        ...process.env,
        ELECTRON_RUN_AS_NODE: "1",
      },
      stdio: ["ignore", "pipe", "pipe"],
      windowsHide: true,
    });

    let stdout = "";
    let stderr = "";

    child.stdout.on("data", (chunk) => {
      stdout += chunk.toString();
    });

    child.stderr.on("data", (chunk) => {
      stderr += chunk.toString();
    });

    child.on("error", (error) => {
      reject(error);
    });

    child.on("close", (code) => {
      if (code === 0) {
        resolve({ stdout, stderr });
        return;
      }

      reject(new Error(stderr.trim() || stdout.trim() || `slopmeter exited with code ${code}.`));
    });
  });
}

async function refreshSnapshot() {
  const { imagePath, jsonPath } = getOutputPaths();

  await runBundledSlopmeter(["--codex", "--dark", "--format", "json", "--output", jsonPath]);
  await runBundledSlopmeter(["--codex", "--dark", "--output", imagePath]);

  const raw = await fs.readFile(jsonPath, "utf8");
  const payload = JSON.parse(raw);

  return toSnapshot(payload);
}

function broadcast(channel, payload) {
  BrowserWindow.getAllWindows().forEach((window) => {
    if (!window.isDestroyed()) {
      window.webContents.send(channel, payload);
    }
  });
}

function createMainWindow() {
  mainWindow = new BrowserWindow({
    ...MAIN_WINDOW_BOUNDS,
    backgroundColor: "#101214",
    autoHideMenuBar: true,
    title: "MyCodexTokens",
    icon: getAssetPath("icon.png"),
    titleBarStyle: process.platform === "darwin" ? "hiddenInset" : "default",
    webPreferences: {
      preload: path.join(__dirname, "preload.js"),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: false,
    },
  });

  mainWindow.loadFile(path.join(__dirname, "..", "index.html"));
  mainWindow.on("closed", () => {
    mainWindow = null;
  });
}

app.whenReady().then(() => {
  ipcMain.handle("slopmeter:get-config", async () => {
    const paths = getOutputPaths();

    return {
      provider: "codex",
      ...paths,
      imageExists: await fileExists(paths.imagePath),
    };
  });

  ipcMain.handle("slopmeter:get-settings", async () => readSettings());

  ipcMain.handle("slopmeter:save-settings", async (_event, partialSettings) => {
    const settings = await writeSettings(partialSettings);
    broadcast("slopmeter:settings-updated", settings);
    return settings;
  });

  ipcMain.handle("slopmeter:get-latest-snapshot", async () => readExistingSnapshot());

  ipcMain.handle("slopmeter:refresh", async () => {
    const snapshot = await refreshSnapshot();
    broadcast("slopmeter:snapshot-updated", snapshot);
    return snapshot;
  });

  ipcMain.handle("slopmeter:open-image", async () => {
    const { imagePath } = getOutputPaths();
    return shell.openPath(imagePath);
  });

  ipcMain.handle("slopmeter:show-downloads", async () => {
    const { downloadsDir, imagePath } = getOutputPaths();

    if (await fileExists(imagePath)) {
      shell.showItemInFolder(imagePath);
      return "";
    }

    return shell.openPath(downloadsDir);
  });

  createMainWindow();

  app.on("activate", () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createMainWindow();
    }
  });
});

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") {
    app.quit();
  }
});
