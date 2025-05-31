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
import java.util.stream.Collectors;

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
    public PharmacyReadOnlyDTO createPharmacy(PharmacyInsertDTO dto) throws EntityAlreadyExistsException, EntityNotFoundException, AppServerException {
        try {
            JPAHelper.beginTransaction();

            // Check if pharmacy name exists
            if (pharmacyDAO.existsByName(dto.name())) {
                throw new EntityAlreadyExistsException("Pharmacy", "Name " + dto.name() + " already exists");
            }
            // Get the creator user
            User creator = userDAO.getById(dto.userId())
                    .orElseThrow(() -> new EntityNotFoundException("User", "Creator not found"));

            // Create pharmacy entity
            Pharmacy pharmacy = Mapper.mapPharmacyInsertToModel(dto);

            // Add to user's pharmacies
            creator.addPharmacy(pharmacy);
            userDAO.update(creator);

            PharmacyReadOnlyDTO pharmacyDTO = pharmacyDAO.insert(pharmacy)
                    .map(Mapper::mapToPharmacyReadOnlyDTO)
                    .orElseThrow(() -> new AppServerException("Pharmacy", "Failed to create pharmacy"));

            JPAHelper.commitTransaction();
            LOGGER.info("Pharmacy created with ID: {}", pharmacyDTO.id());
            return pharmacyDTO;


        } catch (EntityNotFoundException | AppServerException | EntityAlreadyExistsException e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error creating pharmacy", e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public PharmacyReadOnlyDTO updatePharmacy(PharmacyUpdateDTO dto) throws EntityAlreadyExistsException,
            EntityNotAuthorizedException, EntityNotFoundException,
            AppServerException {
        try {
            JPAHelper.beginTransaction();

            // Verify pharmacy exists
            Pharmacy existingPharmacy = pharmacyDAO.getById(dto.id())
                    .orElseThrow(() -> new EntityNotFoundException("Pharmacy"
                            ,  + dto.id() + "not found"));

            if(!userDAO.isAdmin(dto.userId()) && !(Objects.equals(existingPharmacy.getUser().getId(), dto.userId()))){
                throw new EntityNotAuthorizedException("User", "User is not " +
                        "authorized to update Pharmacy with id=" + dto.id());
            }

            // Check name uniqueness if changed
            if (!existingPharmacy.getName().equals(dto.name()) && pharmacyDAO.existsByName(dto.name())) {
                throw new EntityAlreadyExistsException("Pharmacy", "Name " + dto.name() + " already exists");
            }

            // Update pharmacy
            Pharmacy updatedPharmacy = Mapper.mapPharmacyUpdateToModel(dto, existingPharmacy);
            PharmacyReadOnlyDTO readOnlyDTO=
                    pharmacyDAO.update(updatedPharmacy).map(Mapper::mapToPharmacyReadOnlyDTO).orElseThrow(() -> new AppServerException("Pharmacy", "Failed to update pharmacy"));

           JPAHelper.commitTransaction();
            LOGGER.info("Pharmacy {} updated by user {}", dto.id(),
                    dto.userId());
            return readOnlyDTO;

        } catch (EntityNotFoundException | EntityNotAuthorizedException | EntityAlreadyExistsException | AppServerException e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error updating pharmacy {}", dto.id() , e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public void deletePharmacy(Long id, Long deleterUserId) throws EntityNotFoundException {

        try {
            JPAHelper.beginTransaction();

            Pharmacy pharmacy = pharmacyDAO.getById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Pharmacy", id + " not found"));

            // Remove pharmacy from user
            if(pharmacy.getUser()!= null) {
                pharmacy.getUser().getPharmacies().remove(pharmacy);
            }
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

        } catch (EntityNotFoundException  e) {
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
            PharmacyReadOnlyDTO dto = pharmacyDAO.getById(id)
                    .map(Mapper::mapToPharmacyReadOnlyDTO)
                    .orElseThrow(() -> new EntityNotFoundException("Pharmacy", id + " not found"));
            JPAHelper.commitTransaction();
            return dto;
        } catch (EntityNotFoundException e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error fetching pharmacy {}", id, e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public List<PharmacyReadOnlyDTO> getPharmaciesByCriteria(Map<String,
            Object> criteria) {
        try {
            JPAHelper.beginTransaction();
            List<PharmacyReadOnlyDTO> readOnlyDTOS =
                    pharmacyDAO.getByCriteria(criteria)
                            .stream()
                            .map(Mapper::mapToPharmacyReadOnlyDTO)
                            .collect(Collectors.toList());

            JPAHelper.commitTransaction();
            return readOnlyDTOS;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public List<PharmacyReadOnlyDTO> getPharmaciesByCriteriaPaginated(Map<String, Object> criteria, Integer page, Integer size){
        try{
            JPAHelper.beginTransaction();
            List<PharmacyReadOnlyDTO> readOnlyDTOS =
                    pharmacyDAO.getByCriteriaPaginated(Pharmacy.class,
                            criteria, page, size)
                            .stream()
                            .map(Mapper::mapToPharmacyReadOnlyDTO)
                            .collect(Collectors.toList());
            JPAHelper.commitTransaction();
            return readOnlyDTOS;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public long getPharmaciesCountByCriteria(Map<String, Object> criteria){
        try{
            JPAHelper.beginTransaction();
            long count = pharmacyDAO.getCountByCriteria(criteria);
            JPAHelper.commitTransaction();
            return count;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public PharmacyReadOnlyDTO getPharmacyByName(String name) throws EntityNotFoundException {
        try {
            JPAHelper.beginTransaction();
            PharmacyReadOnlyDTO dto = pharmacyDAO.findByField("name", name)
                    .map(Mapper::mapToPharmacyReadOnlyDTO)
                    .orElseThrow(() -> new EntityNotFoundException("Pharmacy", "Name " + name + " not found"));
            JPAHelper.commitTransaction();
            return dto;
        } catch (EntityNotFoundException e) {
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
            List<Pharmacy> pharmacies = pharmacyDAO.getByCriteria( Map.of("name", "%" + name + "%"));
            JPAHelper.commitTransaction();
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
                List<Pharmacy> pharmacies = pharmacyDAO.getByCriteria(Map.of("user.username", username.toLowerCase() + "%"));
                JPAHelper.commitTransaction();
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
            JPAHelper.commitTransaction();
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
