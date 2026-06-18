
const crypto = require('crypto');
const TOL = parseInt(process.env.WEBHOOK_TOLERANCE_SEC || '300', 10);
const SECRET = process.env.WEBHOOK_SECRET || 'dev';
function verify(signatureHeader, body){
  const parts = Object.fromEntries(signatureHeader.split(',').map(s => s.trim().split('=')));
  if (parts['v'] !== '2') return false;
  const ts = parseInt(parts['t'] || '0', 10);
  if (!ts || Math.abs(Math.floor(Date.now()/1000) - ts) > TOL) return false;
  const expected = crypto.createHmac('sha256', SECRET).update(ts + '.' + body).digest('hex');
  return expected === parts['sha256'];
}
module.exports = { verify };
