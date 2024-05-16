'use strict';

const fail = require('../controllers/sfdcFail.js');
const requestsLog = require('./requestsLog');

module.exports = {
    createNewAccount,
};

let allCreateNewAccountRequests = [];

function createNewAccount(req, res) {
    const traceId = req.header('trace-id');
    const CALL_TYPE = "createNewAccount";

    const isCallFailed = fail.failTheCallIfConfigured(CALL_TYPE, req, res);
    if (isCallFailed) {
        return;
    }

    validateAuthorizationHeader(req, res);

    console.log("Received post account request in sfdc stub");
    requestsLog.logNewRequest(CALL_TYPE, req);
    allCreateNewAccountRequests.push(req.body);

    res.status(200).json();
}

function validateAuthorizationHeader(req, res) {
    const expectedAuthorizationHeader = 'Bearer ' + Buffer.from('stub_sfdc_token').toString('base64');

    if (req.headers['authorization'] != expectedAuthorizationHeader) {
        console.log('The Authorization header has incorrect value.');
        res.status(401).json();
        return;
    }
}