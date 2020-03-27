const mongoose = require('mongoose');

mongoose.connect(process.env.URI, { useUnifiedTopology: true, useNewUrlParser: true });

const fcmSchema = new mongoose.Schema({
    walletId: Number,
    fcmToken: String,
    memberId: String
},{collection: 'fcmToken'});

module.exports = mongoose.model('fcmToken', fcmSchema);