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

import java.util.Iterator;
import java.util.Map;

public class SchemaPropertyIterator implements Iterator<SchemaPropertyWrapper> {

    private final Iterator<Map.Entry<String, Object>> iterator;

    public SchemaPropertyIterator(SchemaPropertyWrapper inputProperty) {
        this.iterator = inputProperty.getMap().entrySet().iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public SchemaPropertyWrapper next() {
        Map.Entry<String, Object> next = iterator.next();
        return new SchemaPropertyWrapper(next.getKey(), (Map<String, Object>) next.getValue());
    }
}
