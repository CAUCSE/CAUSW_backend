package net.causw.application.dto.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.comment.ChildComment;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.Role;

import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StatusUtil {

    public static boolean isUpdatable(Comment comment, User user) {
        if (comment.getIsDeleted()) return false;
        return user.getRoles().contains(Role.ADMIN) || comment.getWriter().getId().equals(user.getId());
    }

    public static boolean isDeletable(Comment comment, User user, Board board) {
        Set<Role> roles = user.getRoles();
        if (comment.getIsDeleted()) return false;
        if (roles.contains(Role.ADMIN) || roles.contains(Role.PRESIDENT) || roles.contains(Role.VICE_PRESIDENT) || comment.getWriter().getId().equals(user.getId())) {
            return true;
        }
        User leader = board.getCircle().getLeader().orElse(null);
        if (leader == null) return false;

        return roles.contains(Role.LEADER_CIRCLE) && leader.getId().equals(user.getId());
    }

    public static boolean isUpdatable(ChildComment comment, User user) {
        if (comment.getIsDeleted()) return false;
        return user.getRoles().contains(Role.ADMIN) || comment.getWriter().getId().equals(user.getId());
    }

    public static boolean isDeletable(ChildComment comment, User user, Board board) {
        Set<Role> roles = user.getRoles();
        if (comment.getIsDeleted()) return false;
        if (roles.contains(Role.ADMIN) || roles.contains(Role.PRESIDENT) || roles.contains(Role.VICE_PRESIDENT) || comment.getWriter().getId().equals(user.getId())) {
            return true;
        }

        User leader = board.getCircle().getLeader().orElse(null);
        if (leader == null) return false;

        return roles.contains(Role.LEADER_CIRCLE) && leader.getId().equals(user.getId());
    }

    public static boolean isUpdatable(Post post, User user) {
        if (post.getIsDeleted()) return false;
        return user.getRoles().contains(Role.ADMIN) || post.getWriter().getId().equals(user.getId());
    }

    public static boolean isDeletable(Post post, User user, Board board) {
        Set<Role> roles = user.getRoles();
        if (post.getIsDeleted()) return false;
        if (roles.contains(Role.ADMIN) || roles.contains(Role.PRESIDENT) || roles.contains(Role.VICE_PRESIDENT) || post.getWriter().getId().equals(user.getId())) {
            return true;
        }

        User leader = board.getCircle().getLeader().orElse(null);

        return roles.contains(Role.LEADER_CIRCLE) && leader.getId().equals(user.getId());
    }

    public static boolean isAdminOrPresident(User user) {
        return user.getRoles().contains(Role.ADMIN) || user.getRoles().contains(Role.PRESIDENT) || user.getRoles().contains(Role.VICE_PRESIDENT);
    }
}
