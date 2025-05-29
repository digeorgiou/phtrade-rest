package gr.aueb.cf.phtrade.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record TradeRecordInsertDTO(
        @NotBlank(message = "Παρακαλώ εισάγεται περιγραφή")
        @Size(min = 2, max = 255,
                message = "Η περιγραφή πρέπει να έχει 2 ως 255 χαρακτήρες")
        String description,

        @Positive(message = "Το ποσό πρέπει να είναι θετικός αριθμός")
        @NotBlank(message = "Το ποσό δεν μπορεί να είναι κενό")
        @Size(max = 10 ,
                message = "Το ποσό πρέπει να έχει μέχρι 10 χαρακτήρες")
        Double amount,

        @NotBlank(message = "Η ημερόμηνία δεν μπορεί να είναι κενή")
        @Past(message = "Η ημερομηνία δεν μπορεί να είναι μελλοντική")
        LocalDateTime transactionDate,

        Long giverPharmacyId,

        Long receiverPharmacyId,

        Long recorderUserid
){}
