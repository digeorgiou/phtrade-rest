package gr.aueb.cf.phtrade.service;

import gr.aueb.cf.phtrade.core.exceptions.AppServerException;
import gr.aueb.cf.phtrade.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.phtrade.core.exceptions.EntityNotAuthorizedException;
import gr.aueb.cf.phtrade.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.phtrade.dto.BalanceDTO;
import gr.aueb.cf.phtrade.dto.PharmacyInsertDTO;
import gr.aueb.cf.phtrade.dto.PharmacyReadOnlyDTO;
import gr.aueb.cf.phtrade.dto.PharmacyUpdateDTO;
import gr.aueb.cf.phtrade.model.Pharmacy;

import java.util.List;
import java.util.Map;

public interface IPharmacyService {

    PharmacyReadOnlyDTO createPharmacy(PharmacyInsertDTO dto) throws EntityAlreadyExistsException, EntityNotFoundException,AppServerException;
    PharmacyReadOnlyDTO updatePharmacy(PharmacyUpdateDTO dto) throws EntityAlreadyExistsException, EntityNotAuthorizedException,
            EntityNotFoundException, AppServerException;
    void deletePharmacy(Long id, Long deleterUserId) throws EntityNotFoundException;
    boolean nameExists(String name) throws AppServerException;
    PharmacyReadOnlyDTO getPharmacyById(Long id) throws EntityNotFoundException;
    List<PharmacyReadOnlyDTO> getPharmaciesByCriteria(Map<String,Object> criteria);
    List<PharmacyReadOnlyDTO> getPharmaciesByCriteriaPaginated(Map<String,
            Object> criteria, Integer page, Integer size);
    long getPharmaciesCountByCriteria(Map<String, Object> criteria);
    PharmacyReadOnlyDTO getPharmacyByName(String name) throws EntityNotFoundException;
    List<PharmacyReadOnlyDTO> searchPharmaciesByName(String name) throws AppServerException;
    List<PharmacyReadOnlyDTO> searchPharmaciesByUser(String username) throws AppServerException;
    List<PharmacyReadOnlyDTO> getAllPharmacies() throws AppServerException;
    List<BalanceDTO> getBalanceList(Long pharmacyId, String sortBy) throws EntityNotFoundException;
}
