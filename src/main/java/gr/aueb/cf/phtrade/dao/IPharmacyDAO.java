package gr.aueb.cf.phtrade.dao;

import gr.aueb.cf.phtrade.model.Pharmacy;

import java.util.Optional;

public interface IPharmacyDAO extends IGenericDAO<Pharmacy> {

    Optional<Pharmacy> getByIdWithRelations(Long id,
                                            boolean loadRecordsGiver,
                                            boolean loadRecordsReceiver,
                                            boolean loadContacts);

    boolean existsByName(String name);

}
