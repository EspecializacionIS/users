package com.clinic.users.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;


@Data
public class AddToGroupRequest {

    @NotBlank(message = "Username must not be blank")
    private String username;

    @NotEmpty(message = "Groups list must not be empty")
    private List<String> groups;
}
