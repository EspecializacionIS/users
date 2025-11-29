package com.clinic.users.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserGroupResponse {

    @JsonProperty("GroupName")
    String groupName;
}
