package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.Role;

import java.util.List;

/**
 * Validate is the request user has role for API of the contents (post or comment)
 * Post & Comment Update => The user must be the writer of the contents
 * Post & Comment Delete => The user may be the writer of the contents or administrator of the board
 */
public class ContentsAdminValidator extends AbstractValidator {

    private final Role requestUserRole;

    private final String requestUserId;

    private final String writerUserId;

    private final List<Role> adminRoleList;

    private ContentsAdminValidator(
            Role requestUserRole,
            String requestUserId,
            String writerUserId,
            List<Role> adminRoleList
    ) {
        this.requestUserRole = requestUserRole;
        this.requestUserId = requestUserId;
        this.writerUserId = writerUserId;
        this.adminRoleList = adminRoleList;
    }

    public static ContentsAdminValidator of(
            Role requestUserRole,
            String requestUserId,
            String writerUserId,
            List<Role> adminRoleList
    ) {
        return new ContentsAdminValidator(
                requestUserRole,
                requestUserId,
                writerUserId,
                adminRoleList
        );
    }

    @Override
    public void validate() {
        if (this.requestUserId.equals(this.writerUserId)) {
            return;
        }

        if (this.requestUserRole.equals(Role.ADMIN)) {
            return;
        }

        for (Role adminRole : this.adminRoleList) {
            if (this.requestUserRole.equals(adminRole)) {
                return;
            }
        }

        throw new UnauthorizedException(
                ErrorCode.API_NOT_ALLOWED,
                "접근 권한이 없습니다."
        );
    }
}