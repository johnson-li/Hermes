#!/usr/bin/env bash

cd "$(dirname "$0")"
mkdir -p ~/Workspace/Hermes/bin/webrtc/
cp ~/Workspace/webrtc/src/out/Default/peerconnection_{client_terminal,server} ~/Workspace/Hermes/bin/webrtc/

