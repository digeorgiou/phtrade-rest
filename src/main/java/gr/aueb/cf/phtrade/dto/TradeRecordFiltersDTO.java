package gr.aueb.cf.phtrade.dto;

import gr.aueb.cf.phtrade.model.Pharmacy;
import gr.aueb.cf.phtrade.model.PharmacyContact;
import gr.aueb.cf.phtrade.model.User;

public record TradeRecordFiltersDTO(

        Pharmacy pharmacy,
        User user,
        PharmacyContact contact,
        String description,
        Double amount
){}
