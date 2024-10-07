package net.causw.application.dto.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.comment.ChildComment;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.form.Form;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.userCouncilFee.UserCouncilFee;
import net.causw.adapter.persistence.vote.Vote;
import net.causw.domain.model.enums.user.Role;

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
        User leader = board.getCircle() != null ? board.getCircle().getLeader().orElse(null) : null;
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

    public static boolean isUpdatable(Post post, User user, Boolean hasComment) {
        // 게시글이 삭제된 경우 업데이트 불가
        if (post.getIsDeleted()) {
            return false;
        }

        // 사용자가 관리자 역할을 가진 경우 업데이트 가능
        if (user.getRoles().contains(Role.ADMIN)) {
            return true;
        }

        // 질문글이면서 댓글을 가진 경우는 업데이트 불가능
        if (post.getIsQuestion() && hasComment) {
            return false;
        }

        // 작성자인 경우 업데이트 가능
        return post.getWriter().getId().equals(user.getId());
    }

    public static boolean isDeletable(Post post, User user, Board board, Boolean hasComment) {
        Set<Role> roles = user.getRoles();

        // 게시글이 삭제된 경우 삭제 불가능
        if (post.getIsDeleted()) return false;

        // 관리자, 회장, 부회장인 경우 삭제 가능
        if (roles.contains(Role.ADMIN) || roles.contains(Role.PRESIDENT) || roles.contains(Role.VICE_PRESIDENT)) {
            return true;
        }

        // 동아리 리더인 경우 삭제 가능
        if (board.getCircle() != null) {
            User leader = board.getCircle().getLeader().orElse(null);
            if (leader != null && roles.contains(Role.LEADER_CIRCLE) && leader.getId().equals(user.getId())) {
                return true;
            }
        }

        // 질문글이면서 댓글을 가진 경우는 삭제 불가능
        if (post.getIsQuestion() && hasComment) {
            return false;
        }

        // 작성자인 경우 삭제 가능
        return post.getWriter().getId().equals(user.getId());
    }

    public static boolean isAdminOrPresident(User user) {
        return user.getRoles().contains(Role.ADMIN) || user.getRoles().contains(Role.PRESIDENT) || user.getRoles().contains(Role.VICE_PRESIDENT);
    }

    public static boolean isPostVote(Post post) {
        return post.getVote() != null;
    }

    public static boolean isPostForm(Post post) {
        return post.getForm() != null;
    }

    public static boolean isVoteOwner(Vote vote , User user) {
        return user.equals(vote.getPost().getWriter());
    }

    public static boolean isPostOwner(Post post, User user) {
        return user.equals(post.getWriter());
    }

    public static boolean isCommentOwner(Comment comment, User user) {
        return user.equals(comment.getWriter());
    }

    public static boolean isChildCommentOwner(ChildComment childComment, User user) {
        return user.equals(childComment.getWriter());
    }

    public static int getRestOfSemester(UserCouncilFee userCouncilFee) {
        int refundedAt = userCouncilFee.getRefundedAt() == null ? 0 : userCouncilFee.getRefundedAt();

        Integer startOfAppliedSemester = userCouncilFee.getPaidAt();
        Integer endOfAppliedSemester = !( userCouncilFee.getIsRefunded() ) ?
                ( startOfAppliedSemester - 1 ) + userCouncilFee.getNumOfPaidSemester() :
                refundedAt;

        int restOfSemester;

        if (userCouncilFee.getIsJoinedService()) {
            restOfSemester = Math.max(endOfAppliedSemester - userCouncilFee.getUser().getCurrentCompletedSemester(), 0);
        } else {
            restOfSemester = Math.max(endOfAppliedSemester - userCouncilFee.getCouncilFeeFakeUser().getCurrentCompletedSemester(), 0);
        }
        return restOfSemester;
    }

    public static boolean getIsAppliedCurrentSemester(UserCouncilFee userCouncilFee) {
        int refundedAt = userCouncilFee.getRefundedAt() == null ? 0 : userCouncilFee.getRefundedAt();

        Integer startOfAppliedSemester = userCouncilFee.getPaidAt();
        int endOfAppliedSemester = !( userCouncilFee.getIsRefunded() ) ?
                ( startOfAppliedSemester - 1 ) + userCouncilFee.getNumOfPaidSemester() :
                refundedAt;

        if (userCouncilFee.getIsJoinedService()) {
            return (startOfAppliedSemester <= userCouncilFee.getUser().getCurrentCompletedSemester()) &&
                    (userCouncilFee.getUser().getCurrentCompletedSemester() <= endOfAppliedSemester);
        } else {
            return (startOfAppliedSemester <= userCouncilFee.getCouncilFeeFakeUser().getCurrentCompletedSemester()) &&
                    (userCouncilFee.getCouncilFeeFakeUser().getCurrentCompletedSemester() <= endOfAppliedSemester);
        }
    }

}
