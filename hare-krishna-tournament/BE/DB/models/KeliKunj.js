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
      _1:               { type: ObjectId, ref: 'KrishnaDas', default: null },
      _2:               { type: ObjectId, ref: 'KrishnaDas', default: null },
      _3:               { type: ObjectId, ref: 'KrishnaDas', default: null },
      maxNaamJaap:      { type: ObjectId, ref: 'KrishnaDas', default: null },
      totalMaxNaamJaap: { type: ObjectId, ref: 'KrishnaDas', default: null },
    },
    prizePool: {
      _1:               { prize: { type: Number, default: 0, min: 0 }, title: { type: String, default: 'Champion' } },
      _2:               { prize: { type: Number, default: 0, min: 0 }, title: { type: String, default: 'Runner Up' } },
      _3:               { prize: { type: Number, default: 0, min: 0 }, title: { type: String, default: '2nd Runner Up' } },
      maxNaamJaap:      { prize: { type: Number, default: 0, min: 0 }, title: { type: String, default: 'Naam Jaap Hero' } },
      totalMaxNaamJaap: { prize: { type: Number, default: 0, min: 0 }, title: { type: String, default: 'Naam Jaap Legend' } },
    },
    resultDeclared:  { type: Boolean, default: false },
    showLeaderboard: { type: Boolean, default: false },
  },
  {
    collection: 'keliKunj',
    timestamps: true,
  }
);

export default mongoose.models.KeliKunj
  || mongoose.model('KeliKunj', KeliKunjSchema);
