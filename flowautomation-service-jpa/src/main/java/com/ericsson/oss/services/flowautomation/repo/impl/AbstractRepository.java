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

package com.ericsson.oss.services.flowautomation.repo.impl;

import static java.lang.String.format;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.ericsson.oss.services.flowautomation.jpa.EntityManagerWrapper;
import com.ericsson.oss.services.flowautomation.repo.Repository;

/**
 * Default implementation of the {@link com.ericsson.oss.services.flowautomation.repo.Repository} interface.
 *
 * @param <T>
 *         the type of the entity to handle
 * @param <I>
 *         the type of the entity's identifier
 */
@Default
public abstract class AbstractRepository<T, I extends Serializable> implements Repository<T, I> {

    @Inject
    protected EntityManagerWrapper emw;

    protected abstract Class<T> getEntityType();

    @Override
    public <S extends T> S save(final S entity) {
        emw.getEntityManager().persist(entity);
        return entity;
    }

    @Override
    public <S extends T> S saveAndFlush(final S entity) {
        emw.getEntityManager().persist(entity);
        emw.getEntityManager().flush();
        return entity;
    }

    @Override
    public <S extends T> S update(final S entity) {
        return emw.getEntityManager().merge(entity);
    }

    @Override
    public int update(final String namedQuery, final Map<String, Object> queryParameters) {
        final Query query = emw.getEntityManager().createNamedQuery(namedQuery);
        if (queryParameters != null && !queryParameters.isEmpty()) {
            queryParameters.forEach(query::setParameter);
        }
        return query.executeUpdate();

    }

