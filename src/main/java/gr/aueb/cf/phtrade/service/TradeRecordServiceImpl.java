package gr.aueb.cf.phtrade.service;

import gr.aueb.cf.phtrade.core.exceptions.AppServerException;
import gr.aueb.cf.phtrade.core.exceptions.EntityNotAuthorizedException;
import gr.aueb.cf.phtrade.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.phtrade.dao.IPharmacyDAO;
import gr.aueb.cf.phtrade.dao.ITradeRecordDAO;
import gr.aueb.cf.phtrade.dao.IUserDAO;
import gr.aueb.cf.phtrade.dto.TradeRecordInsertDTO;
import gr.aueb.cf.phtrade.dto.TradeRecordReadOnlyDTO;
import gr.aueb.cf.phtrade.dto.TradeRecordUpdateDTO;
import gr.aueb.cf.phtrade.mapper.Mapper;
import gr.aueb.cf.phtrade.model.Pharmacy;
import gr.aueb.cf.phtrade.model.TradeRecord;
import gr.aueb.cf.phtrade.model.User;
import gr.aueb.cf.phtrade.service.util.JPAHelper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class TradeRecordServiceImpl implements ITradeRecordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TradeRecordServiceImpl.class);
    private final ITradeRecordDAO tradeRecordDAO;
    private final IPharmacyDAO pharmacyDAO;
    private final IUserDAO userDAO;

    @Inject
    public TradeRecordServiceImpl(ITradeRecordDAO tradeRecordDAO,
                                  IPharmacyDAO pharmacyDAO,
                                  IUserDAO userDAO) {
        this.tradeRecordDAO = tradeRecordDAO;
        this.pharmacyDAO = pharmacyDAO;
        this.userDAO = userDAO;
    }

    @Override
    public TradeRecordReadOnlyDTO create(TradeRecordInsertDTO dto) throws EntityNotAuthorizedException, EntityNotFoundException, AppServerException{
        try{
            JPAHelper.beginTransaction();

            User recorderUser =
                    userDAO.getById(dto.recorderUserId()).orElseThrow(()-> new EntityNotFoundException("User",
                    "User with id " + dto.recorderUserId() + " was not found"));

            Pharmacy giver = pharmacyDAO.getById(dto.giverPharmacyId()).orElseThrow(()-> new EntityNotFoundException("Pharmacy",
                    "Pharmacy with id " + dto.giverPharmacyId() + " was not " +
                            "found"));

            Pharmacy receiver = pharmacyDAO.getById(dto.receiverPharmacyId()).orElseThrow(()-> new EntityNotFoundException("Pharmacy",
                    "Pharmacy with id " + dto.giverPharmacyId() + " was not " +
                            "found"));

            boolean isGiverUser =
                    giver.getUser() != null && giver.getUser().getId().equals(dto.recorderUserId());
            boolean isReceiverUser =
                    receiver.getUser() != null && receiver.getUser().getId().equals(dto.recorderUserId());

            if(!userDAO.isAdmin(dto.recorderUserId()) && !isGiverUser && !isReceiverUser){
                throw new EntityNotAuthorizedException("User", "User with " +
                        "id=" + dto.recorderUserId() + "not authorized to " +
                        "create this trade record");
            }

            TradeRecord record = Mapper.mapTradeRecordInsertToModel(dto);

            giver.addRecordGiver(record);
            receiver.addRecordReceiver(record);
            recorderUser.addRecordRecorder(record);
            record.setLastModifiedBy(recorderUser);

            pharmacyDAO.update(giver);
            pharmacyDAO.update(receiver);
            userDAO.update(recorderUser);

            TradeRecordReadOnlyDTO recordDTO =
                    tradeRecordDAO.insert(record)
                            .map(Mapper::mapToTradeRecordReadOnlyDTO)
                            .orElseThrow
                            (()-> new AppServerException("TradeRecord", "Failed to create Trade Record"));

            JPAHelper.commitTransaction();
            LOGGER.info("TradeRecord with description={}, amount ={} was " +
                    "created", dto.description(), dto.amount());
            return recordDTO;

        } catch( EntityNotFoundException | EntityNotAuthorizedException| AppServerException e){
            JPAHelper.rollbackTransaction();
            LOGGER.error("TradeRecord with description={}, amount ={} was not" +
                    " created", dto.description(), dto.amount(), e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public TradeRecordReadOnlyDTO update(TradeRecordUpdateDTO dto) throws EntityNotFoundException, EntityNotAuthorizedException, AppServerException{
        try{
            JPAHelper.beginTransaction();

            TradeRecord existingRecord = tradeRecordDAO.getById(dto.id())
                    .orElseThrow(()-> new EntityNotFoundException("TradeRecord",
                            "TradeRecord with id " + dto.id() + " was not " +
                                    "found"));

            User updaterUser =
                    userDAO.getById(dto.updaterUserId()).orElseThrow(()-> new EntityNotFoundException("User",
                    "User with id " + dto.updaterUserId() + " was not found"));

            // Verify updater is giver, receiver, or admin
            boolean isAdmin = userDAO.isAdmin(dto.updaterUserId());
            boolean isGiverUser = existingRecord.getGiver().getUser() != null &&
                    existingRecord.getGiver().getUser().getId().equals(dto.updaterUserId());
            boolean isReceiverUser =
                    existingRecord.getReceiver().getUser() != null &&
                    existingRecord.getReceiver().getUser().getId().equals(dto.updaterUserId());

            if (!isAdmin && !isGiverUser && !isReceiverUser) {
                throw new EntityNotAuthorizedException("User", "Only giver, " +
                        "receiver,  or admin can " +
                        "update records");
            }

            TradeRecord updatedRecord =
                    Mapper.mapTradeRecordUpdateToModel(dto, existingRecord);

            TradeRecordReadOnlyDTO recordReadOnlyDTO = tradeRecordDAO.update(updatedRecord)
                    .map(Mapper::mapToTradeRecordReadOnlyDTO)
                    .orElseThrow(()-> new AppServerException("TradeRecord",
                            "Failed to update Trade Record"));

            JPAHelper.commitTransaction();
            LOGGER.info("TradeRecord with id={} " +
                    "updated by user with id={}", dto.id(), dto.updaterUserId());
            return recordReadOnlyDTO;

        }catch (EntityNotFoundException | EntityNotAuthorizedException | AppServerException e){
            JPAHelper.rollbackTransaction();
            LOGGER.error("TradeRecord with id={} was not" +
                    " updated", dto.id(), e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public void delete(Long id, Long deleterUserId) throws EntityNotFoundException, EntityNotAuthorizedException{
        try{
            JPAHelper.beginTransaction();
            TradeRecord record = tradeRecordDAO.getById(id).orElseThrow(()-> new EntityNotFoundException("TradeRecord",
                    "TradeRecord with id " + id + " was not " +
                            "found"));

            User deleterUser = userDAO.getById(deleterUserId).orElseThrow(()-> new EntityNotFoundException("User",
                    "User with id " + deleterUserId + " was not found"));

            // Verify deleter is giver or receiver
            boolean isGiverUser = record.getGiver().getUser() != null &&
                    record.getGiver().getUser().getId().equals(deleterUserId);
            boolean isReceiverUser =
                    record.getReceiver().getUser() != null &&
                            record.getReceiver().getUser().getId().equals(deleterUserId);

            if (!isGiverUser && !isReceiverUser) {
                throw new EntityNotAuthorizedException("User", "Only giver, " +
                        "or receiver can delete records");
            }

            // Two-phase deletion logic
            if (isGiverUser) {
                record.setDeletedByGiver(true);
            } else {
                record.setDeletedByReceiver(true);
            }

            // Check if both parties have marked for deletion
            if (record.isDeletedByGiver() && record.isDeletedByReceiver()) {
                // Actually delete the record
                tradeRecordDAO.delete(id);
            } else {
                // Just update the deletion flags
                tradeRecordDAO.update(record);
            }

            JPAHelper.commitTransaction();
            LOGGER.info("TradeRecord with id={} " +
                    "deleted by user with id={}", id,
                    deleterUserId);

        } catch(EntityNotFoundException | EntityNotAuthorizedException e){
            JPAHelper.rollbackTransaction();
            LOGGER.error("TradeRecord with id={} was not" +
                    " deleted", id, e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public TradeRecordReadOnlyDTO getById(Long id) throws EntityNotFoundException{
        try{
            JPAHelper.beginTransaction();
            TradeRecordReadOnlyDTO dto =  tradeRecordDAO.getById(id)
                    .map(Mapper::mapToTradeRecordReadOnlyDTO)
                    .orElseThrow(()-> new EntityNotFoundException("TradeRecord",
                            "TradeRecord with id " + id + " was not " +
                                    "found"));
            JPAHelper.commitTransaction();
            return dto;
        } catch (EntityNotFoundException e){
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error fetching TradeRecord with id={}",id, e);
            throw e;
        } finally{
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public List<TradeRecordReadOnlyDTO> getAll() throws AppServerException{
        try{
            JPAHelper.beginTransaction();
            List<TradeRecord> tradeRecords = tradeRecordDAO
                    .getAll();
            JPAHelper.commitTransaction();
            return Mapper.tradeRecordsToReadOnlyDTOs(tradeRecords);
        } catch (Exception e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error fetching all trade records", e);
            throw new AppServerException("TradeRecord","Error fetching " +
                    "trade records");
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public List<TradeRecordReadOnlyDTO> getRecentTradesForPharmacy(Long pharmacyId, int limit) throws EntityNotFoundException{

        try {
            JPAHelper.beginTransaction();

            // Validate pharmacy exists
            pharmacyDAO.getById(pharmacyId)
                    .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                            "Pharmacy with id " + pharmacyId + " was not found"));

            // Create criteria map for pagination
            Map<String, Object> criteria = new HashMap<>();
            criteria.put("giver.id", pharmacyId);
            criteria.put("receiver.id", pharmacyId);

            // Get records where pharmacy is either giver or receiver, ordered by transaction date
            List<TradeRecord> tradeRecords = tradeRecordDAO.getByCriteriaPaginated(
                    TradeRecord.class,
                    criteria,
                    0, // page
                    limit
            );

            tradeRecords.sort((r1, r2) -> r2.getTransactionDate().compareTo(r1.getTransactionDate()));

            JPAHelper.commitTransaction();
            return Mapper.tradeRecordsToReadOnlyDTOs(tradeRecords);

        } catch (EntityNotFoundException e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error fetching recent trades for pharmacy with id={}", pharmacyId, e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }

    }

    @Override
    public List<TradeRecordReadOnlyDTO> getTradesBetweenPharmacies(Long pharmacy1Id, Long pharmacy2Id,
                                                                   LocalDateTime startDate, LocalDateTime endDate) throws EntityNotFoundException{

        try {
            JPAHelper.beginTransaction();

            // Validate both pharmacies exist
            pharmacyDAO.getById(pharmacy1Id)
                    .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                            "Pharmacy with id " + pharmacy1Id + " was not found"));
            pharmacyDAO.getById(pharmacy2Id)
                    .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                            "Pharmacy with id " + pharmacy2Id + " was not found"));

            // Create criteria for both directions with date range
            Map<String, Object> criteria1 = Map.of(
                    "giver.id", pharmacy1Id,
                    "receiver.id", pharmacy2Id,
                    "transactionDate", Map.of("from", startDate, "to", endDate)
            );

            Map<String, Object> criteria2 = Map.of(
                    "giver.id", pharmacy2Id,
                    "receiver.id", pharmacy1Id,
                    "transactionDate", Map.of("from", startDate, "to", endDate)
            );

            // Get records in both directions
            List<TradeRecord> direction1 = tradeRecordDAO.getByCriteria(
                    TradeRecord.class,
                    criteria1
            );

            List<TradeRecord> direction2 = tradeRecordDAO.getByCriteria(
                    TradeRecord.class,
                    criteria2
            );

            // Combine results
            List<TradeRecord> allRecords = new ArrayList<>();
            allRecords.addAll(direction1);
            allRecords.addAll(direction2);

            // Sort by transaction date (newest first)
            allRecords.sort((r1, r2) -> r2.getTransactionDate().compareTo(r1.getTransactionDate()));

            JPAHelper.commitTransaction();
            return Mapper.tradeRecordsToReadOnlyDTOs(allRecords);

        } catch (EntityNotFoundException e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error fetching trades between pharmacies {} and {}", pharmacy1Id, pharmacy2Id, e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }

    }

    @Override
    public Double calculateBalanceBetweenPharmacies(Long pharmacy1Id, Long pharmacy2Id) throws EntityNotFoundException{

        try {
            JPAHelper.beginTransaction();

            // Validate both pharmacies exist
            pharmacyDAO.getById(pharmacy1Id)
                    .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                            "Pharmacy with id " + pharmacy1Id + " was not found"));
            pharmacyDAO.getById(pharmacy2Id)
                    .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                            "Pharmacy with id " + pharmacy2Id + " was not found"));

            // Get all trades where pharmacy1 is the giver
            List<TradeRecord> pharmacy1AsGiver = tradeRecordDAO.getByCriteria(TradeRecord.class,
                    Map.of("giver.id", pharmacy1Id, "receiver.id", pharmacy2Id));

            // Get all trades where pharmacy1 is the receiver
            List<TradeRecord> pharmacy1AsReceiver = tradeRecordDAO.getByCriteria(TradeRecord.class,
                    Map.of("giver.id", pharmacy2Id, "receiver.id", pharmacy1Id));

            // Calculate total given by pharmacy1 to pharmacy2
            double given = pharmacy1AsGiver.stream()
                    .mapToDouble(TradeRecord::getAmount)
                    .sum();

            // Calculate total received by pharmacy1 from pharmacy2
            double received = pharmacy1AsReceiver.stream()
                    .mapToDouble(TradeRecord::getAmount)
                    .sum();

            // Balance is (received - given)
            double balance = received - given;

            JPAHelper.commitTransaction();
            return balance;

        } catch (EntityNotFoundException e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error calculating balance between pharmacies {} and {}", pharmacy1Id, pharmacy2Id, e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }

    }

    @Override
    public Integer getTradeCountBetweenPharmacies(Long pharmacy1Id, Long pharmacy2Id) throws EntityNotFoundException{

        try {
            JPAHelper.beginTransaction();

            // Validate both pharmacies exist
            pharmacyDAO.getById(pharmacy1Id)
                    .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                            "Pharmacy with id " + pharmacy1Id + " was not found"));
            pharmacyDAO.getById(pharmacy2Id)
                    .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                            "Pharmacy with id " + pharmacy2Id + " was not found"));

            // Get count in both directions
            long count1 = tradeRecordDAO.getCountByCriteria(Map.of(
                    "giver.id", pharmacy1Id,
                    "receiver.id", pharmacy2Id
            ));

            long count2 = tradeRecordDAO.getCountByCriteria(Map.of(
                    "giver.id", pharmacy2Id,
                    "receiver.id", pharmacy1Id
            ));

            JPAHelper.commitTransaction();
            return (int)(count1 + count2);

        } catch (EntityNotFoundException e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error counting trades between pharmacies {} and {}", pharmacy1Id, pharmacy2Id, e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }

    }

    @Override
    public List<TradeRecordReadOnlyDTO> getRecentTradesBetweenPharmacies(Long pharmacy1Id, Long pharmacy2Id, int limit) throws EntityNotFoundException{

        try {
            JPAHelper.beginTransaction();

            // Validate both pharmacies exist
            pharmacyDAO.getById(pharmacy1Id)
                    .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                            "Pharmacy with id " + pharmacy1Id + " was not found"));
            pharmacyDAO.getById(pharmacy2Id)
                    .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                            "Pharmacy with id " + pharmacy2Id + " was not found"));

            // Get trades in both directions
            List<TradeRecord> trades1 = tradeRecordDAO.getByCriteriaPaginated(
                    TradeRecord.class,
                    Map.of("giver.id", pharmacy1Id, "receiver.id", pharmacy2Id),
                    0,
                    limit
            );

            List<TradeRecord> trades2 = tradeRecordDAO.getByCriteriaPaginated(
                    TradeRecord.class,
                    Map.of("giver.id", pharmacy2Id, "receiver.id", pharmacy1Id),
                    0,
                    limit
            );

            // Combine and sort by date
            List<TradeRecord> allTrades = new ArrayList<>();
            allTrades.addAll(trades1);
            allTrades.addAll(trades2);

            allTrades.sort((r1, r2) -> r2.getTransactionDate().compareTo(r1.getTransactionDate()));

            // Limit to requested number
            if (allTrades.size() > limit) {
                allTrades = allTrades.subList(0, limit);
            }

            JPAHelper.commitTransaction();
            return Mapper.tradeRecordsToReadOnlyDTOs(allTrades);

        } catch (EntityNotFoundException e) {
            JPAHelper.rollbackTransaction();
            LOGGER.error("Error fetching recent trades between pharmacies {} and {}", pharmacy1Id, pharmacy2Id, e);
            throw e;
        } finally {
            JPAHelper.closeEntityManager();
        }

    }

    @Override
    public List<TradeRecordReadOnlyDTO> getTradeRecordsByCriteria(Map<String, Object> criteria) {
        try{
            JPAHelper.beginTransaction();
            List<TradeRecordReadOnlyDTO> readOnlyDTOS =
                    tradeRecordDAO.getByCriteria(criteria)
                            .stream()
                            .map(Mapper::mapToTradeRecordReadOnlyDTO)
                            .collect(Collectors.toList());
            JPAHelper.commitTransaction();
            return readOnlyDTOS;
        } finally {
            JPAHelper.closeEntityManager();
        }

    }

    @Override
    public List<TradeRecordReadOnlyDTO> getTradeRecordsByCriteriaPaginated(Map<String, Object> criteria, Integer page, Integer size) {
        try{
            JPAHelper.beginTransaction();
            List<TradeRecordReadOnlyDTO> readOnlyDTOS =
                    tradeRecordDAO.getByCriteriaPaginated(TradeRecord.class,
                            criteria, page, size)
                            .stream()
                            .map(Mapper::mapToTradeRecordReadOnlyDTO)
                            .collect(Collectors.toList());
            JPAHelper.commitTransaction();
            return readOnlyDTOS;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }

    @Override
    public long getTradeRecordsCountByCriteria(Map<String, Object> criteria) {
        try{
            JPAHelper.beginTransaction();
            long count = tradeRecordDAO.getCountByCriteria(criteria);
            JPAHelper.commitTransaction();
            return count;
        } finally {
            JPAHelper.closeEntityManager();
        }
    }
}
