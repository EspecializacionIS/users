package com.clinic.users.infrastructure.adapter.out.cognito;

import com.clinic.users.domain.model.User;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

@Component
public class CognitoMapper {

    public List<AttributeType> toAttributes(Map<String, String> attrs) {
        if (attrs == null || attrs.isEmpty()) return List.of();
        return attrs.entrySet().stream()
                .map(e -> AttributeType.builder().name(e.getKey()).value(e.getValue()).build())
                .toList();
    }

    public User from(UserType userType) {
        return User.builder()
                .username(userType.username())
                .enabled(userType.enabled())
                .attributes(userType.attributes().stream()
                        .collect(Collectors.toMap(AttributeType::name, AttributeType::value)))
                .build();
    }

    public User from(AdminGetUserResponse resp) {
        return User.builder()
                .username(resp.username())
                .enabled(resp.enabled())
                .attributes(resp.userAttributes().stream()
                        .collect(Collectors.toMap(AttributeType::name, AttributeType::value)))
                .build();
    }
}
