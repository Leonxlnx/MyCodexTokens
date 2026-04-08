# MyCodexTokens

MyCodexTokens is a small Electron desktop app that wraps `slopmeter` in a denser, cleaner UI. It gives you a flat Swiss-style dashboard, light and dark themes, and one-click refresh/export into your Downloads folder.

## What It Does

- Runs `slopmeter` locally for `Codex`
- Saves the latest PNG and JSON into your Downloads folder
- Shows today and yesterday totals with input, output, and cached input
- Supports manual refresh plus auto-refresh while the app stays open

## Local Development

```bash
npm install
npm start
```

## Packaging

```bash
npm run package:win
npm run package:mac
```

The packaging scripts raise the Node heap size to reduce Electron Builder memory failures during packaging.

## Output Files

The app overwrites these files in your Downloads folder:

- `mycodextokens-codex-heatmap.png`
- `mycodextokens-codex-usage.json`

## Notes

- The app bundles `slopmeter`, so it does not rely on a global `npx`
