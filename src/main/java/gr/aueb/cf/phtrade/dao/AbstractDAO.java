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
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<T> entityRoot = countQuery.from(persistenceClass);

        List<Predicate> predicates = getPredicatesList(builder, entityRoot, criteria);
        countQuery.select(builder.count(entityRoot))
                .where(predicates.toArray(new Predicate[0]));

        TypedQuery<Long> query = em.createQuery(countQuery);
        addParametersToQuery(query, criteria);

        return query
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
        TypedQuery<T> query = em.createQuery(selectQuery);
        addParametersToQuery(query, criteria);

        return query.getResultList();
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
        addParametersToQuery(query, criteria);

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
    protected List<Predicate> getPredicatesList(CriteriaBuilder builder, Root<T> entityRoot, Map<String , Object> criteria) {
        List<Predicate> predicates = new ArrayList<>();

        for (Map.Entry<String, Object> entry : criteria.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            ParameterExpression<?> val = builder.parameter(value.getClass(), buildParameterAlias(key));
//            Predicate equal = builder.equal(resolvePath(entityRoot, key), val);
            Predicate predicateLike = builder.like((Expression<String>) resolvePath(entityRoot, key), (Expression<String>) val);
//            predicates.add(equal);
            predicates.add(predicateLike);
        }
        return predicates;
    }

    protected String buildParameterAlias(String alias) {
        return alias.replaceAll("\\.", "");
    }

    protected Path<?> resolvePath(Root<T> root, String expression) {
        String[] fields = expression.split("\\.");
        Path<?> path = root.get(fields[0]);
        for (int i = 1; i < fields.length; i++) {
            path = path.get(fields[i]);
        }
        return path;
    }

    protected void addParametersToQuery(TypedQuery<?> query, Map<String , Object> criteria) {
        for (Map.Entry<String , Object> entry : criteria.entrySet()) {
            Object value = entry.getValue();
            query.setParameter(buildParameterAlias(entry.getKey()), value + "%");
        }
    }
}
