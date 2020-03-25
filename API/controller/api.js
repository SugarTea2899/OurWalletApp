const walletDB = require('../models/wallet');
const memberDB = require('../models/member');
const historyDB = require('../models/history');
const admin = require('../config/firebase');
const fcmDB = require('../models/fcmToken');
const banList = require('../models/banList');
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
                        fcmToken: fcmToken
                    });
                    await newFcm.save();
                    await member.save();
    
                    res.send({
                        res: true,
                        isSuperAdmin: false
                    });

                }else{
                    const superAdmin = await memberDB.findById(wallet.superAdminId);
                    let isSuperAdmin;
                    if (superAdmin.name == memberName) isSuperAdmin = true; else isSuperAdmin = false;    
                    res.send({
                        res: true,
                        isSuperAdmin: isSuperAdmin
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
                fcmToken: fcmToken
            });
            await newFcm.save();
            res.send({
                message: "Create wallet successfully.",
                id: id,
                isSuperAdmin: true
            });
        }catch(e){
            res.status(404);
            res.send({
                message: "failed"
            });
        }
    },
    createHistory: async function (req, res, next){
        const id = req.body.walletId;
        const value = req.body.value;
        const isRevenue = req.body.isRevenue;
        const name = req.body.name;
        const fcm = req.body.fcmToken;
        const describe = req.body.describe;
        const now = new Date();

        const history =  new historyDB({
            walletId: id,
            value: value,
            isRevenue: isRevenue,
            createOn: now,
            name: name,
            describe: describe
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
                message: "failed to load history."
            });
        }
    
    },
    quitWallet: async function(req, res, next){
        try{
            const id = req.body.id;
            const name = req.body.name;
            const fcmToken = req.body.fcmToken;

            await memberDB.deleteOne({walletId: id, name: name});
            await fcmDB.deleteOne({fcmToken: fcmToken, walletId: id});

            const members = await memberDB.find({walletId: id});
            if (members.length == 0){
                await walletDB.deleteOne({id: id});
                await historyDB.deleteMany({walletId: id});
            }
            res.json({
                res: true
            });
        }catch(e){
            res.status(404).json({
                res: false
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
                message: "Exception..."
            });
            next(e);
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
                message: "Load list member failed."
            });
        }
        
    }
}