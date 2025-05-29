package gr.aueb.cf.phtrade.service;

import gr.aueb.cf.phtrade.core.exceptions.AppServerException;
import gr.aueb.cf.phtrade.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.phtrade.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.phtrade.dao.IPharmacyContactDAO;
import gr.aueb.cf.phtrade.dao.IPharmacyDAO;
import gr.aueb.cf.phtrade.dao.IUserDAO;
import gr.aueb.cf.phtrade.dto.ContactInsertDTO;
import gr.aueb.cf.phtrade.dto.ContactReadOnlyDTO;
import gr.aueb.cf.phtrade.dto.ContactUpdateDTO;
import gr.aueb.cf.phtrade.mapper.Mapper;
import gr.aueb.cf.phtrade.model.Pharmacy;
import gr.aueb.cf.phtrade.model.PharmacyContact;
import gr.aueb.cf.phtrade.model.User;
import gr.aueb.cf.phtrade.service.util.JPAHelper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ApplicationScoped
public class PharmacyContactServiceImpl implements IPharmacyContactService {


    private static final Logger LOGGER =
            LoggerFactory.getLogger(PharmacyContactServiceImpl.class);
    private final IPharmacyDAO pharmacyDAO;
    private final IUserDAO userDAO;
    private final IPharmacyContactDAO contactDAO;

    @Inject
    public PharmacyContactServiceImpl(IPharmacyDAO pharmacyDAO, IUserDAO userDAO, IPharmacyContactDAO contactDAO) {
        this.pharmacyDAO = pharmacyDAO;
        this.userDAO = userDAO;
        this.contactDAO = contactDAO;
    }

    @Override
    public ContactReadOnlyDTO saveContact(ContactInsertDTO dto) throws EntityNotFoundException, EntityAlreadyExistsException, AppServerException{
        try{
            JPAHelper.beginTransaction();

            User user = userDAO.getById(dto.userId()).orElseThrow(()-> new EntityNotFoundException("User",
                    "User with id " + dto.userId() + " was not found"));

            Pharmacy pharmacy =pharmacyDAO.getById(dto.pharmacyId()).orElseThrow(()-> new EntityNotFoundException("Pharmacy",
                            "Pharmacy with id " + dto.pharmacyId() + " was " +
                                    "not found"));

            if(contactDAO.existsByUserAndPharmacy(dto.userId(),
                    dto.pharmacyId())){
                throw new EntityAlreadyExistsException("Contact",
                        "Contact already exists");
            }

            PharmacyContact contact =
                    Mapper.mapPharmacyContactInsertToModel(dto);

            pharmacy.addContactReference(contact);
            user.addContact(contact);

            pharmacyDAO.update(pharmacy);
            userDAO.update(user);

            ContactReadOnlyDTO contactDTO = contactDAO.insert(contact)
                    .map(Mapper::mapToPharmacyContactReadOnlyDTO)
                    .orElseThrow(() -> new AppServerException("Contact",
                            "Failed to create Contact"));

            JPAHelper.commitTransaction();

            LOGGER.info("Contact created with ID: {}", contactDTO.id());
            return contactDTO;

        } catch (EntityNotFoundException | EntityAlreadyExistsException | AppServerException  e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Contact with name= {} not inserted.",
                    dto.contactName(), e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public ContactReadOnlyDTO updateContact(ContactUpdateDTO dto) throws EntityNotFoundException, AppServerException{
        try {
            JPAHelper.beginTransaction();

            PharmacyContact existingContact = contactDAO.getById(dto.id())
                    .orElseThrow(() -> new EntityNotFoundException("Contact", "Contact with id " + dto.id() + " not found"));

            PharmacyContact updatedContact = Mapper.mapPharmacyContactUpdateToModel(dto, existingContact);
            ContactReadOnlyDTO contactDTO = contactDAO.update(updatedContact)
                    .map(Mapper::mapToPharmacyContactReadOnlyDTO)
                    .orElseThrow(() -> new AppServerException("Contact",
                            "Failed to update Contact"));

            JPAHelper.commitTransaction();
            LOGGER.info("Contact {} updated", dto.id());
            return contactDTO;

        } catch (EntityNotFoundException | AppServerException e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error updating contact {}", dto.id(), e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public void deleteContact(Long contactId) throws EntityNotFoundException, AppServerException {

        try {
            JPAHelper.beginTransaction();

            PharmacyContact contact = contactDAO.getById(contactId)
                    .orElseThrow(() -> new EntityNotFoundException("Contact", "Contact with id " + contactId + " not found"));

            // Handle bidirectional relationships
            if (contact.getPharmacy() != null) {
                contact.getPharmacy().removeContactReference(contact);
            }
            if (contact.getUser() != null) {
                contact.getUser().removeContact(contact);
            }

            contactDAO.delete(contactId);
            JPAHelper.commitTransaction();
            LOGGER.info("Contact {} deleted", contactId);

        } catch (Exception e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error deleting contact {}", contactId, e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }

    }

    @Override
    public ContactReadOnlyDTO findById(Long contactId) throws EntityNotFoundException, AppServerException{
        try {
            JPAHelper.beginTransaction();
            PharmacyContact contact = contactDAO.getById(contactId)
                    .orElseThrow(() -> new EntityNotFoundException("Contact", "Contact with id " + contactId + " not found"));

            return Mapper.mapToPharmacyContactReadOnlyDTO(contact);
        } catch (Exception e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error fetching contact {}", contactId, e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public boolean contactExists(Long userId, Long pharmacyId) throws AppServerException{
        try {
            JPAHelper.beginTransaction();
            boolean exists = contactDAO.existsByUserAndPharmacy(userId, pharmacyId);
            JPAHelper.commitTransaction();
            return exists;
        } catch (Exception e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error checking contact existence for user {} and pharmacy {}", userId, pharmacyId, e);
            throw new AppServerException("Contact", "Error checking contact " +
                    "existence");
        } finally {
            JPAHelper.closeEntityManager();
        }
    }
}
