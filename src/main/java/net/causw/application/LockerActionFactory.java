package net.causw.application;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.LockerLogAction;

import java.util.Map;
import java.util.function.Supplier;

public class LockerActionFactory {
    private final Map<LockerLogAction, Supplier<LockerAction>> map;

    public LockerActionFactory(Map<LockerLogAction, Supplier<LockerAction>> map) {
        this.map = map;
    }

    public LockerAction getLockerAction(LockerLogAction action) {
        Supplier<LockerAction> updateAction = map.get(action);
        if(updateAction == null) {
            throw new BadRequestException(
                    ErrorCode.INVALID_PARAMETER,
                    "Invalid action parameter"
            );
        }
        return updateAction.get();
    }
}
