##

(work in progress)


## Service Management
The DCAT System supports creation of services and loading datasets.

Creation of services is typically based on a docker service wrapper.
The dcat command adds bookkeeping to track which datasets were loaded into which services.


```
dcat service create -t myname service-image
dcat service up myname
dcat service load -t myservice datasetid
dcat service down myname

# Show the list of services and known datasets
dcat service list
```






## The local DCAT repository
It serves two purposes

* An *index* of dataset and distribution records as well as downloads. The folder-based index structure is aimed at making the repo easy to maintain in cotrast to a database and allow for efficient lookups

It is possible to either index whole catalogs at once in advance,
or to index datasets once they are requested.



If the local repository cannot serve the request, the request is delegated to the known catalogs in the order they were registered.



## Catalog resolvers


### Default Directory-based Catalog Resolver



### Url-based catalogs




### Git-based catalogs

A common use case is for a git repository to host the catalog - in one or more files.
A repository can contain a single rdf file describing describing the set of files that make up the complete catalog with the use of filename patterns.


Interplay between git and the file-index:
If pulling a git repo reveals no changes, then there is no need to update the index.





https://stackoverflow.com/questions/2993902/how-do-i-check-the-date-and-time-of-the-latest-git-pull-that-was-executed




## Catalog management

* Index local or remote dcat entries in the local repository
```
dcat catalog index --no-overwrite file.*.ttl
dcat catalog index http://remote.file/url
```



