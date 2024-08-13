package net.causw.application.locker;

import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.locker.Locker;
import net.causw.adapter.persistence.user.User;
import net.causw.application.common.CommonService;
import net.causw.domain.validation.LockerIsActiveValidator;

import java.util.Optional;

@NoArgsConstructor
public class LockerActionEnable implements LockerAction {
    @Override
    public Optional<Locker> updateLocker(
            Locker locker,
            User user,
            LockerService lockerService,
            CommonService commonService
    ) {
        new LockerIsActiveValidator().validate(locker.getIsActive());
        locker.activate();
        return Optional.of(locker);
    }
}
