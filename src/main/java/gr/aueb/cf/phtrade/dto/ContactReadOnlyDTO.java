package gr.aueb.cf.phtrade.dto;

public record ContactReadOnlyDTO(

        Long id,
        String username,
        String contactName,
        String pharmacyName
) {}
