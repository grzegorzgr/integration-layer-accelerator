'use strict';

module.exports = {
    token: token
};

function token(req, res) {
    if (!_isValidRequest(req.body)) {
        res.status(400).send({
            "error" : "invalid_request",
            "error_description" : "One of the parameters is invalid",
            "error_uri": "string"
        });
        return;
    }

    let body = {
        "access_token": Buffer.from('stub_sfdc_token').toString('base64'),
        "instance_url": "https://concardis--SMEJoinDev.cs109.my.salesforce.com",
        "id": "https://test.salesforce.com/id/00D0Q0000000S0pUAE/0050Q000000V2DtQAK",
        "token_type": "Bearer",
        "issued_at": "1543416141742",
        "signature": "mpoZjPKDGkAVCG6UhMbqj5wiRVuoLdKptmyZlSj34mU="
    };
    res.status(201).json(body);
}

function _isValidRequest(body) {
    return (!isValid(body.username) || !isValid(body.password) || !isValid(body.client_id) || !isValid(body.client_secret)) ? false : true;
}

function isValid(param) {
    let defaultTokenRequestValue = 'hello';
    return param != defaultTokenRequestValue ? false : true
}