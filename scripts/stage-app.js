const fs = require("node:fs/promises");
const path = require("node:path");

const rootDir = process.cwd();
const stageDir = path.join(rootDir, ".app-stage");

async function copyEntry(relativePath) {
  const source = path.join(rootDir, relativePath);
  const target = path.join(stageDir, relativePath);
  await fs.cp(source, target, {
    recursive: true,
    force: true,
  });
}

async function main() {
  const packageJsonPath = path.join(rootDir, "package.json");
  const packageJson = JSON.parse(await fs.readFile(packageJsonPath, "utf8"));

  await fs.rm(stageDir, {
    recursive: true,
    force: true,
  });

  await fs.mkdir(stageDir, {
    recursive: true,
  });

  for (const entry of ["electron", "src", "assets", "index.html", "README.md"]) {
    await copyEntry(entry);
  }

  const stagedPackage = {
    name: `${packageJson.name}-app`,
    version: packageJson.version,
    description: packageJson.description,
    main: packageJson.main,
    author: packageJson.author,
    license: packageJson.license,
    dependencies: packageJson.dependencies,
  };

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
