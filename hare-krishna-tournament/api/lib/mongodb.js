import { MongoClient } from 'mongodb';

const uri = process.env.MONGODB_URI;

// Cache connection across warm serverless invocations
let client;
let clientPromise;

if (!uri) {
  throw new Error('Please add MONGODB_URI to environment variables');
}

if (!global._mongoClientPromise) {
  client = new MongoClient(uri);
  global._mongoClientPromise = client.connect();
}
clientPromise = global._mongoClientPromise;

export default clientPromise;
