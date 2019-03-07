#!/usr/bin/env bash

ps -aef | grep example-rest-.*-service | grep -v grep | awk '{print substr($20,25) " port=" substr($22,19) " PID=" $2}'
