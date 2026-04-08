const fs = require("node:fs/promises");
const path = require("node:path");

const rootDir = process.cwd();
const stageDir = path.join(rootDir, ".app-stage");

async function main() {
  const packageJson = JSON.parse(
    await fs.readFile(path.join(rootDir, "package.json"), "utf8")
  );

  const stagedPackage = {
    name: `${packageJson.name}-app`,
    version: packageJson.version,
    description: packageJson.description,
    main: packageJson.main,
    author: packageJson.author,
    license: packageJson.license,
    dependencies: packageJson.dependencies,
  };

  await fs.rm(path.join(stageDir, "node_modules", packageJson.name), {
    recursive: true,
    force: true,
  });

  await fs.writeFile(
    path.join(stageDir, "package.json"),
    JSON.stringify(stagedPackage, null, 2),
    "utf8"
  );
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
