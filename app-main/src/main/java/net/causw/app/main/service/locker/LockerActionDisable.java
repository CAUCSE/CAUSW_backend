package net.causw.app.main.service.locker;

import lombok.NoArgsConstructor;
import net.causw.app.main.domain.model.entity.locker.Locker;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.service.common.CommonService;
import net.causw.app.main.domain.validation.LockerIsDeactivatedValidator;
import net.causw.app.main.domain.validation.UserRoleValidator;
import net.causw.app.main.domain.validation.ValidatorBucket;

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
