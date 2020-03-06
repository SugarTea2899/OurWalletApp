const mongoose = require('mongoose');

mongoose.connect(process.env.URI, { useUnifiedTopology: true, useNewUrlParser: true });

const memberSchema = new mongoose.Schema({
    name: String,
    walletId: Number
},{collection: 'member'});

module.exports = mongoose.model('member', memberSchema);