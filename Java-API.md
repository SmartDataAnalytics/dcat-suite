## DCAT API Java API

### Concepts

#### The Repository

```java
public interface DcatRepository {

    /**
     * 
     * @param dcatDistribution An RDF description of a dcat distribution
     * @param iriResolver A resolver for relative IRIs in case the downloadURLs of the distribution are relative
     * @return
     * @throws Exception
     */
    Collection<URI> resolveDistribution(Resource dcatDistribution, Function<String, String> iriResolver) throws Exception;
}

```

* The `DcatRepository` interface provides the means to retrieve metadata and content related to datasets and distributions.
* Presently, only Distributions are implemented: Given a (DCAT) RDF description of a distribution, the repository can be used to resolve
such a distribution to a collection of URIs. The default implementation will download and cache all referenced files in the local repository, and return the collection of `file://` URLs pointing to these files.
Alternate implementations may simply return locations to files on the Web.
* Distribution URIs are assumed to be unique. If the URI of a distribution is available in the repository, the repository will used and the provided RDF description is ignored, even if they differs. (TODO it may be bettter to issue a warning or even an error).



#### Deployment: CKAN


#### Deployment: Virtuoso


#### Expanding nquads
Nquad datasets may comprise DCAT descriptions as well as the actual distribution data using different graphs.
The DCAT suite supports "expanding" such graphs that correspond to distributions into seperate files, and yielding an updated DCAT description which references the expanded files.





