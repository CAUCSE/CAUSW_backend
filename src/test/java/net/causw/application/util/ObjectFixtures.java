package net.causw.application.util;

import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.comment.ChildComment;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.enums.UserState;

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
    public static Comment getComment() {
        return Comment.of("content", false, getUser(), getPost());
    }

    // childComment
    public static ChildComment getChildComment(String content, boolean isDeleted) {
        return ChildComment.of(content, isDeleted, "tagUserName", "ref", getUser(), getComment());
    }
}

