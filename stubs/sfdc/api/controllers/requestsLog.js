// key (LABEL) is: "TYPE/TRACEID", example "postBusinessArea/abcd-abcd-abcd"
// value is request body
let requests = {};

function getRequestsByTraceIdEndpoint(req, res) {

    let objectType = req.params.objectType;
    let traceId = req.params.traceId;

    let key = getRequestLabel(objectType, traceId)

    if (requests[key]) {
        return res.json(requests[key])
    }

    return res.status(404).json('No requests for given objectType + traceId')
}

function getRequestUrlByTraceIdEndpoint(req, res) {

    let objectType = req.params.objectType;
    let traceId = req.params.traceId;

    let key = getRequestLabel(objectType, traceId, "url")

    if (requests[key]) {
        return res.json(requests[key])
    }

    return res.status(404).json('No url requests for given objectType + traceId')
}

function getAllRequests(req, res) {
    return res.json(requests);
}

function deleteAllRequests(req, res) {
    requests={};
    return res.status(200).send('deleted');;
}

function logNewRequest(objectType, req) {
    let traceId = req.header("trace-id");
    let body = req.body;

    let key = getRequestLabel(objectType, traceId);

    if (!requests[key]) {
        requests[key] = [];
    }

    requests[key].push(body);
    console.log("Logged new request with label", key)
}

function logNewRequestUrl(objectType, req) {
    let traceId = req.header("trace-id");
    let url = req.url;

    let key = getRequestLabel(objectType, traceId, "url");

    if (!requests[key]) {
        requests[key] = [];
    }

    requests[key].push(url);
    console.log("Logged new request with label", key)
}

function getRequestLabel(objectType, traceId, logType = "body") {
    return objectType + "/" + traceId + "/" + logType;
}

module.exports = {
    getRequestsByTraceIdEndpoint,
    getRequestUrlByTraceIdEndpoint,
    logNewRequest,
    logNewRequestUrl,
    getAllRequests,
    deleteAllRequests
};