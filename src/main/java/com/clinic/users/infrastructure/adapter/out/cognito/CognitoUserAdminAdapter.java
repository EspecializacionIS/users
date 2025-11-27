package com.clinic.users.infrastructure.adapter.out.cognito;

import com.clinic.users.application.port.out.CognitoGateway;
import com.clinic.users.domain.exception.DomainException;
import com.clinic.users.domain.model.User;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDisableUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminEnableUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.MessageActionType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

@Slf4j
@Component
@RequiredArgsConstructor
public class CognitoUserAdminAdapter implements CognitoGateway {

    private final CognitoIdentityProviderClient client;
    private final CognitoMapper mapper;

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Override
    public User adminCreate(User user, boolean sendInvite) {
        try {
            var attrs = mapper.toAttributes(user.getAttributes());
            var req = AdminCreateUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(user.getUsername())
                    .userAttributes(attrs)
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
            return mapper.from(resp);
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

            return client.listUsers(req).users().stream().map(mapper::from).toList();

        } catch (CognitoIdentityProviderException e) {
            throw wrap("listUsers", e);
        }
    }

    private DomainException wrap(String op, CognitoIdentityProviderException e) {
        String msg = Objects.nonNull(e.awsErrorDetails())
                ? e.awsErrorDetails().errorMessage()
                : e.getMessage();

        log.error("Cognito operation {} failed: {}", op, msg);
        return new DomainException("Cognito error on " + op + ": " + msg, e);
    }
}
