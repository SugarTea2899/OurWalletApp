const mongoose = require('mongoose');

mongoose.connect(process.env.URI, { useUnifiedTopology: true, useNewUrlParser: true });

const historySchema = new mongoose.Schema({
    name: String, //nguoi thuc hien
    walletId: Number,
    value: Number,
    isRevenue: Boolean,
    createOn: Date,
    describe: String,
    payMemberName: String
},{collection: 'history'});

module.exports = mongoose.model('history', historySchema);