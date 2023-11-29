package org.aksw.dcat_suite.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * The entry point of the Spring Boot application.
 */
@SpringBootApplication
@ServletComponentScan
public class MainAppDcatSuiteVaadin extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(MainAppDcatSuiteVaadin.class, args);
    }

}
