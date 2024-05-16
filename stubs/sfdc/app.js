'use strict';

const SwaggerExpress = require('swagger-express-mw');
const app = require('express')();
var express = require('express');
const bodyParser = require('body-parser');
const formData = require("express-form-data");

const morgan = require('morgan');
const fs = require('fs');
const os = require("os");

const http = require('http');

const requestsLog = require('./api/controllers/requestsLog');
const fail = require('./api/controllers/sfdcFail');

const contractsDir = "./api/swagger/";

const exposeContract = function(contractFile) {
    const contractConfig = {
        appRoot: __dirname,
        swaggerFile: contractFile
    }

    const contractApp = require('express')();

    SwaggerExpress.create(contractConfig, function(err, swaggerExpress) {
      if (err) { throw err; }

      swaggerExpress.register(contractApp);
      app.use('/', contractApp);

      app.use(bodyParser.json({
          limit: '50mb'
      }));

      app.use(bodyParser.urlencoded({
        limit: '50mb',
        extended: true,
        parameterLimit:50000
      }));
    });
}

fs.readdirSync(contractsDir).forEach(contractFile => {
  if (contractFile.endsWith("oas2.yml")) {
    console.info("Exposing " + contractsDir + contractFile + " in the stub");
    exposeContract(contractsDir + contractFile);
  }
});

SwaggerExpress.create({appRoot: __dirname}, function (err, swaggerExpress) {
    if (err) { throw err; }

    app.use(bodyParser.json({
        limit: '50mb'
    }));

    app.use(bodyParser.urlencoded({
      limit: '50mb',
      extended: true,
      parameterLimit:50000
    }));

// parse data with connect-multiparty.
    app.use(formData.parse({
        uploadDir: os.tmpdir(),
        autoClean: true
    }));
// delete from the request all empty files (size == 0)
    app.use(formData.format());
// change the file objects to fs.ReadStream
    //app.use(formData.stream());
// union the body and the files
    app.use(formData.union());

    // install middleware
    swaggerExpress.register(app);

    morgan.token('all-headers', function (req, res) { return JSON.stringify(req.headers); });
    app.use(morgan(`:remote-addr - :remote-user [:date[clf]] ":method :url HTTP/:http-version" :status :res[content-length] ":referrer" ":user-agent" ":all-headers"`));

    app.post('/requests/fail', fail.addControlledFailure);
    app.post('/requests/fail-continuously', fail.addControlledContinuouslyFailure);
    app.get('/requests/generic/:objectType/:traceId', requestsLog.getRequestsByTraceIdEndpoint);
    app.get('/requests/generic/:objectType/:traceId/url', requestsLog.getRequestUrlByTraceIdEndpoint);
    app.get('/requests', requestsLog.getAllRequests);
    app.delete('/requests', requestsLog.deleteAllRequests);
});

const PORT = 8080;

function startServer(requestHandler) {
    const httpServer = http.createServer(requestHandler);
    httpServer.listen(PORT, () => {
        console.log(`server running on port: ${PORT}`);
    })
}

const sharedRequestHandler = (req, res) => {
    app(req, res)
};

startServer(sharedRequestHandler);
