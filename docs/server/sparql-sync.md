---
title: SPARQL Endpoint Synchronization
nav_order: 10
---

## SPARQL Endpoint Synchronization


### File System triggers

The source code of the scripts for performing synchroniztion are located in the [mvn-sync](mvn-sync) folder.


Using `inotify-wait` it is possible to trigger actions when the state of files change, such as when they have been closed after writing.

* Ensure that a sufficient number of inotify watchs can be created. For large repositories, open `/etc/sysctl.conf` and adapt the following setting to your needs:

```
fs.inotify.max_user_watches=81920
```

Run `sudo sysctl -p` to make the changes take effect.

* Install required tools. File system watches are provided by `inotify-tools`. For XML processing for maven repository metadata `xmlstarlet` is needed.

```bash
sudo apt install inotify-tools xmlstarlet
```

* Run a SPARQL endpoint

In principle you can run a SPARQL endpoint of your choice. The following docker command starts a SPARQL endpoint on host port 1234
backed by a persistent database using the Apache Jena TDB2 engine.
This docker image also provides our SPARQL extension functions.

```bash
docker run -p '1234:8642' -v "$PWD/data:/data" -it aklakan/rdf-processing-toolkit integrate --server -e tdb2 --loc /data --db-keep
```

* Configure the sync script
Change e.g. `run-dcat-mvn-sync.sh`

* Start the sync script.


```bash
./run-dcat-mvn-sync.sh
```


