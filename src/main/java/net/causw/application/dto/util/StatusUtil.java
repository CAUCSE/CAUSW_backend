package net.causw.application.dto.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.comment.ChildComment;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.Role;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StatusUtil {

    public static boolean isUpdatable(Comment comment, User user) {
        if (comment.getIsDeleted()) return false;
        return user.getRole() == Role.ADMIN || comment.getWriter().getId().equals(user.getId());
    }

    public static boolean isDeletable(Comment comment, User user, Board board) {
        if (comment.getIsDeleted()) return false;
        if (user.getRole() == Role.ADMIN || user.getRole().getValue().contains("PRESIDENT") || comment.getWriter().getId().equals(user.getId())) {
            return true;
        }
        return board.getCircle() != null && user.getRole().getValue().contains("LEADER_CIRCLE")
                && board.getCircle().getLeader().map(leader -> leader.getId().equals(user.getId())).orElse(false);
    }

    public static boolean isUpdatable(ChildComment comment, User user) {
        if (comment.getIsDeleted()) return false;
        return user.getRole() == Role.ADMIN || comment.getWriter().getId().equals(user.getId());
    }

    public static boolean isDeletable(ChildComment comment, User user, Board board) {
        if (comment.getIsDeleted()) return false;
        if (user.getRole() == Role.ADMIN || user.getRole().getValue().contains("PRESIDENT") || comment.getWriter().getId().equals(user.getId())) {
            return true;
        }
        return board.getCircle() != null && user.getRole().getValue().contains("LEADER_CIRCLE")
                && board.getCircle().getLeader().map(leader -> leader.getId().equals(user.getId())).orElse(false);
    }

    public static boolean isUpdatable(Post post, User user) {
        if (post.getIsDeleted()) return false;
        return user.getRole() == Role.ADMIN || post.getWriter().getId().equals(user.getId());
    }

    public static boolean isDeletable(Post post, User user, Board board) {
        if (post.getIsDeleted()) return false;
        if (user.getRole() == Role.ADMIN || user.getRole().getValue().contains("PRESIDENT") || post.getWriter().getId().equals(user.getId())) {
            return true;
        }
        return board.getCircle() != null && user.getRole().getValue().contains("LEADER_CIRCLE")
                && board.getCircle().getLeader().map(leader -> leader.getId().equals(user.getId())).orElse(false);
    }
}
