package gr.aueb.cf.phtrade.service;

import gr.aueb.cf.phtrade.core.exceptions.*;
import gr.aueb.cf.phtrade.dto.*;

import java.util.List;

public interface IUserService {

    UserReadOnlyDTO insertUser(UserInsertDTO dto) throws AppServerException, EntityAlreadyExistsException;
    UserReadOnlyDTO updateUser(UserUpdateDTO dto) throws EntityNotFoundException, EntityAlreadyExistsException, AppServerException;;
    void deleteUser(Long userIdToDelete, Long loggedInUserId) throws EntityNotAuthorizedException, EntityNotFoundException;
    UserReadOnlyDTO getUserById(Long id) throws EntityNotFoundException;
    List<UserReadOnlyDTO> getAllUsers();
    UserReadOnlyDTO getUserByUsername(String username) throws EntityNotFoundException;
    List<PharmacyReadOnlyDTO> getUserPharmacies(Long userId) throws EntityNotFoundException;
    List<ContactReadOnlyDTO> getUserContacts(Long userId) throws EntityNotFoundException;
    boolean isUserValid(String username, String password);
    boolean usernameExists(String username) throws AppServerException;
    boolean emailExists(String email) throws AppServerException;
}
