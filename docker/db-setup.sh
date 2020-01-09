#!/usr/bin/env bash

###############################################
# Set up database
###############################################

export PGHOST=localhost
cat << EOF | sudo -i -u postgres
createdb groundserver
psql -c "createuser --createdb --pwprompt --superuser --createrole postgres;"
/etc/init.d/postgresql reload
/etc/init.d/postgresql restart
EOF
