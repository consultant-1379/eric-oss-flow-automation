/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.services.flowautomation.flow.usertasks.schema.builder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.DEFAULT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.NAME;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.PROPERTIES;

public class SchemaPropertyWrapper implements Iterable<SchemaPropertyWrapper>{

    private final String key;
    private final Map<String, Object> inputProperty;

    public SchemaPropertyWrapper(final String key, final Map<String, Object> inputProperty) {
        this.key = key;
        this.inputProperty = getNullSafeMapValue(inputProperty);
    }

    public String getKey() {
        return this.key;
    }

    public Map<String, Object> getMap() {
        return inputProperty;
    }

    public String getName() {
        return (String) inputProperty.get(NAME);
    }
    public Map<String, Object> getProperties() {
        return getNullSafeMapValue(inputProperty.get(PROPERTIES));
    }

    /**
     * It can only be used if the value is a Map<String, Object>, otherwise it will throw a ClassCastException
     * @param key
     * @return
     */
    public SchemaPropertyWrapper getPropertyWrapper(final String key) {
        return new SchemaPropertyWrapper(key, getProperty(key));
    }

    public <T> T getProperty(final String key) {
        return (T) inputProperty.get(key);
    }

    public <T> T getDefault() {
        return (T) inputProperty.get(DEFAULT);
    }

    private Map<String, Object> getNullSafeMapValue(final Object value) {
        if (value == null) {
            return new HashMap<>();
        }

        return (Map<String, Object>) value;
    }

    @Override
    public Iterator<SchemaPropertyWrapper> iterator() {
        return new SchemaPropertyIterator(this);
    }
}
