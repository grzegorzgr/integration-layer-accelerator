import { Request, Response } from 'express';

import { HttpHeader } from '../models/HttpHeaders';

export default class FailController {
  static FAILURES: any = {};

  failNextRequest(req: Request, res: Response) {
    const traceId: string = req.body.traceId;
    const requestName: string = req.body.requestName;
    const statusCode: number = req.body.statusCode;

    if (!statusCode || !traceId || !requestName) {
      return res.status(400).json('bad request');
    }

    console.log(`Next request will fail with ${statusCode} ${requestName} ${traceId}`);
    this.saveCallToFail(requestName, traceId, statusCode);
    return res.status(200).json();
  }

  shouldFailOnDemand(req: Request, res: Response, requestName: string, successFunction: any) {
    const traceId: string | undefined = req.get(HttpHeader.TRACE_ID);

    const responseStatusCode = this.getFailureResponseStatusCode(requestName, traceId);

    if (responseStatusCode) {
      const errorResponse = {
        code: responseStatusCode,
        message: 'Error from Petshop stub',
        detail: 'Petshop stub returned this code on demand.',
      };

      console.log(`Returning fail response with status code ${responseStatusCode} on demand for: ${requestName} and TRACE-ID: ${traceId}`);

      return res.status(responseStatusCode).json(errorResponse);
    }

    successFunction(req, res);
  }

  private saveCallToFail(callType: string, traceId: string | undefined, statusCode: number) {
    const failureLabel: string = this.getKeyForFailureMap(callType, traceId);
    FailController.FAILURES[failureLabel] = statusCode;
  }

  private getFailureResponseStatusCode(callType: string, traceId: string | undefined) {
    const failureLabel: string = this.getKeyForFailureMap(callType, traceId);
    const responseStatusCode = FailController.FAILURES[failureLabel];
    if (responseStatusCode) {
      delete FailController.FAILURES[failureLabel];
    }
    return responseStatusCode;
  }

  private getKeyForFailureMap(callType: string, traceId: string | undefined) {
    return `${callType}/${traceId}`;
  }
};
