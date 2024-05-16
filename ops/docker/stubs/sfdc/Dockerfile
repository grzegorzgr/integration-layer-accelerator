FROM kainoscom/node-swagger:10-alpine

COPY tmp/sfdc/ /sfdc

WORKDIR /sfdc

RUN npm install

CMD ["swagger", "project", "start"]
