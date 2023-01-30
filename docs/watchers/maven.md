# Watching Maven Repositories for Changes

A maven repository itself cannot be queried with SPARQL directly. However, we can easily scan a repository folder recursively
for all existing DCAT files and index them in a triple store.
Using a recursive directory watch we can also capture all future changes.

## Inotify

```bash
sudo apt-get install inotify-tools
```

* Set up a watch for files that are closed after being written to
```bash
inotifywait ~/.m2/repository --recursive --monitor --event CLOSE_WRITE
```


### Setting up a systemd service
TODO

