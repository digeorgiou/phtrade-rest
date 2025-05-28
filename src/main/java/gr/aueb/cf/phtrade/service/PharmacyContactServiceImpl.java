package gr.aueb.cf.phtrade.service;

import gr.aueb.cf.phtrade.dto.ContactInsertDTO;
import gr.aueb.cf.phtrade.dto.ContactReadOnlyDTO;
import gr.aueb.cf.phtrade.dto.ContactUpdateDTO;

import java.util.List;

public class PharmacyContactServiceImpl implements IPharmacyContactService {


    @Override
    public ContactReadOnlyDTO saveContact(ContactInsertDTO dto) {
        return null;
    }

    @Override
    public ContactReadOnlyDTO updateContact(ContactUpdateDTO dto) {
        return null;
    }

    @Override
    public void deleteContact(Long contactId) {

    }

    @Override
    public ContactReadOnlyDTO findById(Long contactId) {
        return null;
    }

    @Override
    public boolean contactExists(Long userId, Long pharmacyId) {
        return false;
    }
}
