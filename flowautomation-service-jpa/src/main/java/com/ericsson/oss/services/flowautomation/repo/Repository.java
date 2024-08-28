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

package com.ericsson.oss.services.flowautomation.repo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * The Interface Repository.
 *
 * @param <T> the generic type
 * @param <I> the generic type
 */
public interface Repository<T, I extends Serializable> {

    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the entity instance completely.
     *
     * @param <S>    the type parameter
     * @param entity the entity
     * @return the saved entity
     */
    <S extends T> S save(S entity);

    /**
     * Save and flush.
     *
     * @param <S>    the generic type
     * @param entity the entity
     * @return the s
     */
    <S extends T> S saveAndFlush(S entity);

    /**
     * Update a given entity. Use the returned instance for further operations as the update operation might have changed the entity instance
     * completely.
     *
     * @param <S>    the type parameter
     * @param entity the entity
     * @return the updated entity
     */
    <S extends T> S update(S entity);

    /**
     * Update using Named Query
     *
     * @param namedQuery      the named query
     * @param queryParameters the query parameters
     * @return int int
     */
    int update(final String namedQuery, final Map<String, Object> queryParameters);

    /**
     * Retrieves an entity by its id.
     *
     * @param id must not be {@literal null}.
     * @return the entity with the given id or {@literal null} if none found
     * @throws IllegalArgumentException if {@code id} is {@literal null}
     */
    T findOne(I id);

    /**
     * Find one result.
     *
     * @param namedQuery      the named query
     * @param queryParameters the query parameters
     * @return the t
     */
    T findOne(final String namedQuery, final Map<String, Object> queryParameters);

    /**
     * Returns all instances of the type.
     *
     * @return all entities
     */
    List<T> findAll();

    /**
     * Returns all instances of the type.
     *
     * @param qlString    the plain query string
     * @param qFilters    the query criteria conditions
     * @param qParameters the query parameters
     * @return the list
     */
    List<T> findAll(final String qlString, final Map<String, Object> qFilters, final Map<String, Object> qParameters);

    /**
     * Returns all instances of the type with the given @{link NamedQuery} and its query parameters.
     *
     * @param namedQuery      the named query
     * @param queryParameters the query parameters
     * @return the list
     */
    List<T> findAllResults(final String namedQuery, final Map<String, Object> queryParameters);

    /**
     * Find references only
     *
     * @param id must not be {@literal null}.
     * @return the found entity instance
     */
    T findReferenceOnly(final I id);

    /**
     * Deletes the entity with the given id.
     *
     * @param id must not be {@literal null}.
     * @throws IllegalArgumentException in case the given {@code id} is {@literal null}
     */
    void delete(I id);

    /**
     * Deletes a given entity.
     *
     * @param entity the entity
     * @throws IllegalArgumentException in case the given entity is {@literal null}.
     */
    void delete(T entity);

    /**
     * Delete.
     *
     * @param namedQuery      the named query
     * @param queryParameters the query parameters
     * @return the int
     */
    int delete(final String namedQuery, final Map<String, Object> queryParameters);

    /**
     * Delete.
     *
     * @param sqlString       the sql string
     * @param queryFilters    the query filters
     * @param queryParameters the query parameters
     * @return the int
     */
    int delete(final String sqlString, final Map<String, Object> queryFilters, final Map<String, Object> queryParameters);

    /**
     * Returns the number of entities available matching the filter.
     *
     * @param namedQuery      the named query
     * @param queryParameters the query parameters
     * @return the number of entities
     */
    long count(final String namedQuery, final Map<String, Object> queryParameters);

    /**
     * Single value string.
     *
     * @param namedQuery      the named query
     * @param queryParameters the query parameters
     * @return string string
     */
    String singleValue(final String namedQuery, final Map<String, Object> queryParameters);

    /**
     * Execute native query.
     *
     * @param sqlQuery        the sql query
     * @param queryParameters the query parameters
     * @return the int
     */
    int executeNativeQuery(final String sqlQuery, Map<String, Object> queryParameters);

    /**
     * Execute native query.
     *
     * @param sqlQuery the sql query
     * @return the int
     */
    int executeNativeQuery(final String sqlQuery);


    /**
     * Execute native select query list.
     *
     * @param sqlQuery the sql query
     * @return the list
     */
    List<String> executeNativeSelectQuery(final String sqlQuery);

}
