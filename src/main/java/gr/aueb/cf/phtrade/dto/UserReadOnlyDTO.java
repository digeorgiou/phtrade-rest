package gr.aueb.cf.phtrade.dto;

public record UserReadOnlyDTO(
        Long id,
        String username,
        String password,
        String email,
        String role
) {}
