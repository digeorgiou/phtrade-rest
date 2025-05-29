package gr.aueb.cf.phtrade.mapper;

import gr.aueb.cf.phtrade.core.enums.RoleType;
import gr.aueb.cf.phtrade.dto.*;
import gr.aueb.cf.phtrade.model.Pharmacy;
import gr.aueb.cf.phtrade.model.PharmacyContact;
import gr.aueb.cf.phtrade.model.TradeRecord;
import gr.aueb.cf.phtrade.model.User;


import java.util.List;
import java.util.stream.Collectors;

public class Mapper {


    public static Pharmacy mapPharmacyInsertToModel(PharmacyInsertDTO dto){

        return Pharmacy.builder()
                .name(dto.name())
                .build();

    }

    public static Pharmacy mapPharmacyUpdateToModel(PharmacyUpdateDTO dto,
                                                    Pharmacy existingPharmacy){

         existingPharmacy.setName(dto.name());
         return existingPharmacy;
    }

    public static PharmacyReadOnlyDTO mapToPharmacyReadOnlyDTO(Pharmacy pharmacy){

        return new PharmacyReadOnlyDTO(
                pharmacy.getId(),
                pharmacy.getCreatedAt(),
                pharmacy.getUser().getUsername()
                );

    }

    public static List<PharmacyReadOnlyDTO> pharmaciesToReadOnlyDTOs(List<Pharmacy> pharmacies){

        return pharmacies.stream()
                .map(Mapper::mapToPharmacyReadOnlyDTO)
                .collect(Collectors.toList());
    }

    public static PharmacyContact mapPharmacyContactInsertToModel(ContactInsertDTO dto){
        return PharmacyContact.builder()
                .contactName(dto.contactName())
                .build();
    }

    // Update DTO → Entity
    public static PharmacyContact mapPharmacyContactUpdateToModel(ContactUpdateDTO dto, PharmacyContact existingContact) {

        existingContact.setContactName(dto.contactName());
        return existingContact;

    }

    // Entity → ReadOnly DTO
    public static ContactReadOnlyDTO mapToPharmacyContactReadOnlyDTO(PharmacyContact pharmacyContact) {
        return new ContactReadOnlyDTO(
                pharmacyContact.getId(),
                pharmacyContact.getUser().getUsername(),
                pharmacyContact.getContactName(),
                pharmacyContact.getPharmacy().getName()
        );
    }

    // List of Entities → List of ReadOnly DTOs
    public static List<ContactReadOnlyDTO> pharmacyContactsToReadOnlyDTOs(List<PharmacyContact> pharmacyContacts) {
        return pharmacyContacts.stream()
                .map(Mapper::mapToPharmacyContactReadOnlyDTO)
                .collect(Collectors.toList());
    }

    // Insert DTO → Entity
    public static User mapUserInsertToModel(UserInsertDTO dto) {
        return User.builder()
                .username(dto.username())
                .password(dto.password())
                .email(dto.email())
                .roleType(RoleType.valueOf(dto.role()))
                .build();
    }

    // Update DTO → Entity
    public static User mapUserUpdateToModel(UserUpdateDTO dto,
                                            User existingUser) {
        existingUser.setEmail(dto.email());
        existingUser.setUsername(dto.username());
        existingUser.setPassword(dto.password());

        return existingUser;
    }

    // Entity → ReadOnly DTO
    public static UserReadOnlyDTO mapToUserReadOnlyDTO(User user) {

        return new UserReadOnlyDTO(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getRoleType().toString()
        );
    }

    // List of Entities → List of ReadOnly DTOs
    public static List<UserReadOnlyDTO> usersToReadOnlyDTOs(List<User> users) {

        return users.stream()
                .map(Mapper::mapToUserReadOnlyDTO)
                .collect(Collectors.toList());
    }


    // Insert DTO → Entity
    public static TradeRecord mapTradeRecordInsertToModel(TradeRecordInsertDTO dto) {

        return TradeRecord.builder()
                .description(dto.description())
                .amount(dto.amount())
                .build();
    }

    // Update DTO → Entity
    public static TradeRecord mapTradeRecordUpdateToModel(TradeRecordUpdateDTO dto, TradeRecord existingTradeRecord) {

        existingTradeRecord.setAmount(dto.amount());
        existingTradeRecord.setDescription(dto.description());
        existingTradeRecord.setTransactionDate(dto.transactionDate());

        return existingTradeRecord;

    }

    // Entity → ReadOnly DTO
    public static TradeRecordReadOnlyDTO mapToTradeRecordReadOnlyDTO(TradeRecord tradeRecord) {

        return new TradeRecordReadOnlyDTO(
                tradeRecord.getId(),
                tradeRecord.getDescription(),
                tradeRecord.getAmount(),
                tradeRecord.getGiver().getName(),
                tradeRecord.getReceiver().getName(),
                tradeRecord.getRecorder().getUsername(),
                tradeRecord.getLastModifiedBy().getUsername(),
                tradeRecord.getTransactionDate()
        );
    }

    // List of Entities → List of ReadOnly DTOs
    public static List<TradeRecordReadOnlyDTO> tradeRecordsToReadOnlyDTOs(List<TradeRecord> tradeRecords) {

        return tradeRecords.stream()
                .map(Mapper::mapToTradeRecordReadOnlyDTO)
                .collect(Collectors.toList());
    }

}
