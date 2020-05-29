package org.aksw.ckan_deploy.core;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.ckan_deploy.dcat.CkanDatasetUtils;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.jena.domain.api.MvnEntity;
import org.aksw.dcat.utils.DcatUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.util.SplitIRI;
import org.apache.jena.vocabulary.DCAT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

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
     */
    public static Model deploy(
            CkanClient ckanClient,
            Model dcatModel,
            IRIResolver iriResolver,
            boolean noFileUpload,
            boolean orgaByGroup,
            String organization
        ) {



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



        for(DcatDataset d : dcatDatasets) {

            CkanOrganization targetOrga = null;
            if(orgaByGroup) {
                // If the dataset has a group id, try to match it to an organization
                String datasetGroupId = d.as(MvnEntity.class).getGroupId();
                if(datasetGroupId != null) {
                    logger.info("Dataset " + DcatDataset.getLabel(d) + " has groupId set - attempting to resolve against organizations");
                    if(orgas == null) {
                        // TODO We should directly make a lookup for orgas having a specific tag if the CKAN API allows for it
                        logger.info("Fetching organizations ...");
                        orgas = ckanClient.getOrganizationList();
                    }

                    String GROUP_ID = "groupId";

                    targetOrga = orgas.stream()
                        .filter(orga -> {
                            Map<String, String> map = CkanDatasetUtils.getExtrasAsMap(orga.getExtras());
                            String orgaGroupId = map.get(GROUP_ID);

                            boolean r = Objects.equals(orgaGroupId, datasetGroupId);
                            return r;
                        })
                        .findFirst()
                        .orElse(null);
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

    public static URL toURL(URI uri) {
        URL result;
        try {
            result = uri.toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }


      public static URL newURL(String uri) {
          // There was some reason why to go from String to URL via URI... but i forgot...
          URI tmp = newURI(uri);
          URL result;
          try {
            result = tmp.toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
          return result;
      }

      public static URI newURI(String uri) {
        URI result;
        try {
            result = new URI(uri);
        } catch (URISyntaxException e) {
            result = null;
        }
        return result;
    }

    public static Optional<URI> tryNewURI(String uri) {
        Optional<URI> result = Optional.ofNullable(newURI(uri));
        return result;
    }

    public static void deploy(CkanClient ckanClient, DcatDataset dataset, IRIResolver iriResolver, boolean noFileUpload, String targetOrgaId) {
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

            // Check if there is a graph in the dataset that matches the distribution
            String distributionName = dcatDistribution.getTitle();

            logger.info("Deploying distribution " + distributionName);

            Set<String> downloadUrls = dcatDistribution.getDownloadURLs();

            List<String> resolvedUrls = downloadUrls.stream()
                    //.filter(Resource::isURIResource)
                    //.map(Resource::getURI)
                    .map(iriResolver::resolveToStringSilent)
                    .collect(Collectors.toList());

            List<URI> resolvedValidUrls = resolvedUrls.stream()
                    .map(str -> DcatCkanDeployUtils.tryNewURI(str).orElse(null))
                    .filter(r -> r != null)
                    .collect(Collectors.toList());

            // TODO This breaks if the downloadURLs are web urls.
            // We need a flag whether to do a file upload for web urls, or whether to just update metadata

            Optional<Path> pathReference = resolvedValidUrls.stream()
                .map(DcatCkanDeployUtils::pathsGet)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(Files::exists)
                .findFirst();


            if(pathReference.isPresent()) {
                Path path = pathReference.get();

                //String filename = distributionName + ".nt";
                String filename = path.getFileName().toString();
                String probedContentType = null;
                try {
                    probedContentType = Files.probeContentType(path);
                } catch (IOException e) {
                    logger.warn("Failed to probe content type of " + path, e);
                }

                String contentType = Optional.ofNullable(probedContentType).orElse(ContentType.APPLICATION_OCTET_STREAM.toString());

                if(!noFileUpload) {
                    logger.info("Uploading file " + path);
                    CkanResource tmp = CkanClientUtils.uploadFile(
                            ckanClient,
                            remoteCkanDataset.getName(),
                            remoteCkanResource.getId(),
                            path.toString(),
                            ContentType.create(contentType),
                            filename);

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


                } else {
                    logger.info("File upload disabled. Skipping " + path);
                }
            }

            Resource newDownloadUrl = ResourceFactory.createResource(remoteCkanResource.getUrl());

            org.aksw.jena_sparql_api.rdf.collections.ResourceUtils.setProperty(dcatDistribution, DCAT.downloadURL, newDownloadUrl);
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
