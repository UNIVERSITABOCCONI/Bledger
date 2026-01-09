import fs from "fs";
import path from "path";

const buildInfoDir = path.join(__dirname, "..", "artifacts", "build-info");
const files = fs.readdirSync(buildInfoDir)
  .filter(f => f.endsWith(".json"))
  .map(f => ({ f, t: fs.statSync(path.join(buildInfoDir, f)).mtimeMs }))
  .sort((a,b) => b.t - a.t);

if (files.length === 0) throw new Error("No build-info files. Run `npx hardhat compile` first.");

const latest = path.join(buildInfoDir, files[0].f);
const buildInfo = JSON.parse(fs.readFileSync(latest, "utf8"));

/** The exact Standard JSON input Hardhat fed to solc */
const input = buildInfo.input;

fs.writeFileSync(path.join(__dirname, "..", "solc-input.json"), JSON.stringify(input, null, 2));
console.log("Wrote solc-input.json from:", files[0].f);
console.log("solcVersion:", buildInfo.solcVersion);
