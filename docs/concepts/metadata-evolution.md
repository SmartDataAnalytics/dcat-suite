# Metadata Evolution

Once content and *technical metadata* (e.g. CSV parsing information) of a dataset are published, that information should be immutable.
Technical metadata refers to all parameters needed to correctly parse a dataset (instance of a datamodel) from the given sequence of bytes.

However, it may be desireable to easily modify non-technical metadata, such as tags for discovery, abstracts, etc. without the need create full
releases.
The consequences are:
Firstly, a dataset's metadata may evolve independently of the published content.
Secondly, a dataset's metadata may be revised and published multiple times.


ISSUE Should we support publishing metadata in the form of catalogs as maven artifacts? What would be the consquence, because this would provide metadata for a set of artifacts - without the metadata being under the GAV.

g:a:v:t:c

File-centric model -> Dataset centric model



## Metadata as classifier
Content:
org.aksw.lsq:ls-kegg:v2:bz2

Metadata Version1
org.aksw.lsq:ls-kegg:v2:ttl:dcat-metadata[\_${version}]


Con:
- Custom classifier pattern which includes a version string - must be adhered to be implementors (publishers and consumers)
- Not sure if mvn central allows for deployment of new attachments

Pro:
- Content and metadata have same GAV



## Metadata as separate artifacts

Metadata artifacts are artifacts which represent a single(!) dataset and describe links to other datasets for different combinations of types and classifiers.
org.aksw.lsq:ls-kegg-dcat-metadata:v2:ttl

References to the content are resolved by first using the metadata's GAV and then selecting the appropriate distribution entry based on type and classifier.
I.e. resolution of type/classifier is not handled by maven anymore but wrapped by the DCAT client.


<#org.aksw.lsq:ls-kegg:v2:nt.bz2:content\_1>
  [] a Dataset; dcat:distribution [ type "nt.bz2"; classifier "content\_1"; mavenCoordinate "org.aksw.lsq:ls-kegg:v2:nt.bz2" ] .



org.aksw.lsq:ls-kegg:v2:nt.bz2



An artifact such as below could actually resolve to a text file with another maven identifier that contains the content of the distribution.
org.aksw.lsq:ls-kegg:v2:nt.bz2:distribution



Pro:
- Metadata can be versioned conventionally with the V component of the GAV.
- When publishing content for the first time, an appropriate dcat skeleton/model can be generated to refers to the content identifiers.
- The metadata artifact is the actual dataset description and its GAV is the datasat identifier (regardless of the identifiers of the content).
- By convention, metadata artifact IDs could follow the pattern `"${content artifactId}_dcat-metadata"`. The dcat-metadata part could be hidden by the DCAT client, so a reference to content artifactId checks for whether there is a \_dcat-metadata artifact.

Phycal maven layout:

```bash
org.aksw.lsq:ls-kegg:v2:nt.bz2 # content artifact
org.aksw.lsq:ls-kegg_dcat-metadata:v2_1:ttl
org.aksw.lsq:ls-kegg_dcat-metadata:v2_2:ttl
org.aksw.lsq:ls-kegg_dcat-metadata:v2_3:ttl
```

Virtual dcat layout

```bash
# org.aksw.lsq:ls-kegg:v2:nt.bz2 is not visible because it does not contain dcat-metadata
org.aksw.lsq:ls-kegg:v2_1:nt.bz2
org.aksw.lsq:ls-kegg:v2_2:nt.bz2
org.aksw.lsq:ls-kegg:v2_3:nt.bz2
```




Cons:
- Content artifacts are no longer self-describing; Either they contain no dcat-metadata at all - or only the initial metadata when they were uploaded.
- "Maven coordinates" need to be interpreted by the DCAT client. Maven wouldn't be able to resolve identifiers because type/classifier needs to be interpreted against the DCAT snippet and resolved to the approprite distribution.
- Metadata and Dataset identifiers are decoupled. An update of the metadata may refer to vastly different artifacts for the content.



org.aksw.lsq:ls-kegg:v2:ttl:dcat-metadata
org.aksw.lsq:ls-kegg:v2:nt.bz2:distribution








## 
```
dcat file publish
```






## How to reference published data

Conceptually, a dataset identifier first needs to be resolved against a DCAT catalog.
However, if a catalog system is (also) backed by a maven repository then it resolution can be delegated.

Lookup for a GAV then checks if a GAV:dcat-metadata:ttl artifact exists.



In principle, a dataset can be referred to by its GAV. The GAV acts as a URN which is turned into a (relative) HTTP url (resolved against a catalog), and all attached
artifacts are considered representations of the HTTP resource and are thus subject to content negotiation.



## Publishing Metadata to a Maven Repository


1. Add datasets and files to dcat.trig.
2. Add descriptions.
3. 
3. Create maven model
4. ??? Create updated DCAT model suitable for git publishing ???
4. Deploy with maven


## Publishing Metadata into a Git Repository



