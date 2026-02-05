package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.dao;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GenericDAO<T, ID> {

    private final Class<T> entityClass;

    public GenericDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    // ==================== CRUD ====================

    public T save(EntityManager em, T entity) {
        em.persist(entity);
        em.getTransaction().commit();
        return entity;
    }

    public T update(EntityManager em, T entity) {
        T merged = em.merge(entity);
        em.getTransaction().commit();
        return merged;
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
        em.getTransaction().commit();
    }

    public void deleteById(EntityManager em, ID id) {
        T entity = em.find(entityClass, id);
        if (entity != null) {
            em.remove(entity);
        }
        em.getTransaction().commit();
    }

    // ==================== FILTRAGE GENERIQUE ====================

    /**
     * Méthode générique pour rechercher avec filtres, recherche par mot-clé et pagination.
     *
     * @param em           EntityManager
     * @param filters      Map des filtres (nom du champ -> valeur), peut être null
     * @param keyword      Mot-clé pour recherche textuelle, peut être null
     * @param keywordFields Champs sur lesquels appliquer la recherche par mot-clé
     * @param orderBy      Champ de tri (ex: "date DESC"), peut être null
     * @param joins        Jointures FETCH (ex: "LEFT JOIN FETCH e.author"), peut être null
     * @param page         Numéro de page (0-indexed), -1 pour ignorer la pagination
     * @param size         Taille de page, -1 pour ignorer la pagination
     * @return Liste des résultats
     */
    public List<T> findWithFilters(EntityManager em,
                                   Map<String, Object> filters,
                                   String keyword,
                                   String[] keywordFields,
                                   String orderBy,
                                   String joins,
                                   int page,
                                   int size) {

        StringBuilder jpql = new StringBuilder("SELECT e FROM ")
                .append(entityClass.getSimpleName())
                .append(" e ");

        if (joins != null && !joins.isEmpty()) {
            jpql.append(joins.replace("e.", "e.")).append(" ");
        }

        Map<String, Object> params = new HashMap<>();
        List<String> conditions = new ArrayList<>();

        // Ajout des filtres
        if (filters != null && !filters.isEmpty()) {
            for (Map.Entry<String, Object> entry : filters.entrySet()) {
                String field = entry.getKey();
                String paramName = field.replace(".", "_");
                conditions.add("e." + field + " = :" + paramName);
                params.put(paramName, entry.getValue());
            }
        }

        // Ajout de la recherche par mot-clé
        if (keyword != null && !keyword.isEmpty() && keywordFields != null && keywordFields.length > 0) {
            List<String> keywordConditions = new ArrayList<>();
            for (String field : keywordFields) {
                keywordConditions.add("LOWER(e." + field + ") LIKE LOWER(:keyword)");
            }
            conditions.add("(" + String.join(" OR ", keywordConditions) + ")");
            params.put("keyword", "%" + keyword + "%");
        }

        if (!conditions.isEmpty()) {
            jpql.append("WHERE ").append(String.join(" AND ", conditions)).append(" ");
        }

        if (orderBy != null && !orderBy.isEmpty()) {
            jpql.append("ORDER BY e.").append(orderBy);
        }

        TypedQuery<T> query = em.createQuery(jpql.toString(), entityClass);
        params.forEach(query::setParameter);

        if (page >= 0 && size > 0) {
            query.setFirstResult(page * size);
            query.setMaxResults(size);
        }

        return query.getResultList();
    }

    /**
     * Version simplifiée sans pagination
     */
    public List<T> findWithFilters(EntityManager em,
                                   Map<String, Object> filters,
                                   String keyword,
                                   String[] keywordFields,
                                   String orderBy,
                                   String joins) {
        return findWithFilters(em, filters, keyword, keywordFields, orderBy, joins, -1, -1);
    }

    /**
     * Version minimale - juste les filtres
     */
    public List<T> findWithFilters(EntityManager em, Map<String, Object> filters) {
        return findWithFilters(em, filters, null, null, null, null, -1, -1);
    }

    /**
     * Récupérer tous les éléments avec pagination
     */
    public List<T> findAll(EntityManager em, int page, int size) {
        return findWithFilters(em, null, null, null, null, null, page, size);
    }

    /**
     * Récupérer tous les éléments
     */
    public List<T> findAll(EntityManager em) {
        return findWithFilters(em, null, null, null, null, null, -1, -1);
    }

    // ==================== COMPTAGE GENERIQUE ====================

    /**
     * Compter avec filtres et recherche par mot-clé
     */
    public long countWithFilters(EntityManager em,
                                 Map<String, Object> filters,
                                 String keyword,
                                 String[] keywordFields) {

        StringBuilder jpql = new StringBuilder("SELECT COUNT(e) FROM ")
                .append(entityClass.getSimpleName())
                .append(" e ");

        Map<String, Object> params = new HashMap<>();
        List<String> conditions = new ArrayList<>();

        if (filters != null && !filters.isEmpty()) {
            for (Map.Entry<String, Object> entry : filters.entrySet()) {
                String field = entry.getKey();
                String paramName = field.replace(".", "_");
                conditions.add("e." + field + " = :" + paramName);
                params.put(paramName, entry.getValue());
            }
        }

        if (keyword != null && !keyword.isEmpty() && keywordFields != null && keywordFields.length > 0) {
            List<String> keywordConditions = new ArrayList<>();
            for (String field : keywordFields) {
                keywordConditions.add("LOWER(e." + field + ") LIKE LOWER(:keyword)");
            }
            conditions.add("(" + String.join(" OR ", keywordConditions) + ")");
            params.put("keyword", "%" + keyword + "%");
        }

        if (!conditions.isEmpty()) {
            jpql.append("WHERE ").append(String.join(" AND ", conditions));
        }

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        params.forEach(query::setParameter);

        return query.getSingleResult();
    }

    /**
     * Compter avec filtres uniquement
     */
    public long countWithFilters(EntityManager em, Map<String, Object> filters) {
        return countWithFilters(em, filters, null, null);
    }

    /**
     * Compter tous les éléments
     */
    public long count(EntityManager em) {
        return countWithFilters(em, null, null, null);
    }

    // ==================== RECHERCHE UNIQUE ====================

    /**
     * Trouver un seul élément avec filtres
     */
    public Optional<T> findOneWithFilters(EntityManager em, Map<String, Object> filters, String joins) {
        List<T> results = findWithFilters(em, filters, null, null, null, joins, 0, 1);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<T> findOneWithFilters(EntityManager em, Map<String, Object> filters) {
        return findOneWithFilters(em, filters, null);
    }
}
