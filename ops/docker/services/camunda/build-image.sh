#!/usr/bin/env bash
set -e

TAG=${1:-com.kainos/camunda:0.0.1-SNAPSHOT}

if [ "$2" == "--skipBuild" ]; then
  echo "Flag is set to not trigger building jar again!"
else
  echo "Building Jar by default"
  pushd ../../../../services && ./gradlew --build-cache camunda:bootJar && popd
fi

cp ../../../../services/camunda/build/libs/camunda.jar camunda.jar

docker build --network host -t $TAG .

rm camunda.jar