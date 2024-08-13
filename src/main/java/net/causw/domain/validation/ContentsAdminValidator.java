package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.Role;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Validate is the request user has role for API of the contents (post or comment)
 * Post & Comment Update => The user must be the writer of the contents
 * Post & Comment Delete => The user may be the writer of the contents or administrator of the board
 */
public class ContentsAdminValidator {
    public void validate(Set<Role> requestUserRoles, String requestUserId, String writerUserId, List<Role> adminRoleList) {
        if (requestUserId.equals(writerUserId)) {
            return;
        }

        if (requestUserRoles.stream().anyMatch(role -> EnumSet.of(Role.ADMIN, Role.PRESIDENT, Role.VICE_PRESIDENT).contains(role))) {
            return;
        }

        for (Role adminRole : adminRoleList) {
            if (requestUserRoles.contains(adminRole)) {
                return;
            }
        }

        throw new UnauthorizedException(
                ErrorCode.API_NOT_ALLOWED,
                "접근 권한이 없습니다."
        );
    }
}