package com.clinic.users.application.port.in;

import com.clinic.users.domain.model.User;
import java.util.List;

public interface UserQueryUseCase {

    User findByUsername(String username);
    List<User> list(int limit, String filter);
}
