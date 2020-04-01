const walletDB = require('../models/wallet');
const memberDB = require('../models/member');
const historyDB = require('../models/history');
const admin = require('../config/firebase');
const fcmDB = require('../models/fcmToken');
const banList = require('../models/banList');

function formatDate(dateStr){
    const tokens = dateStr.split('-');
    return tokens[1] + '-' + tokens[0] + '-' + tokens[2];
}
module.exports = {
    connectWallet: async function(req, res, next){
        const id = req.body.id;
        const pass = req.body.password;
        const memberName = req.body.memberName;
        const fcmToken = req.body.fcmToken;
        const isAuto = req.body.isAuto;
        const deviceId = req.body.deviceId;

        const wallet = await walletDB.findOne({id: id});
        if (wallet === null){
            res.status(404);
            res.send({
                message: "ID not found",
                res: false
            });
        }
        else{
            if (pass == wallet.password)
            {    
                const bannedDevice = await banList.findOne({walletId: id, deviceId: deviceId});
                if (bannedDevice !== null){
                    res.status(404).send({
                        message: "Your device is banned to connects this wallet.",
                        res: false
                    });
                    return;
                }

                if (!isAuto){
                    const sameNameMember = await memberDB.find({name: memberName, walletId: id});
                    if (sameNameMember.length != 0){
                        res.status(404).send({
                            message: "Duplicate member name.",
                            res: false
                        });
                        return;
                    }
                    const member = new memberDB({
                        name: memberName,
                        walletId: id,
                        isBanned: false,
                        isAdmin: true,
                        deviceId: deviceId,
                        isSuperAdmin: false
                    });
                    const newFcm = new fcmDB({
                        walletId: id,
                        fcmToken: fcmToken,
                        memberId: member._id
                    });
                    await newFcm.save();
                    await member.save();
    
                    res.send({
                        res: true,
                        isSuperAdmin: false,
                        isAdmin: true
                    });

                }else{
                    const superAdmin = await memberDB.findById(wallet.superAdminId);
                    const member = await memberDB.findOne({walletId: id, name: memberName});
                    let isSuperAdmin;
                    if (superAdmin.name == memberName) isSuperAdmin = true; else isSuperAdmin = false;    
                    res.send({
                        res: true,
                        isSuperAdmin: isSuperAdmin,
                        isAdmin: member.isAdmin
                    });
                }
            }
            else{
                res.status(404);
                res.send({
                    message: "Password not match",
                    res: false
                });
            }
        }
    },
    createWallet: async function(req, res, next){
        let preWallet = await walletDB.findOne().sort('-id');
        if (preWallet === null)
            preWallet = {id: 0};

        const id = preWallet.id + 1;
        const password = req.body.password;
        const memberName = req.body.memberName;
        const fcmToken = req.body.fcmToken;
        const deviceId = req.body.deviceId;
        try{
            const member = new memberDB({
                name: memberName,
                walletId: id,
                isBanned: false,
                isAdmin: true,
                deviceId: deviceId,
                isSuperAdmin: true
            });
            await member.save();

            const newWallet = new walletDB({
                id: id,
                remain: 0,
                revenue: 0,
                expenditure: 0,
                password: password,
                superAdminId: member._id
            });
            await newWallet.save();

            const newFcm = new fcmDB({
                walletId: id,
                fcmToken: fcmToken,
                memberId: member._id
            });
            await newFcm.save();
            res.send({
                message: "Create wallet successfully.",
                id: id,
                isSuperAdmin: true,
                isAdmin: true
            });
        }catch(e){
            res.status(404);
            res.send({
                message: e.message
            });
        }
    },
    createHistory: async function (req, res, next){
        const id = req.body.walletId;
        const value = req.body.value;
        const isRevenue = req.body.isRevenue;
        const name = req.body.name;
        const payMemberName = req.body.payMemberName;
        const fcm = req.body.fcmToken;
        const describe = req.body.describe;
        const now = new Date();

        const history =  new historyDB({
            walletId: id,
            value: value,
            isRevenue: isRevenue,
            createOn: now,
            name: name,
            describe: describe,
            payMemberName: payMemberName
        });

        await history.save();

        const fcmTokens = await fcmDB.find({walletId: id});
        const regTokenList = [];
        for (i = 0; i < fcmTokens.length; i++){
            regTokenList.push(fcmTokens[i].fcmToken);
        }

        const message = {
            data: {
                fcmToken: fcm,
                type: '1'
            },
            tokens: regTokenList
        }
        admin.messaging().sendMulticast(message)
            .then((res) => {
                console.log("successfully");
            })
        res.send({
            res: true,
            message: "successful"
        });
    },
    loadHistory: async function (req, res, next){
        const id = req.query.id;
        if (id === undefined)
        {
            res.status(404).send({
                message: "wallet id not found."
            });
            return;
        }
        try{
            const history = await historyDB.find({walletId: id}).sort({createOn: -1});
            res.status(200).json(history);
        }catch(e){
            res.status(404).send({
                message: e.message
            });
        }
    
    },
    quitWallet: async function(req, res, next){
        try{
            const id = req.body.id;
            const name = req.body.name;
            const fcmToken = req.body.fcmToken;
            const isSuperAdmin = req.body.isSuperAdmin;

            await memberDB.deleteOne({walletId: id, name: name});
            await fcmDB.deleteOne({fcmToken: fcmToken, walletId: id});

            const members = await memberDB.find({walletId: id});
            if (members.length == 0){
                await walletDB.deleteOne({id: id});
                await historyDB.deleteMany({walletId: id});
            }
            if (isSuperAdmin && members.length != 0){
                const member = await memberDB.findOne({walletId: id});
                member.isSuperAdmin = true;
                await member.save();
                const wallet = await walletDB.findOne({id: id});
                wallet.superAdminId = member._id;
                await wallet.save();
                const fcmData = await fcmDB.findOne({memberId: member._id});
                const message = {
                    data: {
                        type: '5'
                    },
                    token: fcmData.fcmToken
                };
                admin.messaging().send(message)
                    .then((response) => {
                        console.log('Successfully sent message:', response);
                    })
                    .catch((error) => {
                        console.log('Error sending message:', error);
                    });
            }
            res.json({
                res: true
            });
        }catch(e){
            res.status(404).json({
                res: false,
                message: e.message
            })
        }
    },
    banUser: async function(req, res, next){
        try{
            const id = req.body.walletId;
            const superAdminName = req.body.sName;
            const bannedMemberName = req.body.bName;

            const superadmin = await memberDB.findOne({walletId: id, name: superAdminName});
            const wallet = await walletDB.findOne({id: id});
            if (wallet.superAdminId == superadmin._id){
                const bannedMember = await memberDB.findOne({walletId: id, name: bannedMemberName});
                bannedMember.isBanned = true;
                const banListDoc = new banList({
                    deviceId: bannedMember.deviceId,
                    walletId: id,
                    memberId: bannedMember._id
                });
                await banListDoc.save();
                await bannedMember.save();

                const fcmData = await fcmDB.findOne({memberId: bannedMember._id});
                const message = {
                    data: {
                        type: '2'
                    },
                    token: fcmData.fcmToken
                };
                admin.messaging().send(message)
                    .then((response) => {
                        console.log('Successfully sent message:', response);
                    })
                    .catch((error) => {
                        console.log('Error sending message:', error);
                    });
                res.send({
                    res: true
                });
            }else{
                res.status(404).send({
                    res: false,
                    message: "You have not permission to use this funtion."
                });
                return;
            }
        }catch(e){
            res.status(404).send({
                res: false,
                message: e.message
            });
            return;
        }
    },
    checkSuperAdmin: async function(req, res, next){
        const id = req.body.walletId;
        const name = req.body.name;

        const superadmin = await memberDB.findOne({walletId: id, name: name});
        const wallet = await walletDB.findOne({id: id});

        if (wallet.superAdminId == superadmin._id){
            res.send({
                res: true
            })
        }else{
            res.send({
                res: false,
            })
        }
    },
    loadListMember: async function(req, res, next){
        const walletId = req.body.walletId;
        try {
            const memebers = await memberDB.find({walletId: walletId});
            res.json(memebers);
        } catch (error) {
            res.status(404).send({
                message: error.message
            });
        }
        
    },
    unBanUser: async function(req, res, next){
        try {
            const id = req.body.walletId;
            const name = req.body.name;
            const member = await memberDB.findOne({walletId: id, name: name});
            member.isBanned = false;
            await member.save();
            await banList.deleteOne({memberId: member._id});
            const fcmData = await fcmDB.findOne({memberId: member._id});
            const message = {
                data: {
                    type: '3'
                },
                token: fcmData.fcmToken
            }
            admin.messaging().send(message)
                .then((response) => {
                    console.log('Successfully sent message:', response);
                })
                .catch((error) => {
                    console.log('Error sending message:', error);
            });
        res.send({
            res: true
        });
            res.send({
                res: true
            })
        } catch (error) {
            res.status(404).send({
                res:false,
                mesaage: error.message
            });
        }
    },
    banEdit: async function(req, res, next){
        const id = req.body.walletId;
        const name = req.body.name;
        const isAdmin = req.body.isAdmin;

        try {
            const member = await memberDB.findOne({walletId: id, name: name});
            member.isAdmin = isAdmin;
            await member.save();

            const fcmData = await fcmDB.findOne({memberId: member._id});
            let isAdminStr;
            if (isAdmin) isAdminStr = '1'; else isAdminStr = '0';
            const message = {
                data: {
                    type: '4',
                    isAdmin: isAdminStr
                },
                token: fcmData.fcmToken
            }
            admin.messaging().send(message)
                .then((response) => {
                    console.log('Successfully sent message:', response);
                })
                .catch((error) => {
                    console.log('Error sending message:', error);
            });
            res.send({
                res: true
            });
        } catch (error) {
            res.status(404).send({
                res: false,
                message: error.message
            });
        }
    },
    statistic: async function(req, res, next){
        let oriDate = req.body.oriDate;
        let desDate = req.body.desDate;
        const id = req.body.walletId;
        if (oriDate === undefined || desDate === undefined){
            res.status(404).send({
                res: false,
                message: "invaid var"
            });
            return;
        }
        try{
            oriDate = new Date(formatDate(oriDate));
            desDate = new Date(formatDate(desDate));
            if (oriDate > desDate)
            {
                res.status(404).send({
                    res: false,
                    message: "invaid date"
                });
                return;
            }
            desDate.setDate(desDate.getDate() + 1);

            const historys = await historyDB.find({walletId: id, createOn: {$gte: oriDate, $lte: desDate}}).sort({createOn: -1});
            res.json(historys);
        } catch (error) {
            res.status(404).send({
                res: false, 
                message: error.message
            });
            return;
        }
    },
    getPayMemberNameList: async function(req, res, next){
        const id = req.query.walletId;
        try {
            let historys = await historyDB.find({walletId: id}).sort({payMemberName: 1});
            let result = [];
            for (i = 1; i < historys.length; i++){
                if(historys[i].payMemberName == historys[i-1].payMemberName){
                    historys.splice(i, 1);
                    i--;
                }
                
            }
            for (i = 0; i < historys.length; i++){
                result.push(historys[i].payMemberName);
            }
            res.json(result);    
        } catch (error) {
            res.status(404).send({
                res: false,
                message: error.message
            });
        }
    },
    removeHistory: async function(req, res, next){
        try {
            const id = req.body.walletId;
            const historyId = req.body.historyId;
            const fcm = req.body.fcmToken;
            await historyDB.deleteOne({_id: historyId});

            const fcmTokens = await fcmDB.find({walletId: id});
            const regTokenList = [];
            for (i = 0; i < fcmTokens.length; i++){
                regTokenList.push(fcmTokens[i].fcmToken);
            }

            const message = {
                data: {
                    fcmToken: fcm,
                    type: '1'
                },
                tokens: regTokenList
            }
            admin.messaging().sendMulticast(message)
                .then((res) => {
                    console.log("successfully");
                });
            res.send({
                res: true
            })
        } catch (error) {
            res.status(404).send({
                res: false,
                message: error.message
            })
        }
    },

    removeHistorys: async function(req, res, next){
        let oriDate = req.body.oriDate;
        let desDate = req.body.desDate;
        const id = req.body.walletId;
        const fcm = req.body.fcmToken;
        if (oriDate === undefined || desDate === undefined){
            res.status(404).send({
                res: false,
                message: "invaid var"
            });
            return;
        }
        try{
            oriDate = new Date(formatDate(oriDate));
            desDate = new Date(formatDate(desDate));
            if (oriDate > desDate)
            {
                res.status(404).send({
                    res: false,
                    message: "invaid date"
                });
                return;
            }
            desDate.setDate(desDate.getDate() + 1);

            await historyDB.deleteMany({walletId: id, createOn: {$gte: oriDate, $lte: desDate}});

            const fcmTokens = await fcmDB.find({walletId: id});
            const regTokenList = [];
            for (i = 0; i < fcmTokens.length; i++){
                regTokenList.push(fcmTokens[i].fcmToken);
            }

            const message = {
                data: {
                    fcmToken: fcm,
                    type: '1'
                },
                tokens: regTokenList
            }
            admin.messaging().sendMulticast(message)
                .then((res) => {
                    console.log("successfully");
                })
            res.send({
                res: true
            });
        } catch (error) {
            res.status(404).send({
                res: false, 
                message: error.message
            });
            return;
        }
    }

}