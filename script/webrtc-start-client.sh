#!/usr/bin/env bash

cd "$(dirname "$0")"
cd ..
mkdir -p /tmp/webrtc
./bin/webrtc/peerconnection_client_termianl --name ${name} --sendonly ${sned_only} --autocall ${auto_call} ${coordinator_ip} &
pid="$!"
echo ${pid} > /tmp/webrtc/client.pid
