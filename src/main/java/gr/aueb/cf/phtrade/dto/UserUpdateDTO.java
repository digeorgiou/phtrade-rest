package gr.aueb.cf.phtrade.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateDTO(
        Long id,
        @NotBlank(message = "Παρακαλώ εισάγεται username")
        @Size(min = 4, max = 55,
                message = "Το username πρέπει να έχει 4 ως 55 χαρακτήρες")
        String username,
        @NotBlank(message = "Παρακαλώ εισάγετε κωδικό")
        @Size(min = 4, max = 30,
                message = "Ο κωδικός πρέπει να έχει 4 ως 30 χαρακτήρες")
        String password,
        @NotBlank(message = "Παρακαλώ εισάγετε κωδικό")
        @Size(min = 4, max = 30,
                message = "Ο κωδικός πρέπει να έχει 4 ως 30 χαρακτήρες")
        String confirmedPassword,
        @NotBlank(message = "Το email δεν μπορεί να ειναι κενό")
        @Email(message = "Παρακαλώ εισάγετε έγκυρο email")
        String email
) {}
