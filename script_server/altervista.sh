#!/bin/bash

HOST=ftp.c1p81.altervista.org
USER=c1p81
PASS=fircudinci54

ftp -inv $HOST << EOF
user $USER $PASS
cd RunnerTracker
put posizioni.txt
bye
EOF
