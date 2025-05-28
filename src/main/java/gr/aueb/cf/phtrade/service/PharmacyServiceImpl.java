package gr.aueb.cf.phtrade.service;

import gr.aueb.cf.phtrade.core.exceptions.AppServerException;
import gr.aueb.cf.phtrade.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.phtrade.core.exceptions.EntityNotAuthorizedException;
import gr.aueb.cf.phtrade.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.phtrade.dao.IPharmacyDAO;
import gr.aueb.cf.phtrade.dao.IUserDAO;
import gr.aueb.cf.phtrade.dto.PharmacyInsertDTO;
import gr.aueb.cf.phtrade.dto.PharmacyReadOnlyDTO;
import gr.aueb.cf.phtrade.dto.PharmacyUpdateDTO;
import gr.aueb.cf.phtrade.mapper.Mapper;
import gr.aueb.cf.phtrade.model.Pharmacy;
import gr.aueb.cf.phtrade.model.User;
import gr.aueb.cf.phtrade.service.util.JPAHelper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ApplicationScoped
public class PharmacyServiceImpl implements IPharmacyService{

    private static final Logger LOGGER = LoggerFactory.getLogger(PharmacyServiceImpl.class);
    private final IPharmacyDAO pharmacyDAO;
    private final IUserDAO userDAO;

    @Inject
    public PharmacyServiceImpl(IPharmacyDAO pharmacyDAO, IUserDAO userDAO) {
        this.pharmacyDAO = pharmacyDAO;
        this.userDAO = userDAO;
    }

