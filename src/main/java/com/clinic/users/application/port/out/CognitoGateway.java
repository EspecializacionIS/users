package com.clinic.users.application.port.out;

import com.clinic.users.domain.model.User;
import java.util.List;

public interface CognitoGateway {

    User adminCreate(User user, boolean sendInvite);
    void adminEnable(String username);
    void adminDisable(String username);
    void adminSetPassword(String username, String password, boolean permanent);
    void adminAddToGroups(String username, List<String> groups);
    User adminGet(String username);
    List<User> listUsers(int limit, String filter);
    List<String> listGroupsForUser(String username);

}
