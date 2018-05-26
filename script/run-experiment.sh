#!/usr/bin/env bash

# Roles:
#  - pi1: producer
#  - pi2: client
#  - lab3: coordinator
#  - workstation: consumer

echo '====== clean dockers ======'
ssh pi1 'cd /home/pi/MEC; git pull; ./docker-clean.sh'
ssh pi2 'cd /home/pi/MEC; git pull; ./docker-clean.sh'
ssh workstation 'cd /home/lix16/MEC; git pull; ./docker-clean.sh'
ssh lab3 'cd /home/lix16/MEC; git pull; ./docker-clean.sh'

echo '===== create networks ====='
ssh pi1 'docker network create --subnet=10.20.30.0/24 hermes 2> /dev/null'
ssh pi2 'docker network create --subnet=10.20.30.0/24 hermes 2> /dev/null'
ssh workstation 'docker network create --subnet=10.20.30.0/24 hermes 2> /dev/null'
ssh lab3 'docker network create --subnet=10.20.30.0/24 hermes 2> /dev/null'

echo '====== create tmux sessions ======'
ssh pi1 'tmux new -s main 2> /dev/null'
ssh pi2 'tmux new -s main 2> /dev/null'
ssh workstation 'tmux new -s main 2> /dev/null'
ssh lab3 'tmux new -s main 2> /dev/null'

echo '====== clear previous programme ======'
ssh pi1 'tmux send-keys -t main:0 C-c'
ssh pi2 'tmux send-keys -t main:0 C-c'
ssh workstation 'tmux send-keys -t main:0 C-c'
ssh lab3 'tmux send-keys -t main:0 C-c'

echo '====== enter directory ======'
ssh pi1 'tmux send-keys -t main:0 "cd /home/pi/MEC/consumer" Enter'
ssh pi2 'tmux send-keys -t main:0 "cd /home/pi/MEC/consumer" Enter'
ssh workstation 'tmux send-keys -t main:0 "cd /home/lix16/MEC/consumer" Enter'
ssh lab3 'tmux send-keys -t main:0 "cd /home/lix16/MEC/master" Enter'

echo '====== start programmes ======'
ssh lab3 'tmux send-keys -t main:0 "node app.js" Enter'
ssh pi1 'tmux send-keys -t main:0 "node consumer.js" Enter'
ssh pi2 'tmux send-keys -t main:0 "node consumer.js" Enter'
ssh workstation 'tmux send-keys -t main:0 "node consumer.js" Enter'

echo '====== select the 2nd window ======'
ssh pi1 'tmux select-window -t main:1 2> /dev/null || tmux new-window -t main:1'
ssh pi2 'tmux select-window -t main:1 2> /dev/null || tmux new-window -t main:1'
ssh workstation 'tmux select-window -t main:1 2> /dev/null || tmux new-window -t main:1'
ssh lab3 'tmux select-window -t main:1 2> /dev/null || tmux new-window -t main:1'

echo '====== start the coordinator ======'
ssh lab3 '/home/lix16/init.sh'

echo '====== start the producer and the consumer ======'
ssh pi1 '/home/pi/init.sh'
ssh workstation '/home/lix16/init.sh'

sleep 20
echo '====== start the client ======'
ssh pi2 '/home/pi/init.sh'
