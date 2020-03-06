var express = require('express');
var router = express.Router();
const apiController = require('../controller/api');

/* GET home page. */
router.post('/connect-wallet', function(req, res, next) {
  apiController.connectWallet(req, res, next);
});

router.post('/create-wallet', function(req, res, next) {
    apiController.createWallet(req, res, next);
});

router.post('/create-history', function(req, res, next) {
  apiController.createHistory(req, res, next);
});

router.get('/load-history', function(req, res, next){
  apiController.loadHistory(req, res, next);
})
module.exports = router;
