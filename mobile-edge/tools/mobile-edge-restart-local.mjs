import { dirname, resolve } from "node:path";
import { spawn, spawnSync } from "node:child_process";
import { fileURLToPath } from "node:url";

const root = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const port = Number.parseInt(process.env.PORT || "8080", 10);

if (!Number.isInteger(port) || port < 1 || port > 65535) {
  console.error("PORT must be a valid TCP port.");
  process.exit(1);
}

function listeningProcessIds(targetPort) {
  const result = spawnSync("netstat", ["-ano", "-p", "tcp"], { encoding: "utf8" });

  if (0 !== result.status) {
    return [];
  }

  const processIds = new Set();

  for (const line of result.stdout.split(/\r?\n/)) {
    if (!line.includes(`:${targetPort}`) || !/LISTENING/i.test(line)) {
      continue;
    }

    const parts = line.trim().split(/\s+/);
    const processId = Number.parseInt(parts[parts.length - 1] || "", 10);

    if (Number.isInteger(processId) && processId > 0 && processId !== process.pid) {
      processIds.add(processId);
    }
  }

  return [...processIds];
}

for (const processId of listeningProcessIds(port)) {
  const result = spawnSync("taskkill", ["/PID", String(processId), "/F"], { encoding: "utf8" });

  if (0 !== result.status) {
    console.error(`Failed to stop process ${processId}: ${result.stderr || result.stdout}`.trim());
    process.exit(1);
  }

  console.log(`Stopped mobile-edge listener process ${processId}.`);
}

const startCommand = `Start-Process -FilePath "${process.execPath}" -ArgumentList "dist/app.js" -WorkingDirectory "${root}" -WindowStyle Hidden`;
const startResult = spawnSync("powershell.exe", ["-NoProfile", "-Command", startCommand], {
  encoding: "utf8",
});

if (0 !== startResult.status) {
  console.error(`Failed to start mobile-edge: ${startResult.stderr || startResult.stdout}`.trim());
  process.exit(1);
}

let healthy = false;

for (let attempt = 0; attempt < 20; attempt += 1) {
  try {
    const response = await fetch(`http://127.0.0.1:${port}/health`, { signal: AbortSignal.timeout(1000) });

    if (response.ok) {
      healthy = true;
      console.log(`mobile-edge restarted on http://127.0.0.1:${port}`);
      break;
    }
  } catch {
  }

  await new Promise((resolveWait) => setTimeout(resolveWait, 250));
}

if (!healthy) {
  console.error(`mobile-edge did not become healthy on port ${port}.`);
  process.exit(1);
}
