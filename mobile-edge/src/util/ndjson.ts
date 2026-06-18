export const toNdjson=(rows:any[])=>Buffer.from(rows.map(r=>JSON.stringify(r)).join('\n'),'utf8');
