'use strict';

module.exports = {
  addControlledFailure,
  addControlledContinuouslyFailure,
  failTheCallIfConfigured,
};

const SHOULD_FAIL_CONTINUOUSLY_PREFIX = "#shouldFailContinuously=";
let failures = {};

function addControlledFailure(req, res) {
  const traceId = req.body.traceId;
  const requestName = req.body.requestName;
  const statusCode = req.body.statusCode;

  _validateFailRequest(req, res);
  _saveCallToFail(requestName, traceId, statusCode, false);

  console.log("Registered request to fail with " + req.body.statusCode + " " + req.body.requestName +  " " + req.body.traceId)

  res.status(200).json();
}

function addControlledContinuouslyFailure(req, res) {
  const traceId = req.body.traceId;
  const requestName = req.body.requestName;
  const statusCode = req.body.statusCode;

  _validateFailRequest(req, res);
  _saveCallToFail(requestName, traceId, statusCode, true);

  console.log("Registered request to continuously fail with " + req.body.statusCode + " " + req.body.requestName +  " " + req.body.traceId)

  res.status(200).json();
}

function failTheCallIfConfigured(callType, req, res) {
  let traceId = req.header('trace-id');

  let responseCodeForFailure = _getFailureResponseCode(callType, traceId);

  if (responseCodeForFailure) {
    console.log("Controlled failure ", callType, traceId, responseCodeForFailure);
    res.status(responseCodeForFailure).json();
    return true;
  }

  return false;
}

function _validateFailRequest(req, res) {
  const traceId = req.body.traceId;
  const requestName = req.body.requestName;
  const statusCode = req.body.statusCode;

  if (!statusCode || ! traceId || !requestName) {
    return res.status(400).json('bad request');
  }
}

function _saveCallToFail(callType, traceId, responseCode, shouldFailContinuously) {
  const failureKey = getKeyForFailureMap(callType, traceId);
  failures[failureKey] = `${responseCode}${SHOULD_FAIL_CONTINUOUSLY_PREFIX}${shouldFailContinuously}`;
}

function _getFailureResponseCode(callType, traceId) {
  const failureKey = getKeyForFailureMap(callType, traceId);
  const failureValue = failures[failureKey];

  if (failureValue) {
    const failureValueList = failureValue.split(SHOULD_FAIL_CONTINUOUSLY_PREFIX);
    const responseCode = failureValueList[0];
    const shouldFailContinuously = failureValueList[1];

    if (shouldFailContinuously === 'false') {
      delete failures[failureKey];
    } 
    return responseCode;
  } else {
    return undefined;
  }
}

function getKeyForFailureMap(callType, traceId) {
  return `${callType}/${traceId}`;
}
