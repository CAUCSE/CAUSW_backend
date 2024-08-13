package net.causw.application.locker;

import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.locker.Locker;
import net.causw.adapter.persistence.user.User;
import net.causw.application.common.CommonService;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.ValidatorBucket;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@NoArgsConstructor
public class LockerActionReturn implements LockerAction {
    @Override
    public Optional<Locker> updateLocker(
            Locker locker,
            User user,
            LockerService lockerService,
            CommonService commonService
    ){
        if (locker.getUser().isEmpty()) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    MessageUtil.LOCKER_UNUSED
            );
        }

        if (!user.getId().equals(locker.getUser().get().getId()))
            new UserRoleValidator().validate(user.getRoles(), Set.of());

        locker.returnLocker();

        return Optional.of(locker);
    }
}
