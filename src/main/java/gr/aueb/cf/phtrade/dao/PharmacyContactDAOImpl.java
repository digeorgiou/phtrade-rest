package gr.aueb.cf.phtrade.dao;

import gr.aueb.cf.phtrade.model.PharmacyContact;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

@ApplicationScoped
public class PharmacyContactDAOImpl extends AbstractDAO<PharmacyContact> implements IPharmacyContactDAO{

    public PharmacyContactDAOImpl() {
        this.setPersistenceClass(PharmacyContact.class);
    }

    @Override
    public boolean existsByUserAndPharmacy(Long userId, Long pharmacyId) {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<PharmacyContact> root = query.from(PharmacyContact.class);

        query.select(cb.count(root));
        query.where(
                cb.and(
                        cb.equal(root.get("user").get("id"), userId),
                        cb.equal(root.get("pharmacy").get("id"), pharmacyId)
                )
        );

        return em.createQuery(query).getSingleResult() > 0;
    }
}
