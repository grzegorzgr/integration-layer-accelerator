#!/usr/bin/env bash
set -e

TAG=${1:-com.kainos/error-handling:0.0.1-SNAPSHOT}

if [ "$2" == "--skipBuild" ]; then
  echo "Flag is set to not trigger building jar again!"
else
  echo "Building Jar by default"
  pushd ../../../../services && ./gradlew --build-cache error-handling:bootJar && popd
fi

cp ../../../../services/error-handling/build/libs/error-handling.jar error-handling.jar

docker build --network host -t $TAG .

rm error-handling.jar