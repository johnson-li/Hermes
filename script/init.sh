#!/usr/bin/env bash

cd "$(dirname "$0")"
mkdir -p ../bin/webrtc

./update.sh

cd ../bin/webrtc
chmod +x peerconnection_server
chmod +x peerconnection_client_terminal
