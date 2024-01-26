# Auto-DCAT Metadata Generator

The docker compose setup in this repository is designed to watch for changes to the files in a maven repository in order to automatically trigger
- generatation of DCAT metadata artifacts
- loading of DCAT metadata (via optional transformation rules) into an RDF store (the data catalog)


## Resources

* `dc.sh`: Wrapper for `docker compose` that sets user-id and group-id to the current host user. Prefer this command over barebone `docker compose`.


## Usage

Adjust the volumes in the docker-compose.yml file to your needs. The setup in the container watches the directory `/repository` for changes.

```
./dc.sh up --build
./dc.sh down
```


## Trouble Shooting

* The following error can be mitigated by running the command below:
```
Failed to watch /home/raven/.m2/repository; upper limit on inotify watches reached!
Please increase the amount of inotify watches allowed per user via `/proc/sys/fs/inotify/max_user_watches'.
```

```bash
echo "1000000" | sudo tee /proc/sys/fs/inotify/max_user_watches
```
