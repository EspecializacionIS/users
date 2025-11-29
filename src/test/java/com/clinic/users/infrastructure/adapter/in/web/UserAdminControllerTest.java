package com.clinic.users.infrastructure.adapter.in.web;

import com.clinic.users.application.port.in.UserAdminUseCase;
import com.clinic.users.application.port.in.UserQueryUseCase;
import com.clinic.users.domain.model.Group;
import com.clinic.users.domain.model.User;
import com.clinic.users.infrastructure.adapter.in.web.dto.CreateUserRequest;
import com.clinic.users.infrastructure.adapter.in.web.dto.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserAdminController.class)
class UserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserAdminUseCase adminUseCase;

    @MockBean
    private UserQueryUseCase queryUseCase;

    private User buildUserFromRequest(CreateUserRequest req) {
        Map<String, String> attrs = new HashMap<>();
        attrs.put("given_name", req.getFirstName());
        attrs.put("family_name", req.getLastName());
        attrs.put("email", req.getEmail());
        attrs.put("phone_number", req.getPhone());
        attrs.put("address", req.getAddress());
        attrs.put("birthdate", req.getBirthdate());
        attrs.put("custom_document", req.getDocument());
        attrs.put("custom_role", req.getRole().name());
        attrs.put("status", "ACTIVE");
        attrs.put("raw_password", req.getPassword());

        return User.builder()
                .username(req.getUsername())
                .enabled(true)
                .attributes(attrs)
                .build();
    }

    @Test
    void shouldReturnOkWhenCreateUserWithValidRequest() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("user123")
                .firstName("Ana")
                .lastName("Perez")
                .document("123456789")
                .email("user@test.com")
                .phone("+3001234567")
                .address("Calle 123")
                .birthdate("01/01/1990")
                .password("Passw0rd!")
                .role(Group.RRHH)
                .sendInvite(true)
                .build();

        User created = buildUserFromRequest(request);

        when(adminUseCase.createUser(any(User.class), anyBoolean()))
                .thenReturn(created);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user123"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void shouldReturnBadRequestWhenEmailIsMissing() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("user123")
                .firstName("Ana")
                .lastName("Perez")
                .document("123456789")
                // sin email
                .phone("3001234567")
                .address("Calle 123")
                .birthdate("01/01/1990")
                .password("Passw0rd!")
                .role(Group.RRHH)
                .sendInvite(true)
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNoContentWhenDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/{username}", "user123"))
                .andExpect(status().isNoContent());
    }
}
