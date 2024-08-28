/*******************************************************************************
 * COPYRIGHT Ericsson 2021
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

package com.ericsson.oss.services.flowautomation.flow.usertasks.schema.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The SchemaCache is a LRU cache.
 */
public enum SchemaCache {

    SCHEMA_CACHE;

    private final LRUCache<String, Object> cache;

    SchemaCache() {
        this.cache = new LRUCache<>(32);
    }

    /**
     * Gets the schema.
     *
     * @param deploymentId
     *            the deployment id
     * @param resourceName
     *            the resource name
     * @return the map
     */
    public synchronized String get(final String deploymentId, final String resourceName) {
        return (String) cache.get(String.join(".-.", deploymentId, resourceName));
    }

    /**
     * Put the schema.
     *
     * @param deploymentId
     *            the deployment id
     * @param resourceName
     *            the resource name
     * @param schema
     *            the schema
     */
    public synchronized void put(final String deploymentId, final String resourceName, final String schema) {
        cache.put(String.join(".-.", deploymentId, resourceName), schema);
    }

    /**
     * The Class LRUCache.
     *
     * @param <String>
     *            the generic type
     * @param <Object>
     *            the generic type
     */
    @SuppressWarnings({ "squid:S2160", "squid:S00119" })
    private class LRUCache<String, Object> extends LinkedHashMap<String, Object> {

        private static final long serialVersionUID = 1L;

        private final int cacheSize;

        private LRUCache(final int cacheSize) {
            super(32, 0.75f, true);
            this.cacheSize = cacheSize;
        }

        @Override
        protected boolean removeEldestEntry(final Map.Entry<String, Object> eldest) {
            return size() > cacheSize;
        }
    }
}
