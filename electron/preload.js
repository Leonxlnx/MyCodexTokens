const { contextBridge, ipcRenderer } = require("electron");

contextBridge.exposeInMainWorld("slopmeter", {
  getConfig: () => ipcRenderer.invoke("slopmeter:get-config"),
  getSettings: () => ipcRenderer.invoke("slopmeter:get-settings"),
  saveSettings: (settings) => ipcRenderer.invoke("slopmeter:save-settings", settings),
  getLatestSnapshot: () => ipcRenderer.invoke("slopmeter:get-latest-snapshot"),
  refresh: () => ipcRenderer.invoke("slopmeter:refresh"),
  openImage: () => ipcRenderer.invoke("slopmeter:open-image"),
  showDownloads: () => ipcRenderer.invoke("slopmeter:show-downloads"),
  onSnapshotUpdated: (callback) => {
    const listener = (_event, payload) => callback(payload);
    ipcRenderer.on("slopmeter:snapshot-updated", listener);
    return () => ipcRenderer.removeListener("slopmeter:snapshot-updated", listener);
  },
  onSettingsUpdated: (callback) => {
    const listener = (_event, payload) => callback(payload);
    ipcRenderer.on("slopmeter:settings-updated", listener);
    return () => ipcRenderer.removeListener("slopmeter:settings-updated", listener);
  },
});
