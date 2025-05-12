#!/bin/sh

sleep 5

mc alias set local http://minio:9000 minioadmin minioadmin
mc mb local/passengerids || echo "Bucket already exists"
