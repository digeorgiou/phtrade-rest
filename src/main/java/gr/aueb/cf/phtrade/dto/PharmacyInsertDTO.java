package gr.aueb.cf.phtrade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PharmacyInsertDTO(

        Long userId,
        @NotBlank(message = "Το Όνομα του φαρμακείου δεν μπορει να ειναι κενό")
        @Size(max = 55,message = "Το Όνομα του φαρμακείου πρέπει να ειναι " +
                "μεχρι 55 χαρακτήρες")
        String name
) {}
