#!/usr/bin/env bash

cd "$(dirname "$0")"
cd ..

[ -f /tmp/webrtc/client ] && kill `cat /tmp/webrtc/client`
[ -f /tmp/webrtc/server ] && kill `cat /tmp/webrtc/server`
