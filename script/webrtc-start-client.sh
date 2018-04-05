#!/usr/bin/env bash

cd "$(dirname "$0")"
cd ..
mkdir -p /tmp/webrtc
./bin/webrtc/peerconnection_client_termianl --name ${name} --peer ${peer} --sendonly ${snedonly} --autocall ${autocall} ${coordinator_ip} &
pid="$!"
echo ${pid} > /tmp/webrtc/client.pid
