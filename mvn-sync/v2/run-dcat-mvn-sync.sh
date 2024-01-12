#!/bin/bash

WATCH_DIR="$1"

if [ -z "$WATCH_DIR" ]; then
  echo "No path to watch specified"
  exit 1
fi

# /opt/archiva/repositories

inotifywait "$WATCH_DIR" --recursive --monitor --format '%w%f' --event CLOSE_WRITE | \
  while read FILE; do
    # Match files with a '-dcat.' in their name
    if [[ "$FILE" =~ ^.*-dcat\..*\.*$ ]]; then
      echo "Processing change in file: $FILE"
      ./sync-mvn.sh "$FILE"
      echo "Completed processing file: $FILE"
    fi
  done

