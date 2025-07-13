package net.causw.app.main.service.locker;

import net.causw.app.main.domain.model.entity.locker.Locker;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.service.common.CommonService;

import java.util.Optional;

public interface LockerAction {
    Optional<Locker> updateLockerDomainModel(
            Locker locker,
            User user,
            LockerService lockerService,
            CommonService commonService
    );
}
