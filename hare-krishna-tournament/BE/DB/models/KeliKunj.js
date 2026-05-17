import mongoose from 'mongoose';

const { ObjectId } = mongoose.Schema.Types;

const KeliKunjSchema = new mongoose.Schema(
  {
    keliKunjWeek: {
      type:     Number,
      required: true,
      unique:   true,
      min:      1,
    },
    winners: {
      _1: { type: ObjectId, ref: 'KrishnaDas', default: null },
      _2: { type: ObjectId, ref: 'KrishnaDas', default: null },
      _3: { type: ObjectId, ref: 'KrishnaDas', default: null },
    },
    prizePool: {
      _1: { type: Number, default: 0, min: 0 },
      _2: { type: Number, default: 0, min: 0 },
      _3: { type: Number, default: 0, min: 0 },
    },
    resultDeclared: { type: Boolean, default: false },
  },
  {
    collection: 'keliKunj',
    timestamps: true,
  }
);

export default mongoose.models.KeliKunj
  || mongoose.model('KeliKunj', KeliKunjSchema);
