import mongoose from 'mongoose';

// All auth-related fields live under one subdocument so they're cleanly
// separated from the contestant's profile. See android/AUTH.md §2.
const AuthSchema = new mongoose.Schema(
  {
    passwordHash:  { type: String, default: null },                                       // bcrypt hash (60 chars). null = no password yet
    passwordSetAt: { type: Date,   default: null },                                       // last time password was set/changed
    role:          { type: String, enum: ['contestant', 'admin'], default: 'contestant' },
    lastLoginAt:   { type: Date,   default: null },                                       // activity monitoring; nullable
  },
  { _id: false }
);

const KrishnaDasSchema = new mongoose.Schema(
  {
    bhaktName:         { type: String,  required: true, trim: true, unique: true },
    email:             { type: String,  trim: true, lowercase: true },
    phone:             { type: String,  trim: true },
    sansarName:        { type: String,  trim: true },
    includeInKeliKunj: { type: Boolean, default: true },

    auth:              { type: AuthSchema, default: () => ({}) },
  },
  {
    collection:  'krishnaDas',
    timestamps:  true,           // auto createdAt & updatedAt
  }
);

export default mongoose.models.KrishnaDas
  || mongoose.model('KrishnaDas', KrishnaDasSchema);
