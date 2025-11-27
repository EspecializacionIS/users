package com.clinic.users.application.service;

import com.clinic.users.application.port.out.CognitoGateway;
import com.clinic.users.domain.exception.DomainException;
import com.clinic.users.domain.model.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private CognitoGateway gateway;

    private UserAdminService service;

    @BeforeEach
    void setUp() {
        service = new UserAdminService(gateway);
    }

    private Map<String, String> buildValidAttributes() {
        Map<String, String> attrs = new HashMap<>();
        attrs.put("custom_document", "123456789");
        attrs.put("email", "user@test.com");
        attrs.put("phone_number", "3001234567");
        attrs.put("address", "Calle 123");
        attrs.put("birthdate", "01/01/1990");
        attrs.put("raw_password", "Passw0rd!");
        return attrs;
    }

    private User buildValidUser() {
        return User.builder()
                .username("user123")
                .enabled(true)
                .attributes(buildValidAttributes())
                .build();
    }

    // ===== US-HR-01: Crear usuario y validaciones =====

    @Test
    void shouldCreateUserWhenDataIsValid() {
        User input = buildValidUser();

        when(gateway.adminCreate(any(User.class), eq(true)))
                .thenAnswer(inv -> inv.getArgument(0, User.class));

        User result = service.createUser(input, true);

        assertNotNull(result);
        assertEquals("user123", result.getUsername());
        verify(gateway).adminCreate(any(User.class), eq(true));
    }

    @Test
    void shouldThrowDomainExceptionWhenUserIsNull() {
        DomainException ex = assertThrows(
                DomainException.class,
                () -> service.createUser(null, true)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("user is required"));
        verify(gateway, never()).adminCreate(any(), anyBoolean());
    }

    @Test
    void shouldFailWhenUsernameIsInvalid() {
        User invalidUser = User.builder()
                .username("user con espacios!") // inválido
                .enabled(true)
                .attributes(buildValidAttributes())
                .build();

        DomainException ex = assertThrows(
                DomainException.class,
                () -> service.createUser(invalidUser, true)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("username"));
        verify(gateway, never()).adminCreate(any(), anyBoolean());
    }

    @Test
    void shouldFailWhenEmailIsInvalid() {
        User input = buildValidUser();
        input.getAttributes().put("email", "invalid-email");

        DomainException ex = assertThrows(
                DomainException.class,
                () -> service.createUser(input, true)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("email is not valid"));
        verify(gateway, never()).adminCreate(any(), anyBoolean());
    }

    @Test
    void shouldFailWhenPhoneIsInvalid() {
        User input = buildValidUser();
        input.getAttributes().put("phone_number", "1234567890123"); // >10 dígitos

        DomainException ex = assertThrows(
                DomainException.class,
                () -> service.createUser(input, true)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("phone must have 1 to 10 digits"));
        verify(gateway, never()).adminCreate(any(), anyBoolean());
    }

    @Test
    void shouldFailWhenAddressIsTooLong() {
        User input = buildValidUser();
        input.getAttributes().put(
                "address",
                "Esta dirección es demasiado larga para ser válida 123456789"
        );

        DomainException ex = assertThrows(
                DomainException.class,
                () -> service.createUser(input, true)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("address must be <= 30 characters"));
        verify(gateway, never()).adminCreate(any(), anyBoolean());
    }

    @Test
    void shouldFailWhenBirthdateIsMissing() {
        User input = buildValidUser();
        input.getAttributes().remove("birthdate");

        DomainException ex = assertThrows(
                DomainException.class,
                () -> service.createUser(input, true)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("birthdate is required"));
        verify(gateway, never()).adminCreate(any(), anyBoolean());
    }

    @Test
    void shouldFailWhenBirthdateHasWrongFormat() {
        User input = buildValidUser();
        input.getAttributes().put("birthdate", "1990-01-01"); // formato inválido

        DomainException ex = assertThrows(
                DomainException.class,
                () -> service.createUser(input, true)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("birthdate must be in format"));
        verify(gateway, never()).adminCreate(any(), anyBoolean());
    }

    @Test
    void shouldFailWhenAgeIsGreaterThan150Years() {
        User input = buildValidUser();
        input.getAttributes().put("birthdate", "01/01/1800");

        DomainException ex = assertThrows(
                DomainException.class,
                () -> service.createUser(input, true)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("age must be between 0 and 150"));
        verify(gateway, never()).adminCreate(any(), anyBoolean());
    }

    @Test
    void shouldFailWhenPasswordDoesNotMeetPolicy() {
        User input = buildValidUser();
        input.getAttributes().put("raw_password", "password1"); // no cumple

        DomainException ex = assertThrows(
                DomainException.class,
                () -> service.createUser(input, true)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("password must have at least 8 chars"));
        verify(gateway, never()).adminCreate(any(), anyBoolean());
    }

    // ===== US-HR-02: Eliminar / inhabilitar usuario =====

    @Test
    void shouldDisableUserWhenDeleteRequested() {
        String username = "user123";

        service.disableUser(username);

        verify(gateway).adminDisable(username);
    }

    // ===== Password permanente =====

    @Test
    void shouldSetPermanentPasswordWhenValid() {
        String username = "user123";
        String password = "Passw0rd!";

        service.setPermanentPassword(username, password);

        verify(gateway).adminSetPassword(username, password, true);
    }

    @Test
    void shouldFailWhenPermanentPasswordIsInvalid() {
        String username = "user123";
        String password = "password"; // no cumple

        DomainException ex = assertThrows(
                DomainException.class,
                () -> service.setPermanentPassword(username, password)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("password does not meet"));
        verify(gateway, never()).adminSetPassword(anyString(), anyString(), anyBoolean());
    }

    // ===== US-HR-03: Validaciones de actualización de datos =====

    @Test
    void shouldValidateUpdatableDataWhenAllFieldsValid() {
        Map<String, String> attrs = new HashMap<>();
        attrs.put("email", "updated@test.com");
        attrs.put("phone_number", "3010000000");
        attrs.put("address", "Nueva direccion 123");
        attrs.put("birthdate", "31/12/1995");

        assertDoesNotThrow(() -> service.validateUpdatableData(attrs));
    }

    @Test
    void shouldFailUpdateWhenEmailIsInvalid() {
        Map<String, String> attrs = new HashMap<>();
        attrs.put("email", "invalid-email");

        DomainException ex = assertThrows(
                DomainException.class,
                () -> service.validateUpdatableData(attrs)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("email is not valid"));
    }

    // ===== Grupos =====

    @Test
    void shouldAddUserToGroupsWhenListIsNotEmpty() {
        String username = "user123";
        List<String> groups = List.of("RRHH", "Medico");

        service.addUserToGroups(username, groups);

        verify(gateway).adminAddToGroups(username, groups);
    }

    @Test
    void shouldSkipAddUserToGroupsWhenListIsEmpty() {
        String username = "user123";

        service.addUserToGroups(username, List.of());

        verify(gateway, never()).adminAddToGroups(anyString(), anyList());
    }
}
