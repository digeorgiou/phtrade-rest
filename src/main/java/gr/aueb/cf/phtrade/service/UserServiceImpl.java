package gr.aueb.cf.phtrade.service;

import gr.aueb.cf.phtrade.core.exceptions.*;
import gr.aueb.cf.phtrade.dao.IUserDAO;
import gr.aueb.cf.phtrade.dto.*;
import gr.aueb.cf.phtrade.mapper.Mapper;
import gr.aueb.cf.phtrade.model.Pharmacy;
import gr.aueb.cf.phtrade.model.PharmacyContact;
import gr.aueb.cf.phtrade.model.User;
import gr.aueb.cf.phtrade.service.util.JPAHelper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserServiceImpl implements IUserService{

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    private final IUserDAO userDAO;

    @Inject
    public UserServiceImpl(IUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public UserReadOnlyDTO insertUser(UserInsertDTO dto) throws AppServerException, EntityAlreadyExistsException {
        try{
            JPAHelper.beginTransaction();

            // Check if username or email already exists
            if (userDAO.usernameExists(dto.username())) {
                throw new EntityAlreadyExistsException("User", "Username " + dto.username() + " already exists");
            }
            if (userDAO.emailExists(dto.email())) {
                throw new EntityAlreadyExistsException("User", "Email " + dto.email() + " already exists");
            }

            User user = Mapper.mapUserInsertToModel(dto);

            UserReadOnlyDTO readOnlyDTO = userDAO.insert(user)
                    .map(Mapper::mapToUserReadOnlyDTO)
                    .orElseThrow(()->new AppServerException("User", "User " +
                            "with username: " + dto.username() + " not " +
                            "inserted"));
            JPAHelper.commitTransaction();
            LOGGER.info("User with username= {} inserted", dto.username());
            return readOnlyDTO;
        } catch (AppServerException | EntityAlreadyExistsException  e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("User with username= {} not inserted.",
                    dto.username(), e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public UserReadOnlyDTO updateUser(UserUpdateDTO dto) throws EntityNotFoundException, EntityAlreadyExistsException, AppServerException {
        try{
            JPAHelper.beginTransaction();

            User existingUser = userDAO.getById(dto.id()).orElseThrow(()-> new EntityNotFoundException("User",
                    "User with id " + dto.id() + " was not found"));

            // Check if new username or email conflicts with other users
            if (!existingUser.getUsername().equals(dto.username()) && userDAO.usernameExists(dto.username())) {
                throw new EntityAlreadyExistsException("User", "Username " + dto.username() + " already exists");
            }
            if (!existingUser.getEmail().equals(dto.email()) && userDAO.emailExists(dto.email())) {
                throw new EntityAlreadyExistsException("User", "Email " + dto.email() + " already exists");
            }

            User updatedUser = Mapper.mapUserUpdateToModel(dto, existingUser);
            UserReadOnlyDTO readOnlyDTO = userDAO.update(updatedUser)
                    .map(Mapper::mapToUserReadOnlyDTO)
                    .orElseThrow(()-> new AppServerException(
                            "User", "User with id=" + dto.id() + " Error " +
                            "during update"));
            JPAHelper.commitTransaction();
            LOGGER.info("User with id={}, username={}, email={}, " +
                            "updated.",
                    updatedUser.getId(), updatedUser.getUsername(),
                    updatedUser.getEmail());

            return readOnlyDTO;

        }catch (EntityNotFoundException | AppServerException | EntityAlreadyExistsException e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("User with id={}, username={}, email={} not updated.",
                    dto.id(), dto.username(), dto.email(), e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public void deleteUser(Long userIdToDelete, Long loggedInUserId) throws EntityNotAuthorizedException, EntityNotFoundException {

        try{
            JPAHelper.beginTransaction();
            if(!userDAO.isAdmin(loggedInUserId)){
                throw new EntityNotAuthorizedException("User", "User not " +
                        "authorized to delete users");
            }

            User userToDelete = userDAO.getByIdWithRelations(userIdToDelete, true, true, true)
                    .orElseThrow(() -> new EntityNotFoundException("User", "User with id " + userIdToDelete + " not found"));

            // Handle related entities before deletion
            // 1. Remove all pharmacy associations
            if (userToDelete.getPharmacies() != null) {
                new ArrayList<>(userToDelete.getPharmacies()).forEach(userToDelete::removePharmacy);
            }

            // 2. Remove all contact associations
            if (userToDelete.getContacts() != null) {
                new ArrayList<>(userToDelete.getContacts()).forEach(userToDelete::removeContact);
            }

            // 3. Handle trade records where user is recorder
            if (userToDelete.getRecordsRecorder() != null) {
                // we shouldn't delete trade records, just nullify the recorder
                new ArrayList<>(userToDelete.getRecordsRecorder()).forEach(record -> {
                    record.setRecorder(null);
                });
            }

            userDAO.delete(userIdToDelete);
            JPAHelper.commitTransaction();
            LOGGER.info("User with id={} deleted by user id={}", userIdToDelete, loggedInUserId);
        } catch (EntityNotFoundException | EntityNotAuthorizedException e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error deleting user with id={}", userIdToDelete, e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public UserReadOnlyDTO getUserById(Long id) throws EntityNotFoundException {
        try {
            JPAHelper.beginTransaction();
            User user = userDAO.getById(id)
                    .orElseThrow(() -> new EntityNotFoundException("User", "User with id " + id + " not found"));

            JPAHelper.commitTransaction();
            return Mapper.mapToUserReadOnlyDTO(user);
        } catch (EntityNotFoundException e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error retrieving user with id={}", id, e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public List<UserReadOnlyDTO> getAllUsers() {
        try {
            JPAHelper.beginTransaction();
            List<User> users = userDAO.getAll();
            List<UserReadOnlyDTO> dtos = users.stream()
                    .map(Mapper::mapToUserReadOnlyDTO)
                    .collect(Collectors.toList());
            JPAHelper.commitTransaction();
            return dtos;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public UserReadOnlyDTO getUserByUsername(String username) throws EntityNotFoundException {
        try {
            JPAHelper.beginTransaction();

            User user = userDAO.findByField("username", username)
                    .orElseThrow(() -> new EntityNotFoundException("User", "Not found"));

            JPAHelper.commitTransaction();
            return Mapper.mapToUserReadOnlyDTO(user);
        } catch (EntityNotFoundException e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error retrieving user with username={}", username, e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public List<PharmacyReadOnlyDTO> getUserPharmacies(Long userId) throws EntityNotFoundException {
        try{
            JPAHelper.beginTransaction();

            User user = userDAO.getByIdWithRelations(userId, true, false, false)
                    .orElseThrow(() -> new EntityNotFoundException("User", "User with id " + userId + " not found"));

            List<Pharmacy> pharmacies = user.getPharmacies() != null ?
                    new ArrayList<>(user.getPharmacies()) :
                    List.of();

            JPAHelper.commitTransaction();
            return pharmacies.stream().map(Mapper::mapToPharmacyReadOnlyDTO)
                    .collect(Collectors.toList());
        } catch (EntityNotFoundException e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error retrieving pharmacies for user id={}", userId,
                    e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public List<ContactReadOnlyDTO> getUserContacts(Long userId) throws EntityNotFoundException {
        try {
            JPAHelper.beginTransaction();

            User user = userDAO.getByIdWithRelations(userId, false, true, false)
                    .orElseThrow(() -> new EntityNotFoundException("User", "User with id " + userId + " not found"));

            List<PharmacyContact> contacts = user.getContacts() != null ?
                    new ArrayList<>(user.getContacts()) :
                    List.of();

            JPAHelper.commitTransaction();
            return contacts.stream().map(Mapper::mapToPharmacyContactReadOnlyDTO)
                    .collect(Collectors.toList());
        } catch (EntityNotFoundException e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error retrieving contacts for user id={}", userId, e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public boolean isUserValid(String username, String password) {
        try{
            JPAHelper.beginTransaction();
            boolean isValid = userDAO.isUserValid(username, password);
            JPAHelper.commitTransaction();
            return isValid;

        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public boolean usernameExists(String username) throws AppServerException {
        try {
            JPAHelper.beginTransaction();
            boolean exists = userDAO.usernameExists(username);
            JPAHelper.commitTransaction();
            return exists;
        } catch (Exception e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error checking username existence for username={}", username, e);
            throw new AppServerException("User", "Error checking username existence");
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public boolean emailExists(String email) throws AppServerException {
        try {
            JPAHelper.beginTransaction();
            boolean exists = userDAO.emailExists(email);
            JPAHelper.commitTransaction();
            return exists;
        } catch (Exception e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error checking email existence for email={}", email, e);
            throw new AppServerException("User", "Error checking email existence");
        } finally {
            JPAHelper.closeEntityManager();
        }
    }
}
