package gr.aueb.cf.phtrade.dto;

import java.util.List;

public record BalanceDTO (

        String contactName,
        String pharmacyName,
        Long pharmacyId,
        double amount,
        List<TradeRecordReadOnlyDTO> recentTrades,
        Integer tradeCount
)
{}
