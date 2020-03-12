const walletDB = require('../models/wallet');
const memberDB = require('../models/member');
const historyDB = require('../models/history');
module.exports = {
    connectWallet: async function(req, res, next){
        const id = req.body.id;
        const pass = req.body.password;
        const memberName = req.body.memberName;

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
                const member = new memberDB({
                    name: memberName,
                    walletId: id
                });
                await member.save();

                res.send({
                    res: true
                });
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
        const preWallet = await walletDB.findOne().sort('-id');
        const id = preWallet.id + 1;
        const password = req.body.password;
        const memberName = req.body.memberName;

        const newWallet = new walletDB({
            id: id,
            remain: 0,
            revenue: 0,
            expenditure: 0,
            password: password
        });
        try{
            await newWallet.save();

            const member = new memberDB({
                name: memberName,
                walletId: id
            });
            await member.save();
            res.send({
                message: "Create wallet successfully.",
                id: id
            })
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
    
    }
}