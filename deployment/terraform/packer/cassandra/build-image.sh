#!/usr/bin/env bash

# todo add install step for packer

packer build -var-file=variables.json cassandra-ami.json