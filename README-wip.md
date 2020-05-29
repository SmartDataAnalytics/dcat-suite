##

(work in progress)


## Service Management
The DCAT System supports creation of services and loading datasets.

Creation of services is typically based on a docker service wrapper.
The dcat command adds bookkeeping via `--managed` to track which datasets were loaded into which service.


The DCAT service management provides a simple abstraction for the most common service management and dataset loading tasks.
The purpose is NOT to reimplement a virtualization infrastructure such as docker.
However, it should simplify the process of bulk loading datasets from a catalog



Often, whenever one needs a good example for a dataset, one doesn't have one at hand.
Based on the catalog it is useful to have quick access to several properties

dcat serendipity

  format preference nt
  encoding preference gz
  dataset size estimate -e.g 1GB
  number of triples - e.g. 1M, .5B
  number of classes
  number of properties
  avg number of properties by resource
  use of multivalued properties
  




```
dcat service create -t myname -e 'KEY=VALUE' tenforce/virtuoso
dcat service up myname


## Dataset Loading
dcat service load -t myservice datasetid-or-file-or-url
dcat service load -t myservice --defer datasetid
dcat service load -t myservice --rm-deferred datasetid
dcat service load -t myservice --list-deferred
dcat service load -t myservice run

## Send a request to the service (e.g. a sparql statement)
dcat service request -t myservice 'query'


## Managed Dataset Loading
--managed registers the service as a distribution of the given dataset
dcat service load -t myservice --register-dist datasetid


## Service/Dataset Mapping
dcat servicemap

myname [ containsData [ graph "g1"; datasetid "foo" ] ]



## Service Life Cycle Management
dcat service down myname
dcat service restart myname
dcat service reload myname # config relead
dcat service delete myname
dcat service getenv myname
dcat service setenv -t myname KEY1=VALUE1 ... KEYn=VALUEn
dcat service unsetenv -t myname KEY1 KEYn


# Show the list of services and known datasets
dcat service list
```


File based services
  start a fuseki backed by a file








Quick serving
```
dcat docker serve --immutable dataset tenforce/virtuoso
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



