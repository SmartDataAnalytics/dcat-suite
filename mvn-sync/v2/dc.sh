#!/bin/bash

# Wrapper for launching docker compose that sets the GROUP_ID and USER_ID variables
# for group id and user id.

export GROUP_ID=`id -g`
export USER_ID=`id -u`

echo "Running with user-id: $USER_ID and group-id: $GROUP_ID"

docker compose "$@"

