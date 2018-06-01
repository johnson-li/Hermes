#!/usr/bin/env bash

mkdir -p ~/Workspace/Hermes/bin/webrtc/
cd ~/Workspace/Hermes/

./gradlew jar
./script/init.sh

cp ~/Workspace/webrtc/src/out/Default/peerconnection_{client_terminal,server} ~/Workspace/Hermes/bin/webrtc/

#docker build -t johnson163/hermes .
docker build -f Dockerfile-arm -t johnson163/hermes-arm .
docker build -f Dockerfile-build -t johnson163/hermes-cuda .

#docker push johnson163/hermes
docker push johnson163/hermes-arm
docker push johnson163/hermes-cuda
