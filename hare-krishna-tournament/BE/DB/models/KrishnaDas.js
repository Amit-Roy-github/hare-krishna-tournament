import mongoose from 'mongoose';

const KrishnaDasSchema = new mongoose.Schema(
  {
    bhaktName:   { type: String, required: true, trim: true, unique: true },
    email:       { type: String, trim: true, lowercase: true },
    phone:       { type: String, trim: true },
    sansarName:  { type: String, trim: true },
    includeInPlayground: { type: Boolean, default: true },
  },
  {
    collection:  'krishnaDas',
    timestamps:  true,           // auto createdAt & updatedAt
  }
);

export default mongoose.models.KrishnaDas
  || mongoose.model('KrishnaDas', KrishnaDasSchema);
