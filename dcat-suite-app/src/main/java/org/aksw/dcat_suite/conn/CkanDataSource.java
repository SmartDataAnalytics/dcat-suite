package org.aksw.dcat_suite.conn;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;

@ResourceView
public interface CkanDataSource
    extends DataSource
{
    @IriType
    @IriNs("eg")
    String getUrl();
    CkanDataSource setUrl(String url);

    @IriNs("eg")
    String getApiKey();
    CkanDataSource setApiKey(String apiKey);
}
