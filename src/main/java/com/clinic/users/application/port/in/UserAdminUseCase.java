package com.clinic.users.application.port.in;
import com.clinic.users.domain.model.User;
import java.util.List;

public interface UserAdminUseCase {

    User createUser(User user, boolean sendInvite);

    void disableUser(String username);

    void enableUser(String username);

    void setPermanentPassword(String username, String password);

    void addUserToGroups(String username, List<String> groups);
}
