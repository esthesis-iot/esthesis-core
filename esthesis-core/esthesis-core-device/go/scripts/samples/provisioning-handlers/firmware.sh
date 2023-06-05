#!/bin/sh

LOG="Provisioning request - $(date)\nExisting version: $1\nNew version: $2\nFile hash: $3\nFile size: $4\nFile: $5\n\n"
echo "$LOG" >> /tmp/firmware.log

/bin/sh "$5"
