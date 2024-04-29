import { Request, Response } from 'express';
import { autoInjectable } from 'tsyringe';

import LogRequestsController from './LogRequestsController';
import FailController from './FailController';
import { HttpHeader } from '../models/HttpHeaders';

@autoInjectable()
export default class PetstoreController {
  static PETS: any = {};

  constructor(
    private logRequestsController: LogRequestsController,
    private failController: FailController,
  ) { }

  createPets(req: Request, res: Response) {
    this.failController.shouldFailOnDemand(
      req,
      res,
      'createPets',
      (req: Request, res: Response) => this.createPetsSuccess(req, res)
    );
  }

  listPets(req: Request, res: Response) {
    this.failController.shouldFailOnDemand(
        req,
        res,
        'listPets',
        (req: Request, res: Response) => this.listPetsSuccess(req, res)
    );
  }

  private createPetsSuccess(req: Request, res: Response) {
    const traceId: string | undefined = req.header(HttpHeader.TRACE_ID);
    console.log(`Received createPets request for TRACE-ID: ${traceId}`);
    this.logRequestsController.logNewRequest('createPets', req);
    if (traceId == undefined) {
      return res.status(201).json();
    }
    if (!PetstoreController.PETS[traceId]) {
      PetstoreController.PETS[traceId] = [];
    }
    PetstoreController.PETS[traceId].push(req.body);
    return res.status(201).json();
  }

  private listPetsSuccess(req: Request, res: Response) {
    const traceId: string | undefined = req.header(HttpHeader.TRACE_ID);
    console.log(`Received getPets request for TRACE-ID: ${traceId}`);
    this.logRequestsController.logNewRequest('listPets', req);
    if (traceId != undefined && PetstoreController.PETS[traceId]) {
      return res.status(200).json(PetstoreController.PETS[traceId]);
    }
    return res.status(201).json([]);
  }
};
