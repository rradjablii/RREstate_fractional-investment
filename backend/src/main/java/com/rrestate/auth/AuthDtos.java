package com.rrestate.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public final class AuthDtos {
    private AuthDtos() {}

    public record LoginRequest(@NotBlank @Email @Size(max = 254) String email, @NotBlank @Size(max = 128) String password) {}

    public record RegisterRequest(
            @NotBlank @Size(max = 80) String firstName,
            @NotBlank @Size(max = 80) String lastName,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 12, max = 128, message = "must be between 12 and 128 characters") String password) {}

    public record UserResponse(UUID id, String email, String firstName, String lastName, List<Role> roles, KycStatus kycStatus) {
        static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(), u.getRoles().stream().sorted().toList(), u.getKycStatus());
        }
    }

    public record AuthResponse(String accessToken, long expiresInSeconds, UserResponse user) {}
}
