const mongoose = require('mongoose');

mongoose.connect(process.env.URI, { useUnifiedTopology: true, useNewUrlParser: true });

const banListSchema = new mongoose.Schema({
    deviceId: String,
    walletId: Number,
    memberId: String
},{collection: 'banList'});

module.exports = mongoose.model('banList', banListSchema);