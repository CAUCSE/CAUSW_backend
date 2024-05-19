package net.causw.application.util;

import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.enums.UserState;

import java.util.List;

public class MockObject {

    public static List<User> getUsers() {
        return List.of(
                User.of("user1", "email", "name", "password", "studentId", 2021, Role.ADMIN, "profileImage", UserState.ACTIVE),
                User.of("user2", "email", "name", "password", "studentId", 2021, Role.ADMIN, "profileImage", UserState.ACTIVE),
                User.of("user3", "email", "name", "password", "studentId", 2021, Role.ADMIN, "profileImage", UserState.ACTIVE)
        );
    }

    public static User getUser() {
        return User.of("user1", "email", "name", "password", "studentId", 2021, Role.ADMIN, "profileImage", UserState.ACTIVE);
    }

    public static Board getBoard() {
        return Board.of("board1", "name", "description", "createRoles", "category", false, null);
    }

}
