package gr.aueb.cf.phtrade.dao;

import gr.aueb.cf.phtrade.model.PharmacyContact;

public interface IPharmacyContactDAO extends IGenericDAO<PharmacyContact>{

    boolean existsByUserAndPharmacy(Long userId, Long pharmacyId);
}
