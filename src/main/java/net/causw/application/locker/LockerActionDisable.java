package net.causw.application.locker;

import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.locker.Locker;
import net.causw.adapter.persistence.user.User;
import net.causw.application.common.CommonService;
import net.causw.domain.model.enums.Role;
import net.causw.domain.validation.LockerIsDeactivatedValidator;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.ValidatorBucket;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public class LockerActionDisable implements LockerAction {
    @Override
    public Optional<Locker> updateLockerDomainModel(
            Locker locker,
            User user,
            LockerService lockerService,
            CommonService commonService
    ) {
        ValidatorBucket.of()
                .consistOf(UserRoleValidator.of(user.getRole(), List.of(Role.PRESIDENT)))
                .consistOf(LockerIsDeactivatedValidator.of(locker.getIsActive()))
                .validate();

        locker.deactivate();
        return Optional.of(locker);
    }
}
