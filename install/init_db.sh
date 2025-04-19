#!/bin/bash

psql -U postgres -c "CREATE USER pawa_admin WITH PASSWORD '123';"
psql -U postgres -c "CREATE DATABASE pawa_be_db OWNER pawa_admin;"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE pawa_be_db TO pawa_admin;"
psql -U postgres -c "GRANT ALL ON SCHEMA public TO pawa_admin;"
