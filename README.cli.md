
## Dcat Suite Command Line Tool


### Local DCAT Repository Management

Tthe dcat cli enables managing a folder as a dcat repository similar as git does.


#### Quick Example
```
# Initialize a dcat repository in the current folder
dcat file init

Add a file as a dataset. The group id must be provided.
```bash
dcat file add -g org.example mydata.ttl

Created dataset 'org.example:mydata:2022-01-01'.
```

The base file name becomes the artifact id and the timestamp the version.


```bash
# Display current status
cat dcat.trig
```






