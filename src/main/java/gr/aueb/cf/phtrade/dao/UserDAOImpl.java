package gr.aueb.cf.phtrade.dao;

import gr.aueb.cf.phtrade.core.enums.RoleType;
import gr.aueb.cf.phtrade.model.Pharmacy;
import gr.aueb.cf.phtrade.model.User;
import gr.aueb.cf.phtrade.security.SecUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

import java.util.Optional;

@ApplicationScoped
public class UserDAOImpl extends AbstractDAO<User> implements IUserDAO{

    public UserDAOImpl(){
        this.setPersistenceClass(User.class);
    }

    @Override
    public Optional<User> getByIdWithRelations(Long id, boolean loadPharmacies, boolean loadContacts, boolean loadRecorderRecords) {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);

        if(loadPharmacies){
            root.fetch("pharmacies", JoinType.LEFT);
        }
        if(loadContacts){
            root.fetch("contacts", JoinType.LEFT);
        }
        if (loadRecorderRecords) {
            root.fetch("recordsRecorder", JoinType.LEFT);
        }

        query.where(cb.equal(root.get("id"), id));
        try {
            User user = em.createQuery(query).getSingleResult();
            return Optional.ofNullable(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> getByUsername(String username) {
        return Optional.empty();
    }

    @Override
    public boolean isUserValid(String username, String password) {

        try {
            User user =
                    findByField("username", username)
                            .orElseThrow(() -> new NoResultException("User " +
                                    "not Found"));
            return SecUtil.checkPassword(password, user.getPassword());
        }   catch (NoResultException e){
            return false;
        }

    }

    @Override
    public boolean isAdmin(Long userId) {
        EntityManager em = getEntityManager();
        User user = em.find(User.class, userId);
        return user.getRoleType() == RoleType.ADMIN;
    }

    @Override
    public boolean usernameExists(String username) {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<User> root = query.from(User.class);

        query.select(cb.count(root))
                .where(cb.equal(root.get("username"),username));

        return em.createQuery(query).getSingleResult() > 0;
    }

    @Override
    public boolean emailExists(String email) {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<User> root = query.from(User.class);

        query.select(cb.count(root))
                .where(cb.equal(root.get("email"),email));

        return em.createQuery(query).getSingleResult() > 0;
    }
}
