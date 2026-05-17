import mongoose from 'mongoose';

const KrishnadasSchema = new mongoose.Schema(
  {
    bhaktName:   { type: String, required: true, trim: true, unique: true },
    email:       { type: String, trim: true, lowercase: true },
    phone:       { type: String, trim: true },
    sansarName:  { type: String, trim: true },
    includeInPlayground: { type: Boolean, default: true },
  },
  {
    collection:  'krishnadas',
    timestamps:  true,           // auto createdAt & updatedAt
  }
);

export default mongoose.models.Krishnadas
  || mongoose.model('Krishnadas', KrishnadasSchema);
