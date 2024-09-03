package net.causw.application.locker;

import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.locker.Locker;
import net.causw.adapter.persistence.user.User;
import net.causw.application.common.CommonService;
import net.causw.domain.model.enums.Role;
import net.causw.domain.validation.LockerIsActiveValidator;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.ValidatorBucket;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@NoArgsConstructor
public class LockerActionEnable implements LockerAction {
    @Override
    public Optional<Locker> updateLockerDomainModel(
            Locker locker,
            User user,
            LockerService lockerService,
            CommonService commonService
    ) {
        ValidatorBucket.of()
                .consistOf(UserRoleValidator.of(user.getRoles(), Set.of()))
                .consistOf(LockerIsActiveValidator.of(locker.getIsActive()))
                .validate();

        locker.activate();

        return Optional.of(locker);
    }
}
