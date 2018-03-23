#!/usr/bin/env bash

cd "$(dirname "$0")"
mkdir -p ../bin/webrtc
cd ../bin/webrtc
wget 'https://drive.google.com/uc?id=1-iQsqhJhPw2QKi-h6OOCkWeGNuo_dvuI&export=download' -O peerconnection_server
wget 'https://drive.google.com/uc?id=1Hx0RG9656MCwmQoDGD691XWEzJMcix9H&export=download' -O peerconnection_client
wget 'https://drive.google.com/uc?id=1qvIOhkD8C5rbopNHnvzSzYCz8Lk8cP2C&export=download' -O peerconnection_client_terminal
chmod +x peerconnection_server
chmod +x peerconnection_client
chmod +x peerconnection_client_terminal
