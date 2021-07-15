package net.causw.application;

import net.causw.application.dto.UserAuthDto;
import net.causw.application.spi.UserAuthPort;
import org.springframework.stereotype.Service;

@Service
public class UserAuthService {
    private final UserAuthPort userAuthPort;

    public UserAuthService(UserAuthPort userAuthPort) {
        this.userAuthPort = userAuthPort;
    }

    public UserAuthDto findById(String id) {
        return this.userAuthPort.findById(id);
    }
}
