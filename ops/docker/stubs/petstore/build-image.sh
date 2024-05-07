#!/usr/bin/env bash
set -e

TAG=${1:-com.kainos/petstore-stub:0.0.1-SNAPSHOT}

mkdir -p ./tmp
cp -r ../../../../stubs/petstore ./tmp/
rm -rf ./tmp/dist

docker build --network host -t $TAG .

rm -rf ./tmp