#!/bin/bash
docker run -d --name ecopostgres \
  -e POSTGRES_DB=ecofarm \
  -e POSTGRES_USER=ecofarm \
  -e POSTGRES_PASSWORD=ecofarm123 \
  -p 5432:5432 \
  postgres:13
