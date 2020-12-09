package org.aksw.dcat_suite.clients;

import static eu.trentorise.opendata.jackan.CkanClient.formatTimestamp;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import eu.trentorise.opendata.jackan.CkanClient;
import eu.trentorise.opendata.jackan.JackanModule;

public class DkanMapper extends JackanModule {
	private static final Logger LOG = Logger.getLogger(JackanModule.class.getName());

    public DkanMapper() {

        setNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        addSerializer(Timestamp.class, new StdSerializer<Timestamp>(Timestamp.class) {
            @Override
            public void serialize(Timestamp value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                try {
                    String str = formatTimestamp(value);
                    jgen.writeString(str);
                }
                catch (Exception ex) {
                    LOG.log(Level.SEVERE, "Couldn't format timestamp " + value + ", writing 'null'", ex);
                    jgen.writeNull();
                }
            }

        });

        addDeserializer(Timestamp.class, new JsonDeserializer<Timestamp>() {

            @Override
            public Timestamp deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
                JsonToken t = jp.getCurrentToken();

                if (t == JsonToken.VALUE_STRING) {
                    String str = jp.getText().trim();
                    try {
                        return DkanClient.parseTimestamp(str);
                    }
                    catch (IllegalArgumentException ex) {
                        LOG.log(Level.SEVERE, "Couldn't parse timestamp " + str + ", returning null", ex);
                        return null;
                    } catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
