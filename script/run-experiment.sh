#!/usr/bin/env bash

# Roles:
#  - pi1: producer %% change pi1 to workstation-vm because we can not run webrtc in ARM
#  - pi2: client
#  - lab3: coordinator
#  - workstation: consumer

echo '====== clean dockers ======'
ssh workstation-vm 'cd /home/johnson/MEC; git pull; ./docker-clean.sh'
ssh pi2 'cd /home/pi/MEC; git pull; ./docker-clean.sh'
ssh workstation 'cd /home/lix16/MEC; git pull; ./docker-clean.sh'
ssh lab3 'cd /home/lix16/MEC; git pull; ./docker-clean.sh'

echo '====== create tmux sessions ======'
ssh workstation-vm 'tmux new -s main'
ssh pi2 'tmux new -s main'
ssh workstation 'tmux new -s main'
ssh lab3 'tmux new -s main'

echo '====== clear previous programme ======'
ssh workstation-vm 'tmux send-keys -t main:0 C-c'
ssh pi2 'tmux send-keys -t main:0 C-c'
ssh workstation 'tmux send-keys -t main:0 C-c'
ssh lab3 'tmux send-keys -t main:0 C-c'

echo '====== enter directory ======'
ssh workstation-vm 'tmux send-keys -t main:0 "cd /home/pi/MEC/consumer" Enter'
ssh pi2 'tmux send-keys -t main:0 "cd /home/pi/MEC/consumer" Enter'
ssh workstation 'tmux send-keys -t main:0 "cd /home/lix16/MEC/consumer" Enter'
ssh lab3 'tmux send-keys -t main:0 "cd /home/lix16/MEC/master" Enter'

echo '====== start programmes ======'
ssh lab3 'tmux send-keys -t main:0 "node app.js" Enter'
ssh workstation-vm 'tmux send-keys -t main:0 "node consumer.js" Enter'
ssh pi2 'tmux send-keys -t main:0 "node consumer.js" Enter'
ssh workstation 'tmux send-keys -t main:0 "node consumer.js" Enter'

echo '====== select the 2nd window ======'
ssh workstation-vm 'tmux select-window -t main:1 2> /dev/null || tmux new-window -t main:1'
ssh pi2 'tmux select-window -t main:1 2> /dev/null || tmux new-window -t main:1'
ssh workstation 'tmux select-window -t main:1 2> /dev/null || tmux new-window -t main:1'
ssh lab3 'tmux select-window -t main:1 2> /dev/null || tmux new-window -t main:1'

echo '====== start the coordinator ======'
ssh lab3 "docker pull johnson163/hermes; docker run -d -e 'roles=coordinator' -e 'host=195.148.127.246' -e 'services=HeartbeatService' --network host johnson163/hermes:latest"

echo '====== start the producer and the consumer ======'
ssh workstation-vm "docker pull johnson163/hermes-cuda; sudo docker run -d --privileged=true -e 'roles=producer' -e 'host=195.148.125.215' --network host johnson163/hermes-cuda:latest"
ssh workstation "docker pull johnson163/hermes-cuda; sudo docker run -d --privileged=true -e 'roles=consumer' -e 'host=195.148.125.212' --network host johnson163/hermes-cuda:latest"

echo '====== start the client ======'
ssh pi2 "docker pull johnson163/hermes-arm; docker run -d -e 'roles=client' -e 'host=195.148.125.214' --network host johnson163/hermes-arm:latest"
