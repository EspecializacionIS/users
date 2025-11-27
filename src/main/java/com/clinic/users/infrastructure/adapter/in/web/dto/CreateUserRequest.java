package com.clinic.users.infrastructure.adapter.in.web.dto;

import com.clinic.users.domain.model.Group;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserRequest {

    @NotBlank
    @Size(max = 15)
    @Pattern(regexp = "^[A-Za-z0-9]+$",
            message = "Username must be alphanumeric and up to 15 characters")
    private String username;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String document; // cédula

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^\\d{1,10}$",
            message = "Phone must have 1 to 10 digits")
    private String phone;

    @NotBlank
    @Size(max = 30)
    private String address;

    @NotBlank
    @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$",
            message = "Birthdate must be in format DD/MM/YYYY")
    private String birthdate;

    @NotBlank
    @Size(min = 8)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
            message = "Password must have at least 8 chars, 1 uppercase, 1 number and 1 special char")
    private String password;

    @NotNull
    private Group role;

    private boolean sendInvite;
}
