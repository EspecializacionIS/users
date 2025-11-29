package com.clinic.users.infrastructure.adapter.out.cognito;

import com.clinic.users.domain.model.User;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;
import java.util.Collections;

@Component
public class CognitoMapper {


    public User from(UserType userType, List<String> groups) {
        return User.builder()
                .username(userType.username())
                .enabled(userType.enabled())
                .attributes(userType.attributes().stream()
                        .collect(Collectors.toMap(AttributeType::name, AttributeType::value)))
                .groups(groups)
                .build();
    }

    public List<AttributeType> toAttributes(Map<String, String> attrs) {
        if (attrs == null) {
            return List.of();
        }

        return attrs.entrySet().stream()
                .map(e -> AttributeType.builder()
                        .name(e.getKey())
                        .value(e.getValue())
                        .build())
                .collect(Collectors.toList());
    }



    public User from(AdminGetUserResponse resp, List<String> groups) {
        return User.builder()
                .username(resp.username())
                .enabled(resp.enabled())
                .attributes(resp.userAttributes().stream()
                        .collect(Collectors.toMap(AttributeType::name, AttributeType::value)))
                .groups(groups)
                .build();
    }

    // Opcional: versiones antiguas delegan a las nuevas con lista vacía
    public User from(UserType userType) {
        return from(userType, Collections.emptyList());
    }

    public User from(AdminGetUserResponse resp) {
        return from(resp, Collections.emptyList());
    }

}
