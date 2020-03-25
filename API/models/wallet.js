const mongoose = require('mongoose');

mongoose.connect(process.env.URI, { useUnifiedTopology: true, useNewUrlParser: true });

const walletSchema = new mongoose.Schema({
    id: Number,
    remain: Number,
    revenue: Number,
    expenditure: Number,
    password: String,
    superAdminId: String
},{collection: 'wallet'});

module.exports = mongoose.model('wallet', walletSchema);