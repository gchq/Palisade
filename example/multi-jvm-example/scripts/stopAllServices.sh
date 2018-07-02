#!/usr/bin/env bash

kill `ps -aef | grep :multi-jvm-example-rest-.*-service | grep -v grep | awk '{print $2}'`