    @Override
    public PharmacyReadOnlyDTO createPharmacy(PharmacyInsertDTO dto,
                                              Long creatorUserId) throws EntityAlreadyExistsException, EntityNotFoundException, AppServerException {
        try {
            JPAHelper.beginTransaction();

            // Check if pharmacy name exists
            if (pharmacyDAO.existsByName(dto.name())) {
                throw new EntityAlreadyExistsException("Pharmacy", "Name " + dto.name() + " already exists");
            }
            // Get the creator user
            User creator = userDAO.getByIdWithRelations(creatorUserId,true,
                            true,true)
                    .orElseThrow(() -> new EntityNotFoundException("User", "Creator not found"));

            // Create and save pharmacy
            Pharmacy pharmacy = Mapper.mapPharmacyInsertToModel(dto, creator);
            Pharmacy savedPharmacy = pharmacyDAO.insert(pharmacy)
                    .orElseThrow(() -> new AppServerException("Pharmacy", "Failed to create pharmacy"));

            // Add to user's pharmacies
            creator.addPharmacy(savedPharmacy);

            JPAHelper.commitTransaction();
            LOGGER.info("Pharmacy created with ID: {}", savedPharmacy.getId());
            return Mapper.mapToPharmacyReadOnlyDTO(savedPharmacy);


        } catch (Exception e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error creating pharmacy", e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public PharmacyReadOnlyDTO updatePharmacy(PharmacyUpdateDTO dto, Long updaterUserId) throws EntityAlreadyExistsException,
            EntityNotAuthorizedException, EntityNotFoundException,
            AppServerException {
        try {
            JPAHelper.beginTransaction();

            // Verify pharmacy exists
            Pharmacy existingPharmacy = pharmacyDAO.getByIdWithRelations(dto.id(),
                            true,true, true)
                    .orElseThrow(() -> new EntityNotFoundException("Pharmacy"
                            ,  + dto.id() + "not found"));

            if(!userDAO.isAdmin(updaterUserId) && !(Objects.equals(existingPharmacy.getUser().getId(), updaterUserId))){
                throw new EntityNotAuthorizedException("User", "User is not " +
                        "authorized to update Pharmacy with id=" + dto.id());
            }

            // Check name uniqueness if changed
            if (!existingPharmacy.getName().equals(dto.name()) && pharmacyDAO.existsByName(dto.name())) {
                throw new EntityAlreadyExistsException("Pharmacy", "Name " + dto.name() + " already exists");
            }

            // Update pharmacy
            Pharmacy updatedPharmacy = Mapper.mapPharmacyUpdateToModel(dto, existingPharmacy);
            pharmacyDAO.update(updatedPharmacy);

            JPAHelper.commitTransaction();
            LOGGER.info("Pharmacy {} updated by user {}", dto.id(),
                    updaterUserId);
            return Mapper.mapToPharmacyReadOnlyDTO(updatedPharmacy);

        } catch (Exception e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error updating pharmacy {}", dto.id() , e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public void deletePharmacy(Long id, Long deleterUserId) throws EntityNotFoundException, EntityNotAuthorizedException {

        try {
            JPAHelper.beginTransaction();

            Pharmacy pharmacy = pharmacyDAO.getByIdWithRelations(id, true,
                            true, true)
                    .orElseThrow(() -> new EntityNotFoundException("Pharmacy", id + " not found"));

            // Check if user owns the pharmacy or is Admin
            if (!pharmacy.getUser().getId().equals(deleterUserId) && !userDAO.isAdmin(deleterUserId)) {
                throw new EntityNotAuthorizedException("User", "User not " +
                        "authorized to delete pharmacy with id=" + id);
            }

            //Removing pharmacy from its user
            User owner =
                    userDAO.getByIdWithRelations(pharmacy.getUser().getId(),
                            true,true, true).orElseThrow(() -> new EntityNotFoundException("User",
                            "User with id " + pharmacy.getUser().getId() + " " +
                                    "not found"));

            owner.removePharmacy(pharmacy);

            // Remove all contacts first
            if (pharmacy.getContactReferences() != null) {
                new ArrayList<>(pharmacy.getContactReferences()).forEach(pharmacy::removeContactReference);
            }

            if(pharmacy.getRecordsGiver() != null){
                new ArrayList<>(pharmacy.getRecordsGiver()).forEach(record -> record.setGiver(null));
            }

            if(pharmacy.getRecordsReceiver() != null){
                new ArrayList<>(pharmacy.getRecordsReceiver()).forEach(record -> record.setReceiver(null));
            }

            pharmacyDAO.delete(id);
            JPAHelper.commitTransaction();
            LOGGER.info("Pharmacy {} deleted by user {}", id, deleterUserId);

        } catch (Exception e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error deleting pharmacy {}", id, e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public boolean nameExists(String name) throws AppServerException {
        try {
            JPAHelper.beginTransaction();
            boolean exists = pharmacyDAO.existsByName(name);
            JPAHelper.commitTransaction();
            return exists;
        } catch (Exception e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error checking pharmacy name existence", e);
            throw new AppServerException("Pharmacy",
                    "Error checking pharmacy name");
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public PharmacyReadOnlyDTO getPharmacyById(Long id) throws EntityNotFoundException {
        try {
            JPAHelper.beginTransaction();
            Pharmacy pharmacy = pharmacyDAO.getById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Pharmacy", id + " not found"));
            return Mapper.mapToPharmacyReadOnlyDTO(pharmacy);
        } catch (Exception e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error fetching pharmacy {}", id, e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public PharmacyReadOnlyDTO getPharmacyByName(String name) throws EntityNotFoundException, AppServerException {
        try {
            JPAHelper.beginTransaction();
            Pharmacy pharmacy = pharmacyDAO.findByField("name", name)
                    .orElseThrow(() -> new EntityNotFoundException("Pharmacy", "Name " + name + " not found"));
            return Mapper.mapToPharmacyReadOnlyDTO(pharmacy);
        } catch (Exception e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error fetching pharmacy by name {}", name, e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public List<PharmacyReadOnlyDTO> searchPharmaciesByName(String name) throws AppServerException {
        try {
            JPAHelper.beginTransaction();
            List<Pharmacy> pharmacies = pharmacyDAO.getByCriteria(Map.of("name", name));
            return Mapper.pharmaciesToReadOnlyDTOs(pharmacies);
        } catch (Exception e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error searching pharmacies by name {}", name, e);
            throw new AppServerException("Pharmacy",
                    "Error searching pharmacies");
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public List<PharmacyReadOnlyDTO> searchPharmaciesByUser(String username) throws AppServerException {
            try {
                JPAHelper.beginTransaction();
                List<Pharmacy> pharmacies = pharmacyDAO.getByCriteria(Map.of("user.username", username));
                return Mapper.pharmaciesToReadOnlyDTOs(pharmacies);
            } catch (Exception e) {
                JPAHelper.rollbackTransaction();
                LOGGER.error("Error searching pharmacies by user {}", username, e);
                throw new AppServerException("Pharmacy","Error searching " +
                        "pharmacies");
            } finally {
                JPAHelper.closeEntityManager();
            }
    }

    @Override
    public List<PharmacyReadOnlyDTO> getAllPharmacies() throws AppServerException {
        try {
            JPAHelper.beginTransaction();
            List<Pharmacy> pharmacies = pharmacyDAO.getAll();
            return Mapper.pharmaciesToReadOnlyDTOs(pharmacies);
        } catch (Exception e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error fetching all pharmacies", e);
            throw new AppServerException("Pharmacy","Error fetching " +
                    "pharmacies");
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

}
