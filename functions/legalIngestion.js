const crypto = require("crypto");
const { LEGAL_SOURCES } = require("./legalSources");

const FETCH_TIMEOUT_MS = 30_000;
const MAX_BODY_BYTES = 8 * 1024 * 1024;

function sha256(value) {
  return crypto.createHash("sha256").update(value).digest("hex");
}

function normalizeForComparison(body) {
  return body
    .replace(/\r\n/g, "\n")
    .replace(/\s+/g, " ")
    .trim();
}

async function fetchSource(source) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), FETCH_TIMEOUT_MS);

  try {
    const response = await fetch(source.url, {
      method: "GET",
      redirect: "follow",
      headers: {
        "User-Agent": "PocketLegalAdvice-LegalSourceMonitor/1.0",
        "Accept": "text/html,application/xhtml+xml,application/pdf;q=0.9,*/*;q=0.8",
      },
      signal: controller.signal,
    });

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }

    const contentLength = Number(response.headers.get("content-length") || 0);
    if (contentLength > MAX_BODY_BYTES) {
      throw new Error("Source response exceeds configured size limit");
    }

    const body = await response.text();
    if (Buffer.byteLength(body, "utf8") > MAX_BODY_BYTES) {
      throw new Error("Source response exceeds configured size limit");
    }

    const normalized = normalizeForComparison(body);
    return {
      ok: true,
      status: response.status,
      finalUrl: response.url,
      contentType: response.headers.get("content-type") || "unknown",
      contentHash: sha256(normalized),
      byteLength: Buffer.byteLength(body, "utf8"),
    };
  } finally {
    clearTimeout(timeout);
  }
}

function validateSourceDefinition(source) {
  if (!source.id || !source.url || !source.jurisdiction || !source.sourceType) {
    throw new Error(`Invalid source definition: ${JSON.stringify(source)}`);
  }
  if (!["ALASKA", "FEDERAL"].includes(source.jurisdiction)) {
    throw new Error(`Unsupported jurisdiction: ${source.jurisdiction}`);
  }
  const parsed = new URL(source.url);
  const allowedHosts = new Set([
    "www.akleg.gov",
    "akleg.gov",
    "courts.alaska.gov",
    "public.courts.alaska.gov",
    "uscode.house.gov",
  ]);
  if (!allowedHosts.has(parsed.hostname)) {
    throw new Error(`Non-authoritative host rejected: ${parsed.hostname}`);
  }
}

async function inspectAllSources() {
  const results = [];
  for (const source of LEGAL_SOURCES) {
    validateSourceDefinition(source);
    try {
      const snapshot = await fetchSource(source);
      results.push({
        ...source,
        ...snapshot,
        checkedAt: new Date().toISOString(),
        status: "CHECKED",
      });
    } catch (error) {
      results.push({
        ...source,
        checkedAt: new Date().toISOString(),
        status: "FAILED",
        error: error instanceof Error ? error.message : String(error),
      });
    }
  }
  return results;
}

module.exports = {
  inspectAllSources,
  sha256,
};
