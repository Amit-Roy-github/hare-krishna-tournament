import bcrypt from 'bcryptjs';
import jwt    from 'jsonwebtoken';

const HASH_ROUNDS  = 10;
const TOKEN_EXPIRY = '30d';

// Hardcoded for local dev convenience. For production, replace with
// `process.env.JWT_SECRET` and set the value in Vercel project settings.
// Rotate this string to invalidate every existing session.
const JWT_SECRET = 'kHc7nP2vXq8mLr5wF9jB3yT4sU6aZdGeH1iN0oRpYuV8WxQbAaSfDk';

function getSecret() {
  const secret = process.env.JWT_SECRET || JWT_SECRET;
  if (!secret) throw new Error('JWT secret not configured');
  return secret;
}

export async function hashPassword(plain) {
  return bcrypt.hash(plain, HASH_ROUNDS);
}

export async function verifyPassword(plain, hash) {
  if (!hash) return false;
  return bcrypt.compare(plain, hash);
}

export function signToken({ bhaktName, role }) {
  return jwt.sign({ sub: bhaktName, role }, getSecret(), { expiresIn: TOKEN_EXPIRY });
}

export function verifyToken(token) {
  return jwt.verify(token, getSecret());
}
