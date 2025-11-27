package com.clinic.users.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class User {

    private String username;
    private Boolean enabled;
    private Map<String, String> attributes;


}