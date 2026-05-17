import mongoose from 'mongoose';

const KrishnadasSchema = new mongoose.Schema(
  {
    name:        { type: String, required: true, trim: true },
    email:       { type: String, trim: true,     lowercase: true },
    phone:       { type: String, trim: true },
    sansarName:  { type: String, trim: true },
  },
  {
    collection:  'krishnadas',
    timestamps:  true,           // auto createdAt & updatedAt
  }
);

export default mongoose.models.Krishnadas
  || mongoose.model('Krishnadas', KrishnadasSchema);
