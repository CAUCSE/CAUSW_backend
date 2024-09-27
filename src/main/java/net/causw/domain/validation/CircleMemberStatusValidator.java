package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.circle.CircleMemberStatus;

import java.util.List;

public class CircleMemberStatusValidator extends AbstractValidator {

    private final CircleMemberStatus status;

    private final List<CircleMemberStatus> statusList;

    private CircleMemberStatusValidator(CircleMemberStatus status, List<CircleMemberStatus> statusList) {
        this.status = status;
        this.statusList = statusList;
    }

    public static CircleMemberStatusValidator of(CircleMemberStatus status, List<CircleMemberStatus> statusList) {
        return new CircleMemberStatusValidator(status, statusList);
    }

    @Override
    public void validate() {
        for (CircleMemberStatus allowedStatus : this.statusList) {
            if (this.status.equals(allowedStatus)) {
                return;
            }
        }

        switch (this.status) {
            case MEMBER:
                throw new BadRequestException(
                        ErrorCode.ROW_ALREADY_EXIST,
                        "이미 동아리에 가입한 사용자 입니다."
                );
            case LEAVE:
                throw new BadRequestException(
                        ErrorCode.APPLY_NOT_EXIST,
                        "동아리를 떠난 사용자 입니다."
                );
            case AWAIT:
                throw new BadRequestException(
                        ErrorCode.AWAITING_STATUS,
                        "동아리 가입 대기 중인 사용자 입니다."
                );
            case REJECT:
                throw new UnauthorizedException(
                        ErrorCode.BLOCKED_USER,
                        "동아리 가입 거절된 사용자 입니다."
                );
            case DROP:
                throw new UnauthorizedException(
                        ErrorCode.BLOCKED_USER,
                        "동아리에서 추방된 사용자 입니다."
                );
            default:
                break;
        }
    }
}