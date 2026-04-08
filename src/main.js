const state = {
  isRefreshing: false,
  snapshot: null,
  autoRefreshTimer: null,
  config: null,
  settings: null,
};

const elements = {
  refreshButton: document.querySelector("#refreshButton"),
  themeToggleButton: document.querySelector("#themeToggleButton"),
  autoRefreshSelect: document.querySelector("#autoRefreshSelect"),
  openImageButton: document.querySelector("#openImageButton"),
  showDownloadsButton: document.querySelector("#showDownloadsButton"),
  statusBadge: document.querySelector("#statusBadge"),
  providerValue: document.querySelector("#providerValue"),
  rangeValue: document.querySelector("#rangeValue"),
  pathValue: document.querySelector("#pathValue"),
  modelValue: document.querySelector("#modelValue"),
  streakValue: document.querySelector("#streakValue"),
  outputPathLabel: document.querySelector("#outputPathLabel"),
  updatedAtValue: document.querySelector("#updatedAtValue"),
  todayTotal: document.querySelector("#todayTotal"),
  todayDate: document.querySelector("#todayDate"),
  todayInput: document.querySelector("#todayInput"),
  todayOutput: document.querySelector("#todayOutput"),
  todayCache: document.querySelector("#todayCache"),
  yesterdayTotal: document.querySelector("#yesterdayTotal"),
  yesterdayDate: document.querySelector("#yesterdayDate"),
  yesterdayInput: document.querySelector("#yesterdayInput"),
  yesterdayOutput: document.querySelector("#yesterdayOutput"),
  yesterdayCache: document.querySelector("#yesterdayCache"),
  previewFrame: document.querySelector("#previewFrame"),
  heatmapImage: document.querySelector("#heatmapImage"),
  emptyState: document.querySelector("#emptyState"),
};

const numberFormatter = new Intl.NumberFormat(undefined);
const dateFormatter = new Intl.DateTimeFormat(undefined, {
  year: "numeric",
  month: "short",
  day: "numeric",
});
const timeFormatter = new Intl.DateTimeFormat(undefined, {
  year: "numeric",
  month: "short",
  day: "numeric",
  hour: "numeric",
  minute: "2-digit",
});

function formatTokens(value) {
  return numberFormatter.format(value ?? 0);
}

function formatDate(value) {
  if (!value) {
    return "Waiting";
  }

  const [year, month, day] = value.split("-").map(Number);
  return dateFormatter.format(new Date(year, month - 1, day));
}

function formatTimestamp(value) {
  if (!value) {
    return "Waiting";
  }

  return timeFormatter.format(new Date(value));
}

function setStatus(tone) {
  const labels = {
    idle: "Ready",
    loading: "Updating",
    success: "Fresh",
    error: "Error",
  };

  elements.statusBadge.dataset.state = tone;
  elements.statusBadge.textContent = labels[tone] ?? labels.idle;
}

function setBusy(isBusy) {
  state.isRefreshing = isBusy;
  elements.refreshButton.disabled = isBusy;
  elements.refreshButton.textContent = isBusy ? "Refreshing..." : "Refresh";
}

function applyTheme(theme) {
  document.body.dataset.theme = theme;
  elements.themeToggleButton.textContent = theme === "dark" ? "Light" : "Dark";
}

function renderDay(prefix, day) {
  elements[`${prefix}Total`].textContent = formatTokens(day.total);
  elements[`${prefix}Date`].textContent = formatDate(day.date);
  elements[`${prefix}Input`].textContent = formatTokens(day.input);
  elements[`${prefix}Output`].textContent = formatTokens(day.output);
  elements[`${prefix}Cache`].textContent = formatTokens(day.cache?.input ?? 0);
}

function renderSnapshot(snapshot) {
  state.snapshot = snapshot;
  elements.providerValue.textContent = snapshot.provider.toUpperCase();
  elements.rangeValue.textContent = `${formatDate(snapshot.range.start)} to ${formatDate(snapshot.range.end)}`;
  elements.pathValue.textContent = snapshot.imagePath;
  elements.modelValue.textContent = snapshot.insights.mostUsedModel;
  elements.streakValue.textContent = `${snapshot.insights.currentStreak} days`;
  elements.outputPathLabel.textContent = snapshot.imagePath;
  elements.updatedAtValue.textContent = formatTimestamp(snapshot.generatedAt);

  renderDay("today", snapshot.today);
  renderDay("yesterday", snapshot.yesterday);

  elements.heatmapImage.src = snapshot.imageUrl;
  elements.previewFrame.classList.remove("is-empty");
  elements.emptyState.hidden = true;
  elements.openImageButton.disabled = false;
}

function applyConfig(config) {
  state.config = config;
  elements.providerValue.textContent = config.provider.toUpperCase();
  elements.outputPathLabel.textContent = config.imagePath;
  elements.pathValue.textContent = config.imagePath;
  elements.openImageButton.disabled = !config.imageExists;
}

function updateActionAvailability() {
  const hasSnapshot = Boolean(state.snapshot || state.config?.imageExists);
  elements.openImageButton.disabled = !hasSnapshot;
}

function scheduleAutoRefresh(minutes) {
  if (state.autoRefreshTimer) {
    window.clearInterval(state.autoRefreshTimer);
    state.autoRefreshTimer = null;
  }

  if (minutes > 0) {
    state.autoRefreshTimer = window.setInterval(() => {
      refreshSnapshot({ background: true });
    }, minutes * 60 * 1000);
  }
}

function applySettings(settings) {
  state.settings = settings;
  elements.autoRefreshSelect.value = String(settings.autoRefreshMinutes);
  applyTheme(settings.theme);
  scheduleAutoRefresh(settings.autoRefreshMinutes);
}

async function saveSettings(partialSettings) {
  const settings = await window.slopmeter.saveSettings(partialSettings);
  applySettings(settings);
}

async function refreshSnapshot({ background = false } = {}) {
  if (state.isRefreshing) {
    return;
  }

  setBusy(true);
  setStatus("loading");

  try {
    const snapshot = await window.slopmeter.refresh();
    renderSnapshot(snapshot);
    state.config = {
      ...state.config,
      imageExists: true,
    };
    updateActionAvailability();
    setStatus("success");
  } catch {
    setStatus("error");
  } finally {
    setBusy(false);
  }
}

async function init() {
  elements.refreshButton.addEventListener("click", () => refreshSnapshot());
  elements.openImageButton.addEventListener("click", () => window.slopmeter.openImage());
  elements.showDownloadsButton.addEventListener("click", () => window.slopmeter.showDownloads());
  elements.themeToggleButton.addEventListener("click", async () => {
    const nextTheme = state.settings?.theme === "dark" ? "light" : "dark";
    await saveSettings({ theme: nextTheme });
  });
  elements.autoRefreshSelect.addEventListener("change", async () => {
    await saveSettings({ autoRefreshMinutes: Number(elements.autoRefreshSelect.value) });
  });

  window.slopmeter.onSnapshotUpdated((snapshot) => {
    renderSnapshot(snapshot);
  });

  window.slopmeter.onSettingsUpdated((settings) => {
    applySettings(settings);
  });

  const config = await window.slopmeter.getConfig();
  applyConfig(config);
  updateActionAvailability();

  const settings = await window.slopmeter.getSettings();
  applySettings(settings);

  const existingSnapshot = await window.slopmeter.getLatestSnapshot();
  if (existingSnapshot) {
    renderSnapshot(existingSnapshot);
    setStatus("success");
  }

  await refreshSnapshot();
}

init();
