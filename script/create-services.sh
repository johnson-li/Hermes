#!/usr/bin/env bash

ssh workstation "docker run -d --privileged=true -e 'functionality=service' -e 'services=WebrtcReceiverService' -e 'job_id=12345' --network host johnson163/hermes-cuda"
ssh pi2 "docker run -d -e 'functionality=service' -e 'services=WebrtcClientService' -e 'job_id=12345' --network host johnson163/hermes-arm"
ssh workstation-vm "docker run -d -e 'functionality=service' -e 'services=WebrtcSenderService' -e 'job_id=12345' --network host johnson163/hermes"
