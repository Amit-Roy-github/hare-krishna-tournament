import mongoose from 'mongoose';

const MONGODB_URI = process.env.MONGODB_URI;
const DB_NAME     = 'shriKrishna';

if (!MONGODB_URI) {
  throw new Error('Please add MONGODB_URI to environment variables');
}

// Cache connection across warm serverless invocations
let cached = global._mongooseCache;

if (!cached) {
  cached = global._mongooseCache = { conn: null, promise: null };
}

async function connectDB() {
  if (cached.conn) return cached.conn;

  if (!cached.promise) {
    cached.promise = mongoose.connect(MONGODB_URI, {
      dbName:               DB_NAME,
      bufferCommands:       false,
    });
  }

  cached.conn = await cached.promise;
  return cached.conn;
}

export default connectDB;
