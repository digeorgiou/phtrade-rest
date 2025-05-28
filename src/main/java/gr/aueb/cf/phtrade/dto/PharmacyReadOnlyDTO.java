package gr.aueb.cf.phtrade.dto;

import java.time.LocalDateTime;

public record PharmacyReadOnlyDTO(

        Long id,
        LocalDateTime createdAt,
        String ownerUsername
) {}
