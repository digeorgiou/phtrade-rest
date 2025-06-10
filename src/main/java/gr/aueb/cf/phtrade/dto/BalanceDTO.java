package gr.aueb.cf.phtrade.dto;

import java.util.List;

public record BalanceDTO (

        String contactName,
        String pharmacyName,
        double balance,
        List<TradeRecordReadOnlyDTO> recentTrades,
        Integer tradeCount
)
{}
