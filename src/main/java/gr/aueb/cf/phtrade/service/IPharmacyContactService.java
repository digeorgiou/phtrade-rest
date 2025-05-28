package gr.aueb.cf.phtrade.service;

import gr.aueb.cf.phtrade.dto.ContactInsertDTO;
import gr.aueb.cf.phtrade.dto.ContactReadOnlyDTO;
import gr.aueb.cf.phtrade.dto.ContactUpdateDTO;

import java.util.List;

public interface IPharmacyContactService {

    ContactReadOnlyDTO saveContact(ContactInsertDTO dto);
    ContactReadOnlyDTO updateContact(ContactUpdateDTO dto);
    void deleteContact(Long contactId) ;
    ContactReadOnlyDTO findById(Long contactId);
    boolean contactExists(Long userId, Long pharmacyId);
}
