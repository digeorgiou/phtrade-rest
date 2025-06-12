package gr.aueb.cf.phtrade.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record TradeRecordReadOnlyDTO(

        Long id,
        String description,
        Double amount,
        String giverName,
        String receiverName,
        String recorderUsername,
        String lastModifiedByUsername,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime transactionDate,
        Boolean deletedByGiver,
        Boolean deletedByReceiver
) {}
