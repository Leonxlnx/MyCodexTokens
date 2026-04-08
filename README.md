# MyCodexTokens

<p align="center">
  <img src="assets/icon.png" alt="MyCodexTokens logo" width="96" height="96">
</p>

<p align="center">
  A clean desktop wrapper for <a href="https://github.com/JeanMeijer/slopmeter">slopmeter</a> that keeps Codex usage visible without living in the terminal.
</p>

<p align="center">
  <img src="assets/readme-preview.svg" alt="MyCodexTokens interface preview">
</p>

## Overview

MyCodexTokens is a lightweight Electron app for people who use Codex heavily and want a cleaner way to check token activity.

It runs `slopmeter` locally, saves the latest export into your Downloads folder, and presents the result in a flat desktop UI with theme-aware heatmaps, quick refresh controls, and a clearer daily split.

## Highlights

- One-click refresh for the latest Codex heatmap
- Light and dark application themes
- Light mode also regenerates a light `slopmeter` image
- Daily split with focused `Today` and `Yesterday` sections
- Current streak and longest streak at a glance
- Local exports saved straight into Downloads

## Requirements

- [Node.js 22+](https://nodejs.org/)
- Codex usage data already available on the machine
- A system that can run Electron, including Windows and macOS

## Getting Started

```bash
npm install
npm start
```

The app will refresh automatically on launch and save two files in your Downloads folder:

- `mycodextokens-codex-heatmap.png`
- `mycodextokens-codex-usage.json`

## Usage Notes

- Use `Refresh` whenever you want a new snapshot immediately.
- Change the theme with the theme button. The next refresh will regenerate the heatmap in the matching color mode.
- Use `Downloads` to jump to the exported files quickly.
- Use `Open PNG` to open the latest rendered heatmap directly.

## Packaging

```bash
npm run package:win
npm run package:mac
```

The packaging flow stages a minimal app directory first and increases the Node heap size to reduce Electron Builder memory issues.

## Project Structure

```text
assets/      Icons and README preview assets
electron/    Electron main and preload processes
scripts/     Build staging helpers
src/         Renderer logic and styles
index.html   Main application shell
```

## Tips

- Keep Codex open long enough for its local usage data to settle before refreshing.
- If the PNG looks out of date, run another refresh instead of relying on an old export.
- If you switch from dark to light mode, wait for the refresh to finish so the image and UI match.

## Credits

- Heatmap generation by [slopmeter](https://github.com/JeanMeijer/slopmeter)
- Desktop shell powered by [Electron](https://www.electronjs.org/)
