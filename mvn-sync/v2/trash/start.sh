#!/bin/bash

# Do not use; right now it seems baking the uid:gid into the image using build args in the most feasibile approach

echoerr() { echo "$@" 1>&2; }

SCRIPT_FILE="$(realpath "${BASH_SOURCE:-$0}")"
SCRIPT_DIR="$(dirname "$SCRIPT_FILE")"

echoerr "Attempting to switch from $USER with uid $UID to $USER_ID:$GROUP_ID"

groupadd -g "$GROUP_ID" user
useradd -m -u "$USER_ID" -g "$GROUP_ID" -s /bin/bash user
su user

echoerr "Currend directory is: $(pwd)"
mkdir -p ~/mvn-sync
cd /home/user/mvn-sync

"$SCRIPT_DIR"/run-dcat-mvn-sync.sh

