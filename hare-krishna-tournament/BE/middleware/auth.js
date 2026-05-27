import { verifyToken } from '../services/authService.js';

// During v1 rollout, STRICT_AUTH=false (default) lets unauthenticated requests
// through write endpoints so the existing web admin keeps working until it
// learns to send a token. Flip STRICT_AUTH=true on Vercel once the web admin
// login is shipped and every contestant has a password. See AUTH.md §11.
const STRICT = process.env.STRICT_AUTH === 'true';

function extractToken(req) {
  const header = req.headers.authorization || '';
  if (header.startsWith('Bearer ')) return header.slice(7);
  return null;
}

export const requireAuth = (handler) => async (req, res) => {
  const token = extractToken(req);
  if (!token) {
    if (STRICT) return res.status(401).json({ error: 'Authentication required' });
    req.auth = null;
    return handler(req, res);
  }
  try {
    req.auth = verifyToken(token);
  } catch {
    return res.status(401).json({ error: 'Invalid or expired token' });
  }
  return handler(req, res);
};

export const requireAdmin = (handler) => requireAuth(async (req, res) => {
  if (!req.auth) {
    if (STRICT) return res.status(403).json({ error: 'Admin access required' });
    return handler(req, res);
  }
  if (req.auth.role !== 'admin') {
    return res.status(403).json({ error: 'Admin access required' });
  }
  return handler(req, res);
});
