package gr.aueb.cf.phtrade.dao;

import gr.aueb.cf.phtrade.model.User;

import java.util.Optional;

public interface IUserDAO extends IGenericDAO<User>{

    Optional<User> getByIdWithRelations(Long id, boolean loadPharmacies,
                                        boolean loadContacts,
                                        boolean loadRecorderRecords);
    Optional<User> getByUsername(String username);
    boolean isUserValid(String username, String password);
    boolean isAdmin(Long userId);
    boolean usernameExists(String username);
    boolean emailExists(String email);
}
