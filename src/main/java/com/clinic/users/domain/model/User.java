package com.clinic.users.domain.model;

import lombok.Builder;
import lombok.Data;


import java.util.List;
import java.util.Map;

@Data
@Builder(toBuilder = true)
public class User {
    private String username;
    private Boolean enabled;
    private Map<String, String> attributes;
    private List<String> groups;
}
