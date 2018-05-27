#!/usr/bin/env bash

cd ~/Workspace/hermes/

./gradlew jar
./script/init.sh

docker build -t johnson163/hermes .
docker build -f Dockerfile-arm -t johnson163/hermes-arm .

docker push johnson163/hermes
docker push johnson163/hermes-arm
