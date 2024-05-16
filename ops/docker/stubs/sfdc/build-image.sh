#!/usr/bin/env bash
set -e

TAG=${1:-com.kainos/sfdc-stub:0.0.1-SNAPSHOT}

rm -rf ./tmp
mkdir -p ./tmp
cp -r ../../../../stubs/sfdc ./tmp/

docker build --network host -t $TAG .

rm -rf ./tmp

