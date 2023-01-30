package org.aksw.dcat_suite.app.model.api;

public interface LifeCycleEntity {

    // A local name
    // String getName();

    void delete() throws Exception;

    boolean exists();
    void create() throws Exception;

    // void open();
    // isOpen();
    // void close();

    default void createIfNotExists() throws Exception {
        if (!exists()) {
            create();
        }
    }
}
