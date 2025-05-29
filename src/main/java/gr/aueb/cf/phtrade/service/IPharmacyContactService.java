package gr.aueb.cf.phtrade.service;

import gr.aueb.cf.phtrade.core.exceptions.AppServerException;
import gr.aueb.cf.phtrade.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.phtrade.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.phtrade.dto.ContactInsertDTO;
import gr.aueb.cf.phtrade.dto.ContactReadOnlyDTO;
import gr.aueb.cf.phtrade.dto.ContactUpdateDTO;

import java.util.List;

public interface IPharmacyContactService {

    ContactReadOnlyDTO saveContact(ContactInsertDTO dto) throws EntityNotFoundException, EntityAlreadyExistsException, AppServerException;
    ContactReadOnlyDTO updateContact(ContactUpdateDTO dto) throws EntityNotFoundException, AppServerException;
    void deleteContact(Long contactId) throws EntityNotFoundException, AppServerException ;
    ContactReadOnlyDTO findById(Long contactId) throws EntityNotFoundException, AppServerException;
    boolean contactExists(Long userId, Long pharmacyId) throws AppServerException;
}
