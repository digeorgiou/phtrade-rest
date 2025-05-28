package gr.aueb.cf.phtrade.service;

import gr.aueb.cf.phtrade.core.exceptions.AppServerException;
import gr.aueb.cf.phtrade.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.phtrade.core.exceptions.EntityNotAuthorizedException;
import gr.aueb.cf.phtrade.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.phtrade.dto.PharmacyInsertDTO;
import gr.aueb.cf.phtrade.dto.PharmacyReadOnlyDTO;
import gr.aueb.cf.phtrade.dto.PharmacyUpdateDTO;
import gr.aueb.cf.phtrade.model.Pharmacy;

import java.util.List;

public interface IPharmacyService {

    PharmacyReadOnlyDTO createPharmacy(PharmacyInsertDTO dto,
                                       Long creatorUserId) throws EntityAlreadyExistsException, EntityNotFoundException,AppServerException;
    PharmacyReadOnlyDTO updatePharmacy(PharmacyUpdateDTO dto,
                                       Long updaterUserId) throws EntityAlreadyExistsException, EntityNotAuthorizedException,
            EntityNotFoundException, AppServerException;
    void deletePharmacy(Long id, Long deleterUserId) throws EntityNotFoundException, EntityNotAuthorizedException;
    boolean nameExists(String name) throws AppServerException;
    PharmacyReadOnlyDTO getPharmacyById(Long id) throws EntityNotFoundException;
    PharmacyReadOnlyDTO getPharmacyByName(String name) throws EntityNotFoundException, AppServerException;
    List<PharmacyReadOnlyDTO> searchPharmaciesByName(String name) throws AppServerException;
    List<PharmacyReadOnlyDTO> searchPharmaciesByUser(String username) throws AppServerException;
    List<PharmacyReadOnlyDTO> getAllPharmacies() throws AppServerException;
}
