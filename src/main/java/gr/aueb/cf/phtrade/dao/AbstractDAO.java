package gr.aueb.cf.phtrade.dao;

import gr.aueb.cf.phtrade.model.IdentifiableEntity;
import gr.aueb.cf.phtrade.service.util.JPAHelper;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import java.util.*;

public class AbstractDAO<T extends IdentifiableEntity> implements IGenericDAO<T> {

    private Class<T> persistenceClass;

    public AbstractDAO() {

    }

    public Class<T> getPersistenceClass() {
        return persistenceClass;
    }

    public void setPersistenceClass(Class<T> persistenceClass) {
        this.persistenceClass = persistenceClass;
    }

    @Override
    public Optional<T> insert(T t) {
        EntityManager em = getEntityManager();
        em.persist(t);
        //Optional.of creates an Optional that must contain a non-null value
        //if t were null, it would throw NullPointerException
        return Optional.of(t);
    }

    @Override
    public Optional<T> update(T t) {
        EntityManager em = getEntityManager();
        em.merge(t);
        return Optional.of(t);
    }

    @Override
    public void delete(Object id) {
        EntityManager em = getEntityManager();
        Optional<T> toDelete = getById(id);
        toDelete.ifPresent(em::remove);
    }

    @Override
    public Optional<T> getById(Object id) {
        EntityManager em = getEntityManager();
        return Optional.ofNullable(em.find(persistenceClass,id));
    }

    @Override
    public long count() {
        EntityManager em = getEntityManager();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<T> entityRoot = countQuery.from(persistenceClass);

        countQuery.select(builder.count(entityRoot));

        return em.createQuery(countQuery).getSingleResult();
    }

    @Override
    public long getCountByCriteria(Map<String, Object> criteria) {
        EntityManager em = getEntityManager();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<T> entityRoot = countQuery.from(persistenceClass);

        List<Predicate> predicates = getPredicatesList(builder, entityRoot, criteria);
        countQuery.select(builder.count(entityRoot))
                .where(predicates.toArray(new Predicate[0]));


        return em.createQuery(countQuery)
                .getSingleResult();
    }


    @Override
    public Optional<T> findByField(String fieldName, Object value) {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(persistenceClass);
        Root<T> root = query.from(persistenceClass);

        // Dynamic WHERE clause: e.fieldName = value
        query.select(root)
                .where(cb.equal(root.get(fieldName), value));

        TypedQuery<T> typedQuery = em.createQuery(query);
        return typedQuery.getResultList().stream().findFirst();
    }

    @Override
    public List<T> getAll() {
        return getByCriteria(getPersistenceClass(), Collections.emptyMap());
    }

    @Override
    public List<T> getByCriteria(Map<String, Object> criteria) {
        return getByCriteria(getPersistenceClass(), criteria);
    }

    @Override
    public List<T> getByCriteria(Class<T> clazz, Map<String, Object> criteria) {
        EntityManager em = getEntityManager();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> selectQuery = builder.createQuery(clazz);
        Root<T> entityRoot = selectQuery.from(clazz);

        List<Predicate> predicates = getPredicatesList(builder, entityRoot, criteria);
        selectQuery.select(entityRoot).where(predicates.toArray(new Predicate[0]));

        return em.createQuery(selectQuery).getResultList();
    }

    @Override
    public List<T> getByCriteriaPaginated(Class<T> clazz, Map<String, Object> criteria, Integer page, Integer size) {
        EntityManager em = getEntityManager();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> selectQuery = builder.createQuery(clazz);
        Root<T> entityRoot = selectQuery.from(clazz);

        // Build predicates
        List<Predicate> predicates = getPredicatesList(builder, entityRoot, criteria);
        selectQuery.select(entityRoot).where(predicates.toArray(new Predicate[0]));

        // Create query and apply pagination
        TypedQuery<T> query = em.createQuery(selectQuery);

        if (page != null && size != null) {
            query.setFirstResult(page * size);      // skip
            query.setMaxResults(size);
        }
        return query.getResultList();
    }



    public EntityManager getEntityManager() {
        return JPAHelper.getEntityManager();
    }


    @SuppressWarnings("unchecked")
    protected List<Predicate> getPredicatesList(CriteriaBuilder builder, Root<T> entityRoot, Map<String, Object> criteria) {
        List<Predicate> predicates = new ArrayList<>();

        for (Map.Entry<String, Object> entry : criteria.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            // Handling the cases where the value is a List, Map or a "isNull" condition
            if (value instanceof List) {
                Path<?> path = resolvePath(entityRoot, key);
                CriteriaBuilder.In<Object> inClause = builder.in(path);
                for (Object v : (List<?>) value) {
                    inClause.value(v);
                }
                predicates.add(inClause);
            } else if (value instanceof Map) {
                // For 'BETWEEN' condition
                Map<String, Object> mapValue = (Map<String, Object>) value;
                if (mapValue.containsKey("from") && mapValue.containsKey("to")) {
                    Object from = mapValue.get("from");
                    Object to = mapValue.get("to");

                    if (from instanceof Comparable && to instanceof Comparable) {
                        Expression<? extends Comparable<Object>> path =
                                (Expression<? extends Comparable<Object>>) resolvePath(entityRoot, key);

                        predicates.add(builder.between(path, (Comparable<Object>) from, (Comparable<Object>) to));
                    }
                }
            } else if ("isNull".equals(value)) {
                // For 'IS NULL' condition
                predicates.add(builder.isNull(resolvePath(entityRoot, key)));
            } else if ("isNotNull".equals(value)) {
                // For 'IS NOT NULL' condition
                predicates.add(builder.isNotNull(resolvePath(entityRoot, key)));
            } else if (value instanceof String ) {
                Path<?> path = resolvePath(entityRoot, key);
                String strValue = (String) value;
                if(strValue.contains("%")){
                // Treat as LIKE pattern (e.g., "Jo%")
                predicates.add(
                        builder.like(
                                builder.lower(path.as(String.class)),
                                strValue.toLowerCase()));
                }else {
                    predicates.add(builder.equal(builder.lower(path.as(String.class)),
                            strValue.toLowerCase()));
                }
            } else {
                Path<?> path = resolvePath(entityRoot, key);
                // For '=' condition (default case)
                predicates.add(builder.equal(path,value));
            }
        }
        return predicates;
    }

    protected Path<?> resolvePath(Root<T> root, String expression) {
        String[] fields = expression.split("\\.");
        Path<?> path = root.get(fields[0]);
        for (int i = 1; i < fields.length; i++) {
            path = path.get(fields[i]);
        }
        return path;
    }


    protected String buildParameterAlias(String alias) {
        return alias.replaceAll("\\.", "");
    }

}
