import mongoose from 'mongoose';

const MONGODB_URI = 'mongodb+srv://kishor:hareKrishna@cluster0.d8sjq72.mongodb.net/?appName=Cluster0';
const DB_NAME     = 'shriKrishna';

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
      // Each Vercel function instance keeps its own pool. The default
      // (Mongoose 9 = 100) is fine; explicit here so it's visible.
      maxPoolSize:          50,
      // If the cluster doesn't answer in 5s, fail fast — better to return
      // a quick 503 than to hold the request for 300s.
      serverSelectionTimeoutMS: 5000,
      socketTimeoutMS:          15000,
    });
  }

  cached.conn = await cached.promise;
  return cached.conn;
}

export default connectDB;
