# Petstore stub

# Supported API endpoints

- `GET /pets` - listPets
- `POST /pets` - createPets


# Debug endpoints

In order to get saved requests from the stub, you can call following endpoint:

- `GET /requests/:objectType/:traceId`

where `objectType`:

- `listPets`
- `createPets`

# Fail on demand endpoint

In order to fail request on demand, call:

- POST `/requests/fail` with request body:

```json
{
  "traceId": "trace id of the call that should fail",
  "requestName": "name of the operation that should fail",
  "statusCode": 500
}
```

IN PETSTORE STUB, FAIL REQUEST (PER TRACE-ID) WORKS ONLY ONCE, NEXT REQUEST WITH SAME TRACE ID WILL SUCCEED
