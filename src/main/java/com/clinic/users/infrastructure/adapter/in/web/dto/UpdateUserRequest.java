package com.clinic.users.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Email
    private String email;

    @Pattern(regexp = "^\\d{1,10}$",
            message = "Phone must have 1 to 10 digits")
    private String phone;

    @Size(max = 30)
    private String address;

    @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$",
            message = "Birthdate must be in format DD/MM/YYYY")
    private String birthdate;
}

