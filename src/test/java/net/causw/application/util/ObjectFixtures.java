package net.causw.application.util;

import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.comment.ChildComment;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.enums.UserState;

/**
 * 테스트에서 사용할 객체를 생성하는 메서드를 정의하는 Fixture 클래스입니다.
 * 중요하지 않은 값들은 여기서 고정값으로 정의하고, 테스트에서 필요한 값만 변경해서 사용합니다.
 * 매개변수를 다르게 하여 값이 일부 다른 객체를 생성하기 위해서 별도의 메서드를 추가로 정의할 수도 있습니다.
 */
public class ObjectFixtures {

    // user
    public static User getUser() {
        return User.of("user1", "email", "name", "password", "studentId", 2021, Role.ADMIN, "profileImage", UserState.ACTIVE);
    }

    // board
    public static Board getBoard() {
        return Board.of("board1", "name", "description", "createRoles", "category", false, null);
    }

    // post
    public static Post getPost() {
        return Post.of("title", "content", getUser(), false, getBoard(),"attachment");
    }

    // comment
    public static Comment getComment(String content) {
        return Comment.of(content, false, getUser(), getPost());
    }

    public static Comment getComment(boolean isDeleted) {
        return Comment.of("content", isDeleted, getUser(), getPost());
    }

    // childComment
    public static ChildComment getChildComment(String content, boolean isDeleted) {
        return ChildComment.of(content, isDeleted, "tagUserName", "ref", getUser(), getComment(false));
    }
}

