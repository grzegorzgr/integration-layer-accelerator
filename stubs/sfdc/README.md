# SFDC stub

Node version: `v10.24.1`

## In order to get new access token, call:

- POST `/services/oauth2/token` with `application/x-www-form-urlencoded` parameters:

```
grant_type:password
username:hello
password:hello
client_id:hello
client_secret:hello
```

Use `access_token` value received in response as a Bearer token for each sfdc request. The Authentication header of any request must be set to `Bearer:c3R1Yl9zZmRjX3Rva2Vu`.

`c3R1Yl9zZmRjX3Rva2Vu` is Base64 for `sfdc_stub_token`.

## In order to fail request on demand

In order to fail request on demand, call:

- POST `/requests/fail` - will fail only ONE time for given `trace-id`
- POST `/requests/fail-continuously` - will fail continuously for given `trace-id`

with request body:

```json
{
    "traceId": "trace id of the call that should fail",
    "requestName": "name of the operation that should fail",
    "statusCode": 500
}
```

Possible call types:

- `createNewAccount`

## In order to get requests, call one of the endpoints:

- GET `/requests/generic/:objectType/:traceId` with given traceId, where object type can be one of the above list.

## Dev notes
```operationId: xxx``` triggers method of the same name ```xxx```.
I.e.
```
paths:
  /composite/tree/ccDelivery__c:
    x-swagger-router-controller: createNewAccount
    post:
      operationId: createNewAccount
```
triggers:
```function sendDelivery(req, res)```

Stoplight docs: https://concardis.stoplight.io/docs/sfdc/branches/3.6/4b402d5ab3edd-welcome
