package com.clinic.users.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserResponse {
    String username;
    Boolean enabled;
    Map<String, String> attributes;
    List<String> groups;
}

