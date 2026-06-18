export type NdjsonObject = Record<string, unknown>;

export const toNdjson = (rows: unknown[]): Buffer =>
  Buffer.from(rows.map((row) => JSON.stringify(row)).join('\n'), 'utf8');

export function parseNdjson(input: string | Buffer): NdjsonObject[] {
  const text = Buffer.isBuffer(input) ? input.toString('utf8') : input;

  return text
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line) => JSON.parse(line))
    .filter((value): value is NdjsonObject =>
      value !== null && typeof value === 'object' && !Array.isArray(value)
    );
}
