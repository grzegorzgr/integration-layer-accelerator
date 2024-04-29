import "reflect-metadata";
import { container } from 'tsyringe';
import express, { Request, Response } from 'express';
import http from 'http';
import morgan from 'morgan';
import inputValidation, { ajvValidatorOptions } from 'openapi-validator-middleware';

import LogRequestsController from './api/controllers/LogRequestsController';
import FailController from './api/controllers/FailController';
import PetstoreController from './api/controllers/PetstoreController';

const validationOptions: ajvValidatorOptions = {
  framework: 'express',
  contentTypeValidation: true,
  beautifyErrors: true
};
inputValidation.init('openapi/Petstore.oas3.yml', validationOptions);

const port = 8083;

const logRequestsController = new LogRequestsController();
const failController = new FailController();
const petstoreController = container.resolve(PetstoreController);

const app = express();

app.use(express.urlencoded({ extended: true }));
app.use(express.json());

morgan.token('all-headers', (req: Request, _res: Response) => { return JSON.stringify(req.headers); });
app.use(morgan(`:remote-addr - :remote-user [:date[clf]] ":method :url HTTP/:http-version" :status :res[content-length] ":referrer" ":user-agent" ":all-headers"`));


// Petstore Controller
app.get('/pets', inputValidation.validate, (req: Request, res: Response) => petstoreController.listPets(req, res));
app.post('/pets', inputValidation.validate, (req: Request, res: Response) => petstoreController.createPets(req, res));

// Test endpoints:

// Get requests
app.get('/requests/:objectType/:traceId', (req: Request, res: Response) => logRequestsController.getRequestsByTraceId(req, res));

// Fail on demand route
app.post('/requests/fail', (req: Request, res: Response) => failController.failNextRequest(req, res));

// Custom error handler
app.use((err: any, _req: Request, res: Response, _next: any) => {
  if (err instanceof inputValidation.InputValidationError) {
    return res.status(400).json({ more_info: JSON.stringify(err.errors) });
  }

  return res.status(err.status || 500).json({
    message: err.message || 'No additional information',
  });
});

http.createServer(app).listen(port);
console.log(`Listening on port ${port}`);

export default app;
