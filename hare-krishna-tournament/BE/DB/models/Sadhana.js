import mongoose from 'mongoose';

const niyamField = {
  point:  { type: Number, default: 0, min: 0 },
  doneAt: { type: Date,   default: null },
};

const SadhanaSchema = new mongoose.Schema(
  {
    krishnadasId: {
      type:     mongoose.Schema.Types.ObjectId,
      ref:      'KrishnaDas',
      required: true,
    },
    date: {
      type:     Date,
      required: true,   // stores midnight UTC of the day (2025-05-17T00:00:00.000Z)
    },
    naamJaapCount: { type: Number, default: 0, min: 0 },
    niyam1: niyamField,
    niyam2: niyamField,
    niyam3: niyamField,
  },
  {
    collection: 'sadhana',
    timestamps: true,
  }
);

// One record per krishnadas per day
SadhanaSchema.index({ krishnadasId: 1, date: 1 }, { unique: true });

export default mongoose.models.Sadhana
  || mongoose.model('Sadhana', SadhanaSchema);
