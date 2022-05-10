package org.aksw.dcat_suite.app.model.api;

public interface LifeCycleEntity {

    // A local name
    // String getName();

    void delete();

    boolean exists();
    void create();

    // void open();
    // isOpen();
    // void close();

    default void createIfNotExists() {
        if (!exists()) {
            create();
        }
    }
}