    @Override
    public T findOne(final I id) {
        return emw.getEntityManager().find(getEntityType(), id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T findOne(final String namedQuery, final Map<String, Object> queryParameters) {
        final Query query = emw.getEntityManager().createNamedQuery(namedQuery);
        if (queryParameters != null && !queryParameters.isEmpty()) {
            queryParameters.forEach(query::setParameter);
        }
        return (T) query.getSingleResult();

    }

    @Override
    public List<T> findAll() {
        final CriteriaBuilder criteriaBuilder = emw.getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<T> cq = criteriaBuilder.createQuery(getEntityType());
        final Root<T> from = cq.from(getEntityType());
        cq.select(from);
        return emw.getEntityManager().createQuery(cq).getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> findAll(final String qlString, final Map<String, Object> qFilters, final Map<String, Object> qParameters) {
        final Query q = getDynamicNamedQuery(qlString, qFilters, qParameters);
        return q.getResultList();
    }


    @SuppressWarnings({"unchecked", "squid:S00107"})
    public List<T> findAllByFilter(final String qlString,
                                   final Map<String, Object> qFilters,
                                   final Map<String, Object> qParameters,
                                   final String sortBy,
                                   final String sortOrder,
                                   final Integer firstResult,
                                   final Integer maxResult) {

        final String qlStringAux = getDynamicQuery(qlString, qFilters, sortBy, sortOrder);
        final Query query = emw.getEntityManager().createQuery(qlStringAux);

        if (firstResult != null && maxResult != null) {
            query.setFirstResult(firstResult);
            query.setMaxResults(maxResult);
        }

        qParameters.forEach(query::setParameter);
        return query.getResultList();
    }

    public long count(final String qlString,
                      final Map<String, Object> qFilters,
                      final Map<String, Object> qParameters) {

        final String qlStringAux = getDynamicQuery(qlString, qFilters, null, null);
        final Query query = emw.getEntityManager().createQuery(qlStringAux);
        qParameters.forEach(query::setParameter);
        return ((Number) query.getSingleResult()).intValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> findAllResults(final String namedQuery, final Map<String, Object> queryParameters) {
        final Query query = emw.getEntityManager().createNamedQuery(namedQuery);
        if (queryParameters != null && !queryParameters.isEmpty()) {
            queryParameters.forEach(query::setParameter);
        }
        return query.getResultList();
    }

    @Override
    public T findReferenceOnly(final I id) {
        return emw.getEntityManager().getReference(getEntityType(), id);
    }

    @Override
    public void delete(final I id) {
        final T entityToBeRemoved = emw.getEntityManager().getReference(getEntityType(), id);
        emw.getEntityManager().remove(entityToBeRemoved);
    }

    @Override
    public void delete(final T entity) {
        emw.getEntityManager().remove(entity);
    }

    @Override
    public int delete(final String namedQuery, final Map<String, Object> queryParameters) {
        return update(namedQuery, queryParameters);
    }

    @Override
    public int delete(final String sqlString, final Map<String, Object> queryFilters, final Map<String, Object> queryParameters) {
        final Query query = getDynamicNamedQuery(sqlString, queryFilters, queryParameters);
        return query.executeUpdate();
    }

    @Override
    public long count(final String namedQuery, final Map<String, Object> queryParameters) {
        final Query query = emw.getEntityManager().createNamedQuery(namedQuery);
        if (queryParameters != null && !queryParameters.isEmpty()) {
            queryParameters.forEach(query::setParameter);
        }
        return ((Number) query.getSingleResult()).intValue();

    }

    @Override
    public String singleValue(final String namedQuery, final Map<String, Object> queryParameters) {
        final Query query = emw.getEntityManager().createNamedQuery(namedQuery);
        if (queryParameters != null && !queryParameters.isEmpty()) {
            queryParameters.forEach(query::setParameter);
        }
        return (String) query.setMaxResults(1).getSingleResult();
    }

    /**
     * Gets the dynamic @NamedQuery.
     *
     * @param qlString
     *         the query string
     * @param filters
     *         the query criteria conditions
     * @param parameters
     *         the query parameters
     * @return the dynamic named query
     */
    private Query getDynamicNamedQuery(final String qlString, final Map<String, Object> filters, final Map<String, Object> parameters) {
        final String qlStringAux = getDynamicQuery(qlString, filters, null, null);
        final Query query = emw.getEntityManager().createQuery(qlStringAux);
        parameters.forEach(query::setParameter);
        return query;
    }

    /**
     * Builds the dynamic query given its criteria conditions.
     *
     * @param query
     *         the query
     * @param filters
     *         the query criteria conditions
     * @param sortBy
     * @param sortOrder
     * @return the dynamic query
     */
    private String getDynamicQuery(final String query, final Map<String, Object> filters, final String sortBy, final String sortOrder) {
        final StringJoiner dynamicQuery = new StringJoiner(" ");
        dynamicQuery.add(query);
        if (filters != null && !filters.isEmpty()) {
            dynamicQuery.add("WHERE");
            final String conditions = filters.entrySet().stream().map(e -> {
                final String key = e.getKey();
                final Object value = e.getValue();
                if (key.contains(" IN")) {
                    return format("%s (:%s)", key, value);
                } else if (key.endsWith(" LIKE")) {
                    return format("%s CONCAT('%%',:%s,'%%')", key, value);
                } else if (key.endsWith(" >=") || key.endsWith(" <=")) {
                    return format("%s :%s", key, value);
                } else {
                    return format("%s = :%s", key, value);
                }
            }).collect(Collectors.joining(" AND "));
            dynamicQuery.add(conditions);
        }

        if (sortBy != null && sortOrder != null) {
            dynamicQuery.add("ORDER BY").add(sortBy).add(sortOrder);
        }
        return dynamicQuery.toString();
    }

    @Override
    public int executeNativeQuery(final String sqlQuery, final Map<String, Object> queryParameters) {
        final Query query = emw.getEntityManager().createNativeQuery(sqlQuery);
        queryParameters.forEach(query::setParameter);
        return query.executeUpdate();
    }

    @Override
    public int executeNativeQuery(final String sqlQuery) {
        final Query query = emw.getEntityManager().createNativeQuery(sqlQuery);
        return query.executeUpdate();
    }

    @Override
    public List<String> executeNativeSelectQuery(final String sqlQuery) {
        final Query query = emw.getEntityManager().createNativeQuery(sqlQuery);
        return query.getResultList();
    }

}
