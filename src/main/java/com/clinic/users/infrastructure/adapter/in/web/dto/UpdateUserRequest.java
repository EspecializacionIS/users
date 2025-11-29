package com.clinic.users.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Email
    private String email;

    @Pattern(
            regexp = "^\\+[0-9]{1,15}$",
            message = "Phone must start with + and contain digits only")

    private String phone;

    @Size(max = 30)
    private String address;

    @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$",
            message = "Birthdate must be in format DD/MM/YYYY")
    private String birthdate;
}

