var express = require('express');
var router = express.Router();
const apiController = require('../controller/api');

router.get('/', function(req, res, next){
  res.send({message: "Our Wallet App Api written by QUANG THIEN TRAN"});
});

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
});

router.post('/quit-wallet', function(req, res, next) {
  apiController.quitWallet(req, res, next);
});

router.post('/ban-user', function(req, res, next) {
  apiController.banUser(req, res, next);
});

router.post('/check-superadmin', function(req, res, next) {
  apiController.checkSuperAdmin(req, res, next);
});

router.post('/list-member', function(req, res, next) {
  apiController.loadListMember(req, res, next);
});

router.post('/unban-user', function(req, res, next) {
  apiController.unBanUser(req, res, next);
});

router.post('/ban-edit', function(req, res, next) {
  apiController.banEdit(req, res, next);
});


router.post('/statistic', function(req, res, next) {
  apiController.statistic(req, res, next);
});

router.get('/pay-member-list', function(req, res, next){
  apiController.getPayMemberNameList(req, res, next);
});

router.post('/remove-history', function(req, res, next){
  apiController.removeHistory(req, res, next);
});

router.post('/remove-historys', function(req, res, next) {
  apiController.removeHistorys(req, res, next);
});
module.exports = router;
