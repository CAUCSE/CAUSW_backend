package net.causw.app.main.service.locker;

import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;
import net.causw.app.main.domain.model.enums.locker.LockerLogAction;
import net.causw.global.constant.MessageUtil;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class LockerActionFactory {
    private final Map<LockerLogAction, Supplier<LockerAction>> map;

    public LockerActionFactory() {
        this.map = new HashMap<>();

        this.map.put(LockerLogAction.ENABLE, LockerActionEnable::new);
        this.map.put(LockerLogAction.DISABLE, LockerActionDisable::new);
        this.map.put(LockerLogAction.REGISTER, LockerActionRegister::new);
        this.map.put(LockerLogAction.RETURN, LockerActionReturn::new);
        this.map.put(LockerLogAction.EXTEND, LockerActionExtend::new);
    }

    public LockerAction getLockerAction(LockerLogAction action) {
        Supplier<LockerAction> updateAction = map.get(action);
        if(updateAction == null) {
            throw new BadRequestException(
                    ErrorCode.INVALID_PARAMETER,
                    MessageUtil.INVALID_PARAMETER
            );
        }
        return updateAction.get();
    }
}
