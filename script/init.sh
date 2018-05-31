#!/usr/bin/env bash

cd "$(dirname "$0")"
mkdir -p ../bin/webrtc

./update.sh

cd ../bin/webrtc
chmod +x peerconnection_server
chmod +x peerconnection_client_terminal

ln -s /usr/lib/x86_64-linux-gnu/libswscale.so.3 /usr/lib/x86_64-linux-gnu/libswscale-ffmpeg.so.3
ln -s /usr/lib/x86_64-linux-gnu/libavutil.so.54 /usr/lib/x86_64-linux-gnu/libavutil-ffmpeg.so.54
