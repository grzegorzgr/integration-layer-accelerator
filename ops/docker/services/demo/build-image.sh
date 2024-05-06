#!/usr/bin/env bash
set -e

TAG=${1:-com.kainos/demo:0.0.1-SNAPSHOT}

if [ "$2" == "--skipBuild" ]; then
  echo "Flag is set to not trigger building jar again!"
else
  echo "Building Jar by default"
  pushd ../../../../services && ./gradlew --build-cache demo:bootJar && popd
fi

cp ../../../../services/demo/build/libs/demo.jar demo.jar

docker build --network host -t $TAG .

rm demo.jar