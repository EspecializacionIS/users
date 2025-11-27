package com.clinic.users.infrastructure.adapter.in.web;

import com.clinic.users.application.port.in.UserAdminUseCase;
import com.clinic.users.application.port.in.UserQueryUseCase;
import com.clinic.users.domain.model.User;
import com.clinic.users.infrastructure.adapter.in.web.dto.AddToGroupRequest;
import com.clinic.users.infrastructure.adapter.in.web.dto.CreateUserRequest;
import com.clinic.users.infrastructure.adapter.in.web.dto.SetPasswordRequest;
import com.clinic.users.infrastructure.adapter.in.web.dto.UpdateUserRequest;
import com.clinic.users.infrastructure.adapter.in.web.dto.UserResponse;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminUseCase adminUseCase;
    private final UserQueryUseCase queryUseCase;


    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        log.info("Creating user {}", request.getUsername());

        Map<String, String> attrs = new HashMap<>();
        attrs.put("given_name", request.getFirstName());
        attrs.put("family_name", request.getLastName());
        attrs.put("email", request.getEmail());
        attrs.put("phone_number", request.getPhone());
        attrs.put("address", request.getAddress());
        attrs.put("birthdate", request.getBirthdate());
        attrs.put("custom_document", request.getDocument());
        attrs.put("custom_role", request.getRole().name());
        attrs.put("status", "ACTIVE");
        attrs.put("raw_password", request.getPassword());

        User user = User.builder()
                .username(request.getUsername())
                .enabled(true)
                .attributes(attrs)
                .build();

        User created = adminUseCase.createUser(user, request.isSendInvite());

        adminUseCase.addUserToGroups(
                created.getUsername(),
                List.of(request.getRole().getIamName()));

        adminUseCase.setPermanentPassword(
                created.getUsername(),
                request.getPassword());

        return ResponseEntity.ok(toResponse(created));
    }


    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        log.info("Disabling (soft-deleting) user {}", username);
        adminUseCase.disableUser(username);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{username}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String username,
            @Valid @RequestBody UpdateUserRequest request) {

        log.info("Updating user {}", username);

        User existing = queryUseCase.findByUsername(username);

        Map<String, String> attrs = new HashMap<>(existing.getAttributes());

        if (request.getEmail() != null) {
            attrs.put("email", request.getEmail());
        }
        if (request.getPhone() != null) {
            attrs.put("phone_number", request.getPhone());
        }
        if (request.getAddress() != null) {
            attrs.put("address", request.getAddress());
        }
        if (request.getBirthdate() != null) {
            attrs.put("birthdate", request.getBirthdate());
        }

        User updated = User.builder()
                .username(existing.getUsername())
                .enabled(existing.getEnabled())
                .attributes(attrs)
                .build();

        return ResponseEntity.ok(toResponse(updated));
    }

    @PostMapping("/{username}/password")
    public ResponseEntity<Void> setPassword(
            @PathVariable String username,
            @Valid @RequestBody SetPasswordRequest request) {

        adminUseCase.setPermanentPassword(username, request.getPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/groups")
    public ResponseEntity<Void> addToGroups(@Valid @RequestBody AddToGroupRequest request) {
        adminUseCase.addUserToGroups(request.getUsername(), request.getGroups());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserResponse> getByUsername(@PathVariable String username) {
        User user = queryUseCase.findByUsername(username);

        return ResponseEntity.ok(toResponse(user));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> list(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String filter) {

        List<UserResponse> users =
                queryUseCase.list(limit, filter).stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(users);
    }

    private UserResponse toResponse(User u) {
        String status = Boolean.TRUE.equals(u.getEnabled()) ? "ACTIVE" : "INACTIVE";
        Map<String, String> attrs = new HashMap<>(u.getAttributes());
        attrs.put("status", status);

        return UserResponse.builder()
                .username(u.getUsername())
                .enabled(u.getEnabled())
                .attributes(attrs)
                .build();
    }
}
