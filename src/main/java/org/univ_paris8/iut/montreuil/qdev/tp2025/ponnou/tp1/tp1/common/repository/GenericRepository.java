package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenericRepository<T, ID> {

    private static final Pattern VALID_FIELD_NAME = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_.]*$");
    private static final Pattern VALID_JOIN_CLAUSE = Pattern.compile(
            "(LEFT\\s+)?JOIN\\s+FETCH\\s+e\\.[a-zA-Z_][a-zA-Z0-9_.]*"
    );
    private static final Set<String> VALID_ORDER_DIRECTIONS = Set.of("ASC", "DESC");

    private final Class<T> entityClass;

    public GenericRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public T save(EntityManager em, T entity) {
        em.persist(entity);
        return entity;
    }

    public T update(EntityManager em, T entity) {
        return em.merge(entity);
    }

    public Optional<T> findById(EntityManager em, ID id) {
        T entity = em.find(entityClass, id);
        return Optional.ofNullable(entity);
    }

    public void delete(EntityManager em, T entity) {
        if (!em.contains(entity)) {
            entity = em.merge(entity);
        }
        em.remove(entity);
    }

    public boolean deleteById(EntityManager em, ID id) {
        T entity = em.find(entityClass, id);
        if (entity == null) {
            return false;
        }
        em.remove(entity);
        return true;
    }

    public List<T> findWithFilters(EntityManager em,
                                   Map<String, Object> filters,
                                   String keyword,
                                   String[] keywordFields,
                                   String orderBy,
                                   String joins,
                                   int page,
                                   int size) {

        boolean hasJoins = joins != null && !joins.isEmpty();

        StringBuilder jpql = new StringBuilder(hasJoins ? "SELECT DISTINCT e FROM " : "SELECT e FROM ")
                .append(entityClass.getSimpleName())
                .append(" e ");

        if (hasJoins) {
            validateJoinClause(joins);
            jpql.append(joins).append(" ");
        }

        Map<String, Object> params = new HashMap<>();
        buildWhereClause(jpql, params, filters, keyword, keywordFields);

        if (orderBy != null && !orderBy.isEmpty()) {
            appendOrderBy(jpql, orderBy);
        }

        TypedQuery<T> query = em.createQuery(jpql.toString(), entityClass);
        params.forEach(query::setParameter);

        if (page >= 0 && size > 0) {
            query.setFirstResult(page * size);
            query.setMaxResults(size);
        }

        return query.getResultList();
    }

    public List<T> findWithFilters(EntityManager em,
                                   Map<String, Object> filters,
                                   String keyword,
                                   String[] keywordFields,
                                   String orderBy,
                                   String joins) {
        return findWithFilters(em, filters, keyword, keywordFields, orderBy, joins, -1, -1);
    }

    public long countWithFilters(EntityManager em,
                                 Map<String, Object> filters,
                                 String keyword,
                                 String[] keywordFields) {

        StringBuilder jpql = new StringBuilder("SELECT COUNT(e) FROM ")
                .append(entityClass.getSimpleName())
                .append(" e ");

        Map<String, Object> params = new HashMap<>();
        buildWhereClause(jpql, params, filters, keyword, keywordFields);

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        params.forEach(query::setParameter);

        return query.getSingleResult();
    }

    public long countWithFilters(EntityManager em, Map<String, Object> filters) {
        return countWithFilters(em, filters, null, null);
    }

    public long count(EntityManager em) {
        return countWithFilters(em, null, null, null);
    }

    public Optional<T> findOneWithFilters(EntityManager em, Map<String, Object> filters, String joins) {
        List<T> results = findWithFilters(em, filters, null, null, null, joins, 0, 1);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<T> findOneWithFilters(EntityManager em, Map<String, Object> filters) {
        return findOneWithFilters(em, filters, null);
    }

    private void buildWhereClause(StringBuilder jpql,
                                  Map<String, Object> params,
                                  Map<String, Object> filters,
                                  String keyword,
                                  String[] keywordFields) {

        List<String> conditions = new ArrayList<>();

        if (filters != null && !filters.isEmpty()) {
            int i = 0;
            for (Map.Entry<String, Object> entry : filters.entrySet()) {
                String field = entry.getKey();
                validateFieldName(field);
                String paramName = field.replace(".", "_") + "_" + i;
                conditions.add("e." + field + " = :" + paramName);
                params.put(paramName, entry.getValue());
                i++;
            }
        }

        if (keyword != null && !keyword.isEmpty() && keywordFields != null && keywordFields.length > 0) {
            List<String> keywordConditions = new ArrayList<>();
            for (String field : keywordFields) {
                validateFieldName(field);
                keywordConditions.add("LOWER(e." + field + ") LIKE LOWER(:keyword)");
            }
            conditions.add("(" + String.join(" OR ", keywordConditions) + ")");
            params.put("keyword", "%" + keyword + "%");
        }

        if (!conditions.isEmpty()) {
            jpql.append("WHERE ").append(String.join(" AND ", conditions)).append(" ");
        }
    }

    private void appendOrderBy(StringBuilder jpql, String orderBy) {
        String[] parts = orderBy.trim().split("\\s+");
        String field = parts[0];
        validateFieldName(field);

        jpql.append("ORDER BY e.").append(field);

        if (parts.length > 1) {
            String direction = parts[1].toUpperCase();
            if (!VALID_ORDER_DIRECTIONS.contains(direction)) {
                throw new IllegalArgumentException("Direction de tri invalide : " + direction);
            }
            jpql.append(" ").append(direction);
        }
    }

    private void validateFieldName(String field) {
        if (field == null || !VALID_FIELD_NAME.matcher(field).matches()) {
            throw new IllegalArgumentException("Nom de champ invalide : " + field);
        }
    }

    private void validateJoinClause(String joins) {
        Matcher matcher = VALID_JOIN_CLAUSE.matcher(joins.trim());
        int lastEnd = 0;
        boolean found = false;
        while (matcher.find()) {
            found = true;
            String between = joins.trim().substring(lastEnd, matcher.start()).trim();
            if (!between.isEmpty()) {
                throw new IllegalArgumentException("Clause JOIN invalide : " + joins);
            }
            lastEnd = matcher.end();
        }
        if (!found || lastEnd != joins.trim().length()) {
            throw new IllegalArgumentException("Clause JOIN invalide : " + joins);
        }
    }
}
