package gr.aueb.cf.phtrade.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record PharmacyReadOnlyDTO(

        Long id,
        String name,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        String ownerUsername
) {}
