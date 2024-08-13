package net.causw.application.locker;

import net.causw.adapter.persistence.locker.Locker;
import net.causw.adapter.persistence.user.User;
import net.causw.application.common.CommonService;

import java.util.Optional;

public interface LockerAction {
    Optional<Locker> updateLocker(
            Locker locker,
            User user,
            LockerService lockerService,
            CommonService commonService
    );
}
