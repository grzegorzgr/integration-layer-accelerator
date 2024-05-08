#!/usr/bin/env bash
set -ex

docker exec -it local_broker_1 kafka-topics --delete --zookeeper local_zookeeper_1 --topic "^[^_]+"