package net.causw.application;

import net.causw.application.dto.UserAuthDto;
import net.causw.application.spi.UserAuthPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAuthService {
    private final UserAuthPort userAuthPort;

    public UserAuthService(UserAuthPort userAuthPort) {
        this.userAuthPort = userAuthPort;
    }

    @Transactional(readOnly = true)
    public UserAuthDto findById(String id) {
        return this.userAuthPort.findById(id);
    }
}
