package gr.aueb.cf.phtrade.service;

import gr.aueb.cf.phtrade.core.exceptions.AppServerException;
import gr.aueb.cf.phtrade.core.exceptions.EntityNotAuthorizedException;
import gr.aueb.cf.phtrade.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.phtrade.dto.TradeRecordInsertDTO;
import gr.aueb.cf.phtrade.dto.TradeRecordReadOnlyDTO;
import gr.aueb.cf.phtrade.dto.TradeRecordUpdateDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ITradeRecordService {

    TradeRecordReadOnlyDTO create(TradeRecordInsertDTO dto) throws EntityNotAuthorizedException, EntityNotFoundException, AppServerException;
    TradeRecordReadOnlyDTO update(TradeRecordUpdateDTO dto)throws EntityNotFoundException, EntityNotAuthorizedException, AppServerException;
    void delete(Long id, Long deleterUserId, Boolean isGiverSide) throws EntityNotFoundException, EntityNotAuthorizedException;
    TradeRecordReadOnlyDTO getById(Long id) throws EntityNotFoundException;
    List<TradeRecordReadOnlyDTO> getAll() throws AppServerException;
    List<TradeRecordReadOnlyDTO> getRecentTradesForPharmacy(Long pharmacyId,
                                                            int limit) throws EntityNotFoundException;
    List<TradeRecordReadOnlyDTO> getTradesBetweenPharmacies(
            Long pharmacy1Id, Long pharmacy2Id, LocalDateTime startDate,
            LocalDateTime endDate) throws EntityNotFoundException;
    Double calculateBalanceBetweenPharmacies(Long pharmacy1Id,
                                             Long pharmacy2Id) throws EntityNotFoundException;
    Integer getTradeCountBetweenPharmacies(Long pharmacy1Id, Long pharmacy2Id) throws EntityNotFoundException;
    List<TradeRecordReadOnlyDTO> getRecentTradesBetweenPharmacies(Long pharmacy1Id,
                                             Long pharmacy2Id, int limit) throws EntityNotFoundException;

}
