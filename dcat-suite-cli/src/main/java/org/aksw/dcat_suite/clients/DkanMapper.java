package org.aksw.dcat_suite.clients;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import eu.trentorise.opendata.jackan.JackanModule;


public class DkanMapper extends JackanModule {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(JackanModule.class.getName());

    public DkanMapper() {

        setNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        /**
         * Method adapted from the Jackan CkanClient, see
         * https://github.com/opendatatrentino/jackan/blob/branch-0.4/src/main/java/eu/trentorise/opendata/jackan/JackanModule.java 
        */ 
        addDeserializer(Timestamp.class, new JsonDeserializer<Timestamp>() {

            @Override
            public Timestamp deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
                JsonToken t = jp.getCurrentToken();

                if (t == JsonToken.VALUE_STRING) {
                    String str = jp.getText().trim();
                    try {
                        return DkanClient.parseTimestamp(str);
                    }
                    catch (IllegalArgumentException | ParseException ex) {
                        LOG.log(Level.SEVERE, "Couldn't parse timestamp " + str + ", returning null", ex);
                        return null;
                    } 
                }

                if (t == JsonToken.VALUE_NULL) {
                    return null;
                }

                LOG.log(Level.SEVERE, "Unrecognized json token for timestamp {0}, returning null", t.asString());
                return null;
                
            }

        });
   }

}
