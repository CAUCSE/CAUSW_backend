package net.causw.application.locker;

import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.locker.Locker;
import net.causw.adapter.persistence.user.User;
import net.causw.application.common.CommonService;
import net.causw.domain.validation.LockerIsDeactivatedValidator;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.ValidatorBucket;

import java.util.Optional;
import java.util.Set;

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
                .consistOf(UserRoleValidator.of(user.getRoles(), Set.of()))
                .consistOf(LockerIsDeactivatedValidator.of(locker.getIsActive()))
                .validate();

        locker.deactivate();
        return Optional.of(locker);
    }
}
