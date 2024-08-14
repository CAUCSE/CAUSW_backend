package net.causw.application.util;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.user.User;
import net.causw.application.circle.CircleService;
import net.causw.application.comment.ChildCommentService;
import net.causw.application.comment.CommentService;
import net.causw.application.post.PostService;
import net.causw.application.user.UserService;
import net.causw.domain.model.enums.CircleMemberStatus;
import net.causw.domain.model.enums.Role;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ServiceProxy {
    private final ApplicationContext applicationContext;

    /* UserService */

    public User getUserByEmailSignIn(String email) {
        UserService userService = applicationContext.getBean(UserService.class);
        return userService.getUserByEmailSignIn(email);
    }

    public CircleMember getCircleMemberUser(String userId, String circleId, List<CircleMemberStatus> list) {
        UserService userService = applicationContext.getBean(UserService.class);
        return userService.getCircleMember(userId, circleId, list);
    }

    public User getGrantee(String granteeId, Set<Role> granterRoles, Role targetRole) {
        UserService userService = applicationContext.getBean(UserService.class);
        return userService.getGrantee(granteeId, granterRoles, targetRole);
    }

    /* CircleService */

    // User의 Role에 상관 없이 UserEqualValidator을 실행하는 메서드 (userId == circleLeaderId)
    public Circle getCircleWithUserEqualValidator(User user, String circleId) {
        CircleService circleService = applicationContext.getBean(CircleService.class);
        return circleService.getCircleUserEqualValidator(user, circleId);
    }

    // User의 Role에 상관 없이 UserNotEqualValidator을 실행하는 메서드 (userId == circleLeaderId)
    public Circle getCircleWithUserNotEqualValidator(User user, String circleId) {
        CircleService circleService = applicationContext.getBean(CircleService.class);
        return circleService.getCircleUserNotEqualValidator(user, circleId);
    }

    // User의 Role에 LEADER_CIRCLE이 포함되는지 조건을 확인하여 있으면 UserEqualValidator을 실행하는 메서드 (userId == circleLeaderId)
    public Circle getCircleWithValidatorWithRoleCheck(User user, String circleId) {
        CircleService circleService = applicationContext.getBean(CircleService.class);
        return circleService.getCircleWithRoleCheck(user, circleId);
    }

    // User의 Role에 LEADER_CIRCLE이 포함되는지 조건을 확인하여 있으면 UserEqualValidator + UserNotEqualValidator을 실행하는 메서드 (userId == circleLeaderId)
    public Circle getCircleWithValidatorWithRoleCheckAll(User user, String circleId) {
        CircleService circleService = applicationContext.getBean(CircleService.class);
        return circleService.getCircleWithRoleCheckAll(user, circleId);
    }

    public CircleMember getCircleMemberById(String applicationId, List<CircleMemberStatus> list) {
        CircleService circleService = applicationContext.getBean(CircleService.class);
        return circleService.getCircleMember(applicationId, list);
    }

    public CircleMember getCircleMemberCircleService(String userId, String circleId, List<CircleMemberStatus> list) {
        CircleService circleService = applicationContext.getBean(CircleService.class);
        return circleService.getCircleMember(userId, circleId, list);
    }

    public CircleMember getCircleMemberOrCreate(User user, Circle circle, List<CircleMemberStatus> list) {
        CircleService circleService = applicationContext.getBean(CircleService.class);
        return circleService.getCircleMemberOrCreate(user, circle, list);
    }

    public User getLeader(String granteeId, Set<Role> granterRoles, Role targetRole) {
        CircleService circleService = applicationContext.getBean(CircleService.class);
        return circleService.getLeader(granteeId, granterRoles, targetRole);
    }

    /* ChildCommentService */

    public CircleMember getCircleMemberChildComment(String userId, String circleId, List<CircleMemberStatus> list) {
        ChildCommentService childCommentService = applicationContext.getBean(ChildCommentService.class);
        return childCommentService.getCircleMember(userId, circleId, list);
    }

    /* CommentService */

    public CircleMember getCircleMemberComment(String userId, String circleId, List<CircleMemberStatus> list) {
        CommentService commentService = applicationContext.getBean(CommentService.class);
        return commentService.getCircleMember(userId, circleId, list);
    }

    /* PostService */

    public CircleMember getCircleMemberPost(String userId, String circleId, List<CircleMemberStatus> list) {
        PostService postService = applicationContext.getBean(PostService.class);
        return postService.getCircleMember(userId, circleId, list);
    }

}
