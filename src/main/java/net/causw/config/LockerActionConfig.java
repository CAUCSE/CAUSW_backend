package net.causw.config;

import net.causw.application.LockerAction;
import net.causw.application.LockerActionDisable;
import net.causw.application.LockerActionEnable;
import net.causw.application.LockerActionFactory;
import net.causw.application.LockerActionRegister;
import net.causw.application.LockerActionReturn;
import net.causw.domain.model.LockerLogAction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Configuration
public class LockerActionConfig {
    @Bean
    LockerActionFactory initializeLockerActionFactory() {
        Map<LockerLogAction, Supplier<LockerAction>> map = new HashMap<>();

        map.put(LockerLogAction.ENABLE, LockerActionEnable::new);
        map.put(LockerLogAction.DISABLE, LockerActionDisable::new);
        map.put(LockerLogAction.REGISTER, LockerActionRegister::new);
        map.put(LockerLogAction.RETURN, LockerActionReturn::new);

        return new LockerActionFactory(map);
    }
}
