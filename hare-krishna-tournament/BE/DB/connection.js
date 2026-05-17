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
    });
  }

  cached.conn = await cached.promise;
  return cached.conn;
}

export default connectDB;
