#!/bin/bash

baseFolder=${1:-~/.dcat}

# Remove all broken links in the given subtree
find "$baseFolder" -xtype l -delete

# Recursively remove all empty directories
find "$baseFolder" -type d -empty -delete

