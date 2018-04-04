#!/usr/bin/env bash

cd "$(dirname "$0")"
cd ..
mkdir -p /tmp/webrtc
./bin/webrtc/peerconnection_server &
pid="$!"
echo ${pid} > /tmp/webrtc/server.pid
