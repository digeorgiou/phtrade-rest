package gr.aueb.cf.phtrade.dao;

import gr.aueb.cf.phtrade.model.Pharmacy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

import java.util.Optional;

@ApplicationScoped
public class PharmacyDAOImpl extends AbstractDAO<Pharmacy> implements IPharmacyDAO{
    public PharmacyDAOImpl() {
        this.setPersistenceClass(Pharmacy.class);
    }

    public Optional<Pharmacy> getByIdWithRelations(Long id,
                                                   boolean loadRecordsGiver,
                                                   boolean loadRecordsReceiver,
                                                   boolean loadContacts) {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Pharmacy> query = cb.createQuery(Pharmacy.class);
        Root<Pharmacy> root = query.from(Pharmacy.class);

        if (loadRecordsGiver) {
            root.fetch("recordsGiver", JoinType.LEFT);
        }
        if(loadRecordsReceiver){
            root.fetch("recordsReceiver", JoinType.LEFT);
        }
        if (loadContacts) {
            root.fetch("contactReferences", JoinType.LEFT);
        }
        query.where(cb.equal(root.get("id"), id));
        try{
            Pharmacy pharmacy = em.createQuery(query).getSingleResult();
            return Optional.ofNullable(pharmacy);
        } catch (NoResultException e){
            return Optional.empty();
        }
    }

    public boolean existsByName(String name){
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Pharmacy> pharmacyRoot = query.from(Pharmacy.class);

        query.select(cb.count(pharmacyRoot))
                .where(cb.equal(pharmacyRoot.get("name"),name));

        boolean result =
                getEntityManager().createQuery(query).getSingleResult() > 0;

        return result;
    }

}
