#!/usr/bin/env bash
set -e

pushd ../docker/services/api-gateway && ./build-image.sh
popd

docker-compose down
docker-compose up