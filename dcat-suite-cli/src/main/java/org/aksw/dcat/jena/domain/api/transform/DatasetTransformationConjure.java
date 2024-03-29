package org.aksw.dcat.jena.domain.api.transform;

import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRef;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.RdfTypeNs;
import org.aksw.jenax.annotation.reprogen.ResourceView;

/**
 *
 *
 * @author raven
 *
 */
@ResourceView
@RdfTypeNs("rpid")
public interface DatasetTransformationConjure
    extends DatasetTransformation
{
    @IriNs("rpif")
//    Job getJob();
//    void setJob(Job job);
    RdfDataRef getJobDataRef();
    DatasetTransformationConjure setJobDataRef(RdfDataRef jobDataRef);
}
