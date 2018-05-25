#!/usr/bin/env bash

ssh pi1 '/home/pi/MEC/docker-clean.sh'
ssh pi2 '/home/pi/MEC/docker-clean.sh'
ssh workstation '/home/lix16/MEC/docker-clean.sh'

# crease tmux sessions
ssh pi1 'tmux new -s main'
ssh pi2 'tmux new -s main'
ssh workstation 'tmux new -s main'
ssh lab3 'tmux new -s main'

# clear previous programme
ssh pi1 'tmux send-keys -t0 C-c'
ssh pi2 'tmux send-keys -t0 C-c'
ssh workstation 'tmux send-keys -t0 C-c'
ssh lab3 'tmux send-keys -t0 C-c'

# enter directory
ssh pi1 'tmux send-keys "cd /home/pi/MEC/consumer" Enter'
ssh pi2 'tmux send-keys "cd /home/pi/MEC/consumer" Enter'
ssh workstation 'tmux send-keys "cd /home/lix16/MEC/consumer" Enter'
ssh lab3 'tmux send-keys "cd /home/lix16/MEC/master" Enter'

# start programmes
ssh lab3 'tmux send-keys "node app.js" Enter'
ssh pi1 'tmux send-keys "node consumer.js" Enter'
ssh pi2 'tmux send-keys "node consumer.js" Enter'
ssh workstation 'tmux send-keys "node consumer.js" Enter'
