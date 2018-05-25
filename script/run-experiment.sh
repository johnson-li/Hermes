#!/usr/bin/env bash

ssh pi1 '/home/pi/MEC/docker-clean.sh'
ssh pi2 '/home/pi/MEC/docker-clean.sh'
ssh workstation '/home/lix16/MEC/docker-clean.sh'

# crease tmux sessions
ssh pi1 'tmux new -s main'
ssh pi2 'tmux new -s main'
ssh workstation 'tmux new -s main'
ssh lab3 'tmux new -s main'

# select the 1st window
ssh pi1 'tmux select-window -t 0'
ssh pi2 'tmux select-window -t 0'
ssh workstation 'tmux select-window -t 0'
ssh lab3 'tmux select-window -t 0'

# clear previous programme
ssh pi1 'tmux send-keys -t0 C-c'
ssh pi2 'tmux send-keys -t0 C-c'
ssh workstation 'tmux send-keys -t0 C-c'
ssh lab3 'tmux send-keys -t0 C-c'

# enter directory
ssh pi1 'tmux send-keys -t0 "cd /home/pi/MEC/consumer" Enter'
ssh pi2 'tmux send-keys -t0 "cd /home/pi/MEC/consumer" Enter'
ssh workstation 'tmux send-keys -t0 "cd /home/lix16/MEC/consumer" Enter'
ssh lab3 'tmux send-keys -t0 "cd /home/lix16/MEC/master" Enter'

# start programmes
ssh lab3 'tmux send-keys -t0 "node app.js" Enter'
ssh pi1 'tmux send-keys -t0 "node consumer.js" Enter'
ssh pi2 'tmux send-keys -t0 "node consumer.js" Enter'
ssh workstation 'tmux send-keys -t0 "node consumer.js" Enter'

# select the 2nd window
ssh pi1 'tmux select-window -t 1 2> /dev/null || tmux new-window'
ssh pi2 'tmux select-window -t 1 2> /dev/null || tmux new-window'
ssh workstation 'tmux select-window -t 1 2> /dev/null || tmux new-window'
ssh lab3 'tmux select-window -t 1 2> /dev/null || tmux new-window'

# start the coordinator
ssh lab3 '/home/lix16/init.sh'

# start the producer and the consumer
ssh pi1 '/home/pi/init.sh'
ssh workstation '/home/lix16/init.sh'

# start the client
ssh pi2 '/home/pi/init.sh'
