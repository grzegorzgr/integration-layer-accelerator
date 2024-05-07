FROM kainoscom/node:10-alpine-swagger as build-dev

WORKDIR /petstore
COPY /tmp/petstore/package.json .
RUN npm install
COPY /tmp/petstore .
RUN npm run build

FROM kainoscom/node:10-alpine-swagger

WORKDIR /petstore
COPY /tmp/petstore/package.json .
RUN npm install --only=production
COPY --from=build-dev /petstore/dist ./dist
COPY --from=build-dev /petstore/openapi ./openapi

CMD ["node", "dist/index.js"]
