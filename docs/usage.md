
# Maven Identifiers

Maven identifiers take the following form:
groupId:artifactId:version[:type[:classifier]]

In order to turn maven identifiers into _uniform resource names_ (URNs) the prefix `urn:mvn:` is prepended.


There are three major types of entities: datasets, distributions and download links.


## Content and metadata

A DCAT Suite project is comprised of two components:
* the *file mapping* aka *content mapping* which is used to assign maven IDs to files
* the *metadata mapping* which aggregates content into logical dataset

Content and metadata are separate artifacts and can thus be versioned indepedently. For example, a large CSV file may be deployed once with maven,
but the metadata may be revised and redeployed many times thus enabling the evolution of a rich metadata model over the original content.

In fact, a major point in separating metadata and content is that while the former can be managed in a git repository, the latter should be stored elsewhere
from where it can be recovered on demand.
Consequently, once the content is deployed, one often may want to delete the local copy (optionally caching it in the local maven repository) - i.e. once the large files are safely stored in the remote (maven) repository, there is no need to keep it in the local git repository.

The metadata mapping however should be versioned using e.g. a git repository such that a history of all future changes is tracked and appropriate versions can be published as releases in the remote maven repository.

```
content.dcat-build.trig
metadata.dcat-build.trig
```


# Basic Commands

* Initialize a local dcat repository
```bash
dcat init
```

* Set a default group for the local repository
dcat set groupId=org.example.mydataset



* Create a dataset and distribution from a file
```bash
dcat add file.nt.bz2
```


    * It also also possible to create dataset, distribution and the link between the two separately:
```bash
dcat add --dataset file.nt.bz2
```

```bash
dcat add --dataset 'org.example.mydata:mydataset` file.nt.bz2 # Derive version from file's last modified date
dcat add --dist file.nt.bz2
dcat add --dataset 'some:dataset:id' --distribution='some:dist:id'

```


## Removing entries


```bash
dcat rm file.nt.bz2
```


If `--orphaned` is specified then a distribution without a download link is removed and subsequently a dataset without distributions is removed.
```
dcat rm --orphaned
```


## Updating references
Altering an entity's maven identifier is not recommended because certain operations (e.g. computation of void descriptions) may have been
computed against the prior identifier.

An experimental command is
```
dcat relabel
```




