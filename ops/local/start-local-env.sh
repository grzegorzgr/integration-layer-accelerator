#!/usr/bin/env bash
set -e

pushd ../docker/services/api-gateway && ./build-image.sh
popd

pushd ../docker/services/demo && ./build-image.sh
popd

pushd ../docker/services/error-handling && ./build-image.sh
popd

pushd ../docker/stubs/petstore && ./build-image.sh
popd

docker-compose down
docker-compose up -d