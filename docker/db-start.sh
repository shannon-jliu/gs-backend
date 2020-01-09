#!/usr/bin/env bash

###############################################
# Start database
###############################################

cat << EOF |
pg_ctl reload
pg_ctl restart
EOF
