var admin = require("firebase-admin");

var serviceAccount = require("../firebase-sdk.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://wallet-fca43.firebaseio.com"
});


module.exports = admin;