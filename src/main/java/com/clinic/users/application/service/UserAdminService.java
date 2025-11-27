package com.clinic.users.application.service;

import com.clinic.users.application.port.in.UserAdminUseCase;
import com.clinic.users.application.port.in.UserQueryUseCase;
import com.clinic.users.application.port.out.CognitoGateway;
import com.clinic.users.domain.exception.DomainException;
import com.clinic.users.domain.model.User;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAdminService implements UserAdminUseCase, UserQueryUseCase {

    private final CognitoGateway gateway;

    private static final DateTimeFormatter BIRTHDATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9]{1,15}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\d{1,10}$");

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");

    @Override
    public User createUser(User user, boolean sendInvite) {
        if (user == null) {
            throw new DomainException("User is required");
        }

        log.info("Creating user {}", user.getUsername());

        validateNewUser(user);

        User created = gateway.adminCreate(user, sendInvite);
        log.info("User {} created in IdP", created.getUsername());
        return created;
    }

    @Override
    public void disableUser(String username) {
        log.info("Disabling user {}", username);
        gateway.adminDisable(username);
    }

    @Override
    public void enableUser(String username) {
        log.info("Enabling user {}", username);
        gateway.adminEnable(username);
    }

    @Override
    public void setPermanentPassword(String username, String password) {
        log.info("Setting permanent password for {}", username);

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new DomainException(
                    "Password does not meet complexity requirements");
        }

        gateway.adminSetPassword(username, password, true);
    }

    @Override
    public void addUserToGroups(String username, List<String> groups) {
        if (groups == null || groups.isEmpty()) {
            log.info("No groups provided for user {}, skipping", username);
            return;
        }
        log.info("Adding user {} to groups {}", username, groups);
        gateway.adminAddToGroups(username, groups);
    }


    @Override
    public User findByUsername(String username) {
        log.info("Fetching user: {}", username);
        return gateway.adminGet(username);
    }

    @Override
    public List<User> list(int limit, String filter) {
        log.info("Listing users. limit={}, filter={}", limit, filter);
        return gateway.listUsers(limit, filter);
    }


    private void validateNewUser(User user) {

        if (!USERNAME_PATTERN.matcher(user.getUsername()).matches()) {
            throw new DomainException(
                    "Username must be alphanumeric and up to 15 characters");
        }

        Map<String, String> attrs = Objects.requireNonNull(
                user.getAttributes(),
                "Attributes map is required");

        String document = attrs.get("custom_document");
        String email = attrs.get("email");
        String phone = attrs.get("phone_number");
        String address = attrs.get("address");
        String birthdate = attrs.get("birthdate");
        String password = attrs.get("raw_password");

        if (document == null || document.isBlank()) {
            throw new DomainException("Document (cédula) is required");
        }

        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new DomainException("Email is not valid");
        }

        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new DomainException("Phone must have 1 to 10 digits");
        }

        if (address == null || address.length() > 30) {
            throw new DomainException("Address must be <= 30 characters");
        }

        validateBirthdate(birthdate);

        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new DomainException(
                    "Password must have at least 8 chars, 1 uppercase, 1 number, 1 special char");
        }
    }

    public void validateUpdatableData(Map<String, String> attrs) {
        if (attrs == null) {
            return;
        }

        String email = attrs.get("email");
        if (email != null && (!email.contains("@") || !email.contains("."))) {
            throw new DomainException("Email is not valid");
        }

        String phone = attrs.get("phone_number");
        if (phone != null && !PHONE_PATTERN.matcher(phone).matches()) {
            throw new DomainException("Phone must have 1 to 10 digits");
        }

        String address = attrs.get("address");
        if (address != null && address.length() > 30) {
            throw new DomainException("Address must be <= 30 characters");
        }

        String birthdate = attrs.get("birthdate");
        if (birthdate != null) {
            validateBirthdate(birthdate);
        }
    }

    private void validateBirthdate(String birthdate) {
        if (birthdate == null) {
            throw new DomainException("Birthdate is required");
        }
        try {
            LocalDate date = LocalDate.parse(birthdate, BIRTHDATE_FORMAT);
            long years = ChronoUnit.YEARS.between(date, LocalDate.now());
            if (years < 0 || years > 150) {
                throw new DomainException("Age must be between 0 and 150 years");
            }
        } catch (DateTimeParseException e) {
            throw new DomainException(
                    "Birthdate must be in format DD/MM/YYYY", e);
        }
    }
}
