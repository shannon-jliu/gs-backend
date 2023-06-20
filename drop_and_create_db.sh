psql -U postgres -c "DROP DATABASE groundserver  with (FORCE)"
psql -U postgres -c "CREATE DATABASE groundserver WITH ENCODING='UTF8' OWNER=postgres CONNECTION LIMIT=-1;"
