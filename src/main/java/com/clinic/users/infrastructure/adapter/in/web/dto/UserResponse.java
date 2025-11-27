package com.clinic.users.infrastructure.adapter.in.web.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserResponse {

    String username;
    Boolean enabled;
    Map<String, String> attributes;
}
