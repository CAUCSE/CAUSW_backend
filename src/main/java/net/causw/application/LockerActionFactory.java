package net.causw.application;

import lombok.NoArgsConstructor;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.LockerLogAction;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@NoArgsConstructor
public class LockerActionFactory {
    final static Map<LockerLogAction, Supplier<LockerAction>> map = new HashMap<>();
    static {
        map.put(LockerLogAction.ENABLE, LockerActionEnable::new);
        map.put(LockerLogAction.DISABLE, LockerActionDisable::new);
        map.put(LockerLogAction.REGISTER, LockerActionRegister::new);
        map.put(LockerLogAction.RETURN, LockerActionReturn::new);
    }

    public LockerAction getLockerAction(LockerLogAction action) {
        Supplier<LockerAction> updateAction = map.get(action);
        if(updateAction != null) {
            return updateAction.get();
        }
        throw new BadRequestException(
                ErrorCode.INVALID_PARAMETER,
                "Invalid action parameter"
        );
    }
}
