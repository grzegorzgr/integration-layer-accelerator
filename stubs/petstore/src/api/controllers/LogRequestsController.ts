import { Request, Response } from 'express';

import { HttpHeader } from '../models/HttpHeaders';

export default class LogRequestsController {
  static REQUESTS: any = {};

  getRequestsByTraceId(req: Request, res: Response) {
    const objectType: string = req.params.objectType;
    const traceId: string = req.params.traceId;

    const key: string = this.getRequestLabel(objectType, traceId);

    if (LogRequestsController.REQUESTS[key]) {
      return res.json(LogRequestsController.REQUESTS[key]);
    }

    return res.status(404).send('No requests for given objectType + traceId');
  }

  logNewRequest(objectType: string, req: Request) {
    const traceId: string | undefined = req.header(HttpHeader.TRACE_ID);
    const body: any = req.body;

    const key: string = this.getRequestLabel(objectType, traceId);

    if (!LogRequestsController.REQUESTS[key]) {
      LogRequestsController.REQUESTS[key] = [];
    }

    LogRequestsController.REQUESTS[key].push(body);
    console.log('Logged new request with trace-id: ', traceId, body);
  }

  private getRequestLabel(objectType: string, traceId: string | undefined) {
    return objectType + "/" + traceId;
  }
}
