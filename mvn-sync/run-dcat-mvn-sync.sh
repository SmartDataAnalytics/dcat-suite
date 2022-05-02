#!/bin/bash

inotifywait /opt/archiva/repositories --recursive --monitor --format '%w%f' --event CLOSE_WRITE | \
  while read FILE; do
    if [[ "$FILE" =~ ^.*-dataset.*\.trig$ ]]; then
      echo "Processing change in file $FILE"
      ./sync-mvn.sh "$FILE"
    fi
  done

