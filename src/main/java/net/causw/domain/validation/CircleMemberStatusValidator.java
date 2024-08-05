package net.causw.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.Setter;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.CircleMemberStatus;
import net.causw.domain.validation.valid.CircleMemberValid;
import org.springframework.stereotype.Component;

import java.util.List;

@Setter
@Component
public class CircleMemberStatusValidator implements ConstraintValidator<CircleMemberValid, CircleMember> {
    private List<CircleMemberStatus> statusList;

    @Override
    public boolean isValid(CircleMember circleMember, ConstraintValidatorContext constraintValidatorContext) {
        CircleMemberStatus status = circleMember.getStatus();
        for (CircleMemberStatus allowedStatus : statusList) {
            if (status.equals(allowedStatus)) {
                return true;
            }
        }

        switch (status) {
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
        return false;
    }
}