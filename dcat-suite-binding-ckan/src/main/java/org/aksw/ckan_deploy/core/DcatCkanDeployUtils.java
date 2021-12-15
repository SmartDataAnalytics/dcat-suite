package org.aksw.ckan_deploy.core;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.ckan_deploy.dcat.CkanDatasetUtils;
import org.aksw.commons.io.util.UriUtils;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.jena.domain.api.MavenEntity;
import org.aksw.dcat.utils.DcatUtils;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFile;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl;
import org.apache.http.message.BasicHttpRequest;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.irix.IRIx;
import org.apache.jena.irix.IRIxResolver;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.SplitIRI;
import org.apache.jena.vocabulary.DCAT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Streams;

import eu.trentorise.opendata.jackan.CkanClient;
import eu.trentorise.opendata.jackan.exceptions.CkanException;
import eu.trentorise.opendata.jackan.exceptions.CkanNotFoundException;
import eu.trentorise.opendata.jackan.internal.org.apache.http.entity.ContentType;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanOrganization;
import eu.trentorise.opendata.jackan.model.CkanResource;
import eu.trentorise.opendata.jackan.model.CkanTag;

public class DcatCkanDeployUtils {

    private static final Logger logger = LoggerFactory.getLogger(DcatCkanDeployUtils.class);

    public static Function<String, List<String>> stringSplitter(String pattern) {
        return str -> Arrays.asList(str.split(pattern)).stream()
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .collect(Collectors.toList());
    }

    public static <K, X, T> Multimap<K, T> indexByAttributeSplit(
            Iterable<T> items,
            Function<? super T, ? extends X> keyFn,
            Function<? super X, ? extends Collection<K>> keySplitter) {

        Multimap<K, T> result = index(items, item -> {
            X key = keyFn.apply(item);
            Collection<K> r = key == null ? Collections.emptyList() : keySplitter.apply(key);
            return r;
        });

        return result;
    }


    public static <T, K> Multimap<K, T> index(Iterable<T> items, Function<? super T, ? extends Collection<K>> keysFn) {
        Multimap<K, T> result = ArrayListMultimap.create();

        Streams.stream(items)
            // item -> (unsplitKey, item)
            .map(item -> Maps.immutableEntry(keysFn.apply(item), item))
            // (item, keys) -> (key1, itemA), (key2, itemA), (key1, itemB) ...
            .flatMap(e -> e.getKey().stream()
                    .map(k -> Maps.immutableEntry(k, e.getValue())))
            .forEach(e -> result.put(e.getKey(), e.getValue()));

        return result;
    }

    /**
     *
     *
     * If both orgaByGroup and organization are provided,
     * the second is used a fallback orga in case no mapping applies.
     *
     * If orga by group is used without fallback, an attempt to upload without group is madem
     *
     * @param ckanClient
     * @param dcatModel
     * @param iriResolver
     * @param noFileUpload
     * @param orgaByGroup
     * @param organization
     * @return
     * @throws IOException
     */
    public static Model deploy(
            CkanClient ckanClient,
            Model dcatModel,
            IRIxResolver iriResolver,
            boolean noFileUpload,
            boolean orgaByGroup,
            String organization
        ) throws IOException {



        List<CkanOrganization> orgas = null;



        CkanOrganization fallbackTargetOrga = null;

        if(organization != null) {
            fallbackTargetOrga = ckanClient.getOrganization(organization);

            if(fallbackTargetOrga == null) {
                throw new RuntimeException("No organization found for id or name " + organization);
            }
        }

        // List dataset descriptions
        Model result = DcatUtils.createModelWithDcatFragment(dcatModel);
        Collection<DcatDataset> dcatDatasets = DcatUtils.listDcatDatasets(result);

        String GROUP_ID_KEY = "groupId";


        Multimap<String, CkanOrganization> groupIdToOrgas = null;

        for(DcatDataset d : dcatDatasets) {

            CkanOrganization targetOrga = null;
            if(orgaByGroup) {
                // If the dataset has a group id, try to match it to an organization
                String datasetGroupId = d.as(MavenEntity.class).getGroupId();
                if(datasetGroupId != null) {
                    logger.info("Dataset " + DcatDataset.getLabel(d) + " has groupId set - attempting to resolve against organizations");
                    if(orgas == null) {
                        // TODO We should directly make a lookup for orgas having a specific tag if the CKAN API allows for it
                        logger.info("Fetching organizations ...");
                        List<String> orgaNames = ckanClient.getOrganizationNames();
                        orgas = orgaNames.stream()
                                .map(name -> ckanClient.getOrganization(name))
                                .collect(Collectors.toList());
                        // The list does not include extra tags...
                        //orgas = ckanClientgetOrganizationList();

                        groupIdToOrgas = indexByAttributeSplit(orgas,
                                orga -> CkanDatasetUtils.getExtrasAsMap(orga.getExtras()).get(GROUP_ID_KEY),
                                stringSplitter("\\s+"));
                    }

                    Collection<CkanOrganization> candTargetOrgas = groupIdToOrgas.get(datasetGroupId);
                    if(candTargetOrgas.size() > 1) {
                        throw new RuntimeException("Multiple organization candidates: " + candTargetOrgas);
                    }


                    targetOrga = candTargetOrgas.isEmpty() ? null : candTargetOrgas.iterator().next();
                }
            }

            if(targetOrga == null) {
                targetOrga = fallbackTargetOrga;
            }

            String targetOrgaId = Optional.ofNullable(targetOrga)
                    .map(CkanOrganization::getId)
                    .orElse(null);

            String orgaSuffix = targetOrga != null ? " to organization " + targetOrga.nameOrId() : " without organization";

            logger.info("Deploying dataset " + DcatDataset.getLabel(d) + " (title = " + d.getTitle() + ")" + orgaSuffix);
            deploy(ckanClient, d, iriResolver, noFileUpload, targetOrgaId);
        }

        return result;
    }

    public static Optional<Path> pathsGet(URI uri) {
        Optional<Path> result;
        try {
            result = Optional.of(Paths.get(uri));
        } catch (Exception e) {
            result = Optional.empty();
            //throw new RuntimeException(e);
        }
        return result;
    }


//	public static URL tryToURL(URI uri) {
//		URL result;
//		try {
//			result = uri.toURL();
//		} catch (MalformedURLException e) {
//			throw new RuntimeException(e);
//		}
//		return result;
//	}

    public static void deploy(CkanClient ckanClient, DcatDataset dataset, IRIxResolver iriResolver, boolean noFileUpload, String targetOrgaId) throws IOException {
        String rawDatasetName = DcatDataset.getLabel(dataset);

        String datasetName = rawDatasetName
                .replace(":", "-")
                .replace(".", "-")
//                .replaceAll("[0-9]", "x");
                ;
        // datasetName = "test";
        logger.info("Post-processed name to " + datasetName);

        CkanDataset remoteCkanDataset;


        boolean isDatasetCreationRequired = false;
        try {
            remoteCkanDataset = ckanClient.getDataset(datasetName);
        } catch(CkanNotFoundException e) {
            logger.info("Dataset does not yet exist");
            remoteCkanDataset = new CkanDataset();
            isDatasetCreationRequired = true;
        } catch(CkanException e) {
            // TODO Maybe the dataset was deleted
            remoteCkanDataset = new CkanDataset();
            isDatasetCreationRequired = true;
        }

//		System.out.println("Before: " + remoteCkanDataset);

        // Update existing attributes with non-null values
        //dataset.getName(datasetId);
        DcatCkanRdfUtils.convertToCkan(remoteCkanDataset, dataset);

        // Use post processed name
//        remoteCkanDataset.setId(datasetName);
        remoteCkanDataset.setName(datasetName);

        remoteCkanDataset.setOwnerOrg(targetOrgaId);


        // Append tags
        // TODO Add switch whether to overwrite instead of append
        boolean replaceTags = false; // true = appendTags

        Optional<List<CkanTag>> existingTags = Optional.ofNullable(remoteCkanDataset.getTags());

        Optional<List<CkanTag>> newTags;
        if(replaceTags) {
            newTags = Optional.of(dataset.getKeywords().stream().map(CkanTag::new).collect(Collectors.toList()));
        } else {
            // Index existing tags by name
            Map<String, CkanTag> nameToTag = existingTags.orElse(Collections.emptyList()).stream()
                    .filter(tag -> tag.getVocabularyId() == null)
                    .collect(Collectors.toMap(CkanTag::getName, x -> x));

            // Allocate new ckan tags objects for non-covered keywords
            List<CkanTag> addedTags = dataset.getKeywords().stream()
                    .filter(keyword -> !nameToTag.containsKey(keyword))
                    .map(CkanTag::new)
                    .collect(Collectors.toList());

            // If there was no change, leave the original value (whether null or empty list)
            // Otherwise, reuse the existing tag list or allocate a new one
            newTags = addedTags.isEmpty()
                    ? existingTags
                    : Optional.of(existingTags.orElse(new ArrayList<>()));

            // If there were changes, append the added tags
            if(newTags.isPresent()) {
                newTags.get().addAll(addedTags);
            }
        }

        newTags.ifPresent(remoteCkanDataset::setTags);

//		System.out.println("After: " + remoteCkanDataset);

        if(isDatasetCreationRequired) {
            remoteCkanDataset = ckanClient.createDataset(remoteCkanDataset);
        } else {
            remoteCkanDataset = ckanClient.updateDataset(remoteCkanDataset);
        }


        for(DcatDistribution dcatDistribution : dataset.getDistributions()) {


            CkanResource remoteCkanResource = createOrUpdateResource(ckanClient, remoteCkanDataset, dataset, dcatDistribution);

            if (!noFileUpload) {

                // Check if there is a graph in the dataset that matches the distribution
                String distributionName = dcatDistribution.getTitle();

                logger.info("Deploying distribution " + distributionName);

                Set<String> downloadUrls = dcatDistribution.getDownloadURLs();

                List<String> resolvedUrls = downloadUrls.stream()
                        //.filter(Resource::isURIResource)
                        //.map(Resource::getURI)
                        .map(iriResolver::resolve)
                        .map(IRIx::str)
                        .collect(Collectors.toList());

                Set<URI> resolvedValidUrls = resolvedUrls.stream()
                        .map(str -> UriUtils.tryNewURI(str).orElse(null))
                        .filter(r -> r != null)
                        .collect(Collectors.toCollection(LinkedHashSet::new));

                if (resolvedUrls.size() > 1) {
                    logger.warn("Multiple URLs associated with a distribution; assuming they mirror content and choosing one from " + resolvedUrls);
                }

                Set<URI> urlsToExistingPaths = resolvedValidUrls.stream()
                        .filter(uri ->
                                DcatCkanDeployUtils.pathsGet(uri)
                                .filter(Files::exists)
                                .filter(Files::isRegularFile)
                                .isPresent())
                        .collect(Collectors.toSet());

                Set<URI> webUrls = Sets.difference(resolvedValidUrls, urlsToExistingPaths);

                String downloadFilename;
                Optional<Path> pathReference = Optional.empty();
                Path root = null;
                if (urlsToExistingPaths.size() > 0) {
                    URI fileUrl = urlsToExistingPaths.iterator().next();
                    pathReference = DcatCkanDeployUtils.pathsGet(fileUrl);
                    downloadFilename = pathReference.get().getFileName().toString();
                } else {
                    // TODO This should go through the conjure resource cache
                    root = Files.createTempDirectory("http-cache-");
                    URI webUrl = webUrls.iterator().next();
                    String webUrlPathStr = webUrl.getPath();
                    Path tmp =  Paths.get(webUrlPathStr);
                    downloadFilename = tmp.getFileName().toString();

                    HttpResourceRepositoryFromFileSystemImpl manager = HttpResourceRepositoryFromFileSystemImpl.create(root);

                    BasicHttpRequest r = new BasicHttpRequest("GET", webUrl.toASCIIString());
    //                r.setHeader(HttpHeaders.ACCEPT, WebContent.contentTypeTurtleAlt2);
    //                r.setHeader(HttpHeaders.ACCEPT_ENCODING, "gzip,identity;q=0");

                    RdfHttpEntityFile httpEntity = manager.get(r, HttpResourceRepositoryFromFileSystemImpl::resolveRequest);
                    pathReference = Optional.ofNullable(httpEntity).map(RdfHttpEntityFile::getAbsolutePath);
                }

                // TODO This breaks if the downloadURLs are web urls.
                // We need a flag whether to do a file upload for web urls, or whether to just update metadata

    //            Optional<Path> pathReference = resolvedValidUrls.stream()
    //                .map(DcatCkanDeployUtils::pathsGet)
    //                .filter(Optional::isPresent)
    //                .map(Optional::get)
    //                .filter(Files::exists)
    //                .findFirst();
    //

                if (pathReference.isPresent()) {
                    Path path = pathReference.get();

                    //String filename = distributionName + ".nt";
                    String probedContentType = null;
                    try {
                        probedContentType = Files.probeContentType(path);
                    } catch (IOException e) {
                        logger.warn("Failed to probe content type of " + path, e);
                    }

                    String contentType = Optional.ofNullable(probedContentType).orElse(ContentType.APPLICATION_OCTET_STREAM.toString());

//	                if (!noFileUpload) {

                    logger.info("Uploading file " + path);
                    CkanResource tmp = CkanClientUtils.uploadFile(
                            ckanClient,
                            remoteCkanDataset.getName(),
                            remoteCkanResource.getId(),
                            path.toString(),
                            ContentType.create(contentType),
                            downloadFilename);

                    tmp.setOthers(remoteCkanResource.getOthers());
                    int maxRetries = 5;
                    for(int i = 0; i < maxRetries; ++i) {
                        try {
                            remoteCkanResource = ckanClient.updateResource(tmp);
                            break;
                        } catch(Exception e) {
                            if(i + 1 < maxRetries) {
                                logger.warn("Failed to update resource, retrying " + (i + 1) + "/" + maxRetries);
                            } else {
                                logger.error("Giving up on updating a resource after " + maxRetries, e);
                            }
                        }
                    }
//					remoteCkanResource.setUrl(tmp.getUrl());
//					remoteCkanResource.setUrlType(tmp.getUrlType());

                    //remoteCkanResource.set
                    //remoteCkanResource = ckanClient.getResource(tmp.getId());
                    // Run the metadata update again

                    // This works, but retrieves the whole dataset on each resource, which we want to avoid
//					if(false) {
//						remoteCkanDataset = ckanClient.getDataset(remoteCkanDataset.getId());
//						remoteCkanResource = createOrUpdateResource(ckanClient, remoteCkanDataset, dataset, dcatDistribution);
//					}

                    //DcatCkanRdfUtils.convertToCkan(remoteCkanResource, dcatDistribution);


                    // FIXME upload currently destroys custom tags, hence we update the metadata again
                    //remoteCkanResource = ckanClient.updateResource(remoteCkanResource);


//	                } else {
//	                    logger.info("File upload disabled. Skipping " + path);
//	                }
                }

                Resource newDownloadUrl = ResourceFactory.createResource(remoteCkanResource.getUrl());

                org.aksw.jena_sparql_api.rdf.collections.ResourceUtils.setProperty(dcatDistribution, DCAT.downloadURL, newDownloadUrl);

                if (root != null) {
                    logger.info("Removing directory recursively: " + root);
                    // MoreFiles.deleteRecursively(root);
                }
            }
        }
    }

    /**
     * Create or update the appropriate resource among the ones in a given dataset
     *
     * @param ckanClient
     * @param dataset
     * @param res
     * @throws IOException
     */
    public static CkanResource createOrUpdateResource(CkanClient ckanClient, CkanDataset ckanDataset, DcatDataset dataset, DcatDistribution res) {
        Multimap<String, CkanResource> nameToCkanResources = Multimaps.index(
                Optional.ofNullable(ckanDataset.getResources()).orElse(Collections.emptyList()),
                CkanResource::getName);

        // Resources are required to have an ID
        String resName = res.getTitle();

        if(resName == null) {
            if(res.isURIResource()) {
                resName = SplitIRI.localname(res.getURI());
            }
        }

        if(resName == null) {
            new RuntimeException("DCAT Distribution / CKAN Resource must have a name i.e. public id");
        }

        boolean isResourceCreationRequired = false;

        CkanResource remote = null;
        Collection<CkanResource> remotes = nameToCkanResources.get(resName);

        // If there are multiple resources with the same name,
        // update the first one and delete all others

        Iterator<CkanResource> it = remotes.iterator();
        remote = it.hasNext() ? it.next() : null;

        while(it.hasNext()) {
            CkanResource tmp = it.next();
            ckanClient.deleteResource(tmp.getId());
        }


        // TODO We need a file for the resource

        if(remote == null) {
            isResourceCreationRequired = true;

            remote = new CkanResource(null, ckanDataset.getName());
            remote.setName(resName);
        }

        // Update existing attributes with non-null values
        DcatCkanRdfUtils.convertToCkan(remote, res);

        if (isResourceCreationRequired) {
            remote = ckanClient.createResource(remote);
        } else {
            remote = ckanClient.updateResource(remote);
        }

        return remote;
    }

}
