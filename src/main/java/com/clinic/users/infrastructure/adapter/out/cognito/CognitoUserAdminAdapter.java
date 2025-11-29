package com.clinic.users.infrastructure.adapter.out.cognito;

import com.clinic.users.application.port.out.CognitoGateway;
import com.clinic.users.domain.exception.DomainException;
import com.clinic.users.domain.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.clinic.users.infrastructure.adapter.in.web.dto.CreateUserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListGroupsForUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GroupType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.MessageActionType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CognitoUserAdminAdapter implements CognitoGateway {

    private final CognitoIdentityProviderClient client;
    private final CognitoMapper mapper;

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    private DomainException wrap(String op, CognitoIdentityProviderException e) {
        String msg = (e.awsErrorDetails() != null)
                ? e.awsErrorDetails().errorMessage()
                : e.getMessage();

        log.error("Cognito operation {} failed: {}", op, msg);
        return new DomainException("Cognito error on " + op + ": " + msg, e);
    }

    @Override
    public User adminCreate(User user, boolean sendInvite) {
        try {
            List<AttributeType> attrs = mapper.toAttributes(user.getAttributes());

            AdminCreateUserRequest req = AdminCreateUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(user.getUsername())
                    .userAttributes(attrs) // <- ahora es List<AttributeType>
                    .messageAction(sendInvite ? MessageActionType.RESEND : MessageActionType.SUPPRESS)
                    .build();

            UserType created = client.adminCreateUser(req).user();
            return mapper.from(created);

        } catch (CognitoIdentityProviderException e) {
            throw wrap("adminCreate", e);
        }
    }


    @Override
    public void adminEnable(String username) {
        try {
            client.adminEnableUser(AdminEnableUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .build());
        } catch (CognitoIdentityProviderException e) {
            throw wrap("adminEnable", e);
        }
    }

    @Override
    public void adminDisable(String username) {
        try {
            client.adminDisableUser(AdminDisableUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .build());
        } catch (CognitoIdentityProviderException e) {
            throw wrap("adminDisable", e);
        }
    }

    @Override
    public void adminSetPassword(String username, String password, boolean permanent) {
        try {
            client.adminSetUserPassword(AdminSetUserPasswordRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .password(password)
                    .permanent(permanent)
                    .build());
        } catch (CognitoIdentityProviderException e) {
            throw wrap("adminSetPassword", e);
        }
    }

    @Override
    public void adminAddToGroups(String username, List<String> groups) {
        if (groups == null || groups.isEmpty()) return;

        for (String g : groups) {
            try {
                System.out.println("Asignando a grupo Cognito: " + g);
                client.adminAddUserToGroup(AdminAddUserToGroupRequest.builder()
                        .userPoolId(userPoolId)
                        .username(username)
                        .groupName(g)
                        .build());
            } catch (CognitoIdentityProviderException e) {
                throw wrap("adminAddUserToGroup(" + g + ")", e);
            }
        }
    }

    @Override
    public User adminGet(String username) {
        try {
            AdminGetUserResponse resp = client.adminGetUser(AdminGetUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .build());

            List<String> groups = loadGroups(username);

            return mapper.from(resp, groups);
        } catch (CognitoIdentityProviderException e) {
            throw wrap("adminGet", e);
        }
    }


    @Override
    public List<User> listUsers(int limit, String filter) {
        try {
            var req = ListUsersRequest.builder()
                    .userPoolId(userPoolId)
                    .limit(limit > 0 ? limit : 20)
                    .filter(filter)
                    .build();

            return client.listUsers(req).users().stream()
                    .map(userType -> {
                        List<String> groups = loadGroups(userType.username());
                        return mapper.from(userType, groups);
                    })
                    .toList();

        } catch (CognitoIdentityProviderException e) {
            throw wrap("listUsers", e);
        }
    }



    @Override
    public List<String> listGroupsForUser(String username) {
        try {
            var req = AdminListGroupsForUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .build();

            return client.adminListGroupsForUser(req)
                    .groups()
                    .stream()
                    .map(GroupType::groupName)
                    .toList();

        } catch (CognitoIdentityProviderException e) {
            throw wrap("adminListGroupsForUser", e);
        }
    }


    private User toDomain(CreateUserRequest req) {
        Map<String, String> attrs = new HashMap<>();

        // Atributos estándar de Cognito
        attrs.put("given_name", req.getFirstName());
        attrs.put("family_name", req.getLastName());
        attrs.put("email", req.getEmail());
        attrs.put("address", req.getAddress());
        attrs.put("birthdate", req.getBirthdate());
        attrs.put("phone_number", req.getPhone());

        // Custom attributes (deben existir en el schema del User Pool)
        attrs.put("custom:document", req.getDocument());   // ojo con el nombre en Cognito
        attrs.put("raw_password", req.getPassword());

        // 👉 Aquí enviamos el rol a Cognito como custom attribute
        attrs.put("custom:role", req.getRole().name());    // "MEDICO", "ENFERMERA", etc.

        // Opcional – si tu pool exige username = email:
        // attrs.put("preferred_username", req.getUsername());

        return User.builder()
                // Si Cognito está configurado para usar email como username:
                .username(req.getEmail())
                // Si NO, y te permite username normal:
                // .username(req.getUsername())
                .enabled(true)
                .attributes(attrs)
                // Si todavía usas grupos de Cognito:
                // .groups(List.of(req.getRole().getIamName()))
                .build();
    }


    private List<String> loadGroups(String username) {
        var req = AdminListGroupsForUserRequest.builder()
                .userPoolId(userPoolId)
                .username(username)
                .build();

        return client.adminListGroupsForUser(req)
                .groups()
                .stream()
                .map(GroupType::groupName) 
                .toList();
    }


}
