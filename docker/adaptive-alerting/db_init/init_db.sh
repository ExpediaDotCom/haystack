#!/usr/bin/env sh

mysql -u root -proot < /home/build-db.sql
mysql -u root -proot < /home/stored-procs.sql
mysql -u root -proot < /home/sample-data.sql
