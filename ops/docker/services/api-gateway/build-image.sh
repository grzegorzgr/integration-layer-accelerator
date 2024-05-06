#!/usr/bin/env bash
set -e

TAG=${1:-com.kainos/api-gateway:0.0.1-SNAPSHOT}

if [ "$2" == "--skipBuild" ]; then
  echo "Flag is set to not trigger building jar again!"
else
  echo "Building Jar by default"
  pushd ../../../../services && ./gradlew --build-cache api-gateway:bootJar && popd
fi

cp ../../../../services/api-gateway/build/libs/api-gateway.jar api-gateway.jar

docker build --network host -t $TAG .

rm api-gateway.jar