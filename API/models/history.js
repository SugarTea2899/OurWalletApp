const mongoose = require('mongoose');

mongoose.connect(process.env.URI, { useUnifiedTopology: true, useNewUrlParser: true });

const historySchema = new mongoose.Schema({
    name: String,
    walletId: Number,
    value: Number,
    isRevenue: Boolean,
    createOn: Date,
    describe: String
},{collection: 'history'});

module.exports = mongoose.model('history', historySchema);