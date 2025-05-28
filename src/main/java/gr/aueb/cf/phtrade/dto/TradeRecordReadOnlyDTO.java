package gr.aueb.cf.phtrade.dto;

import java.time.LocalDateTime;

public record TradeRecordReadOnlyDTO(

        Long id,
        String giverName,
        String receiverName,
        String recorderUsername,
        String lastModifiedByUsername,
        LocalDateTime transactionDate
) {}
