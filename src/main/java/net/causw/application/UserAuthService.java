package net.causw.application;

import net.causw.application.dto.UserAuthDto;
import net.causw.domain.spi.UserAuthPort;
import org.springframework.stereotype.Service;

@Service
public class UserAuthService {
    private final UserAuthPort userAuthPort;

    public UserAuthService(UserAuthPort userAuthPort) {
        this.userAuthPort = userAuthPort;
    }

    public UserAuthDto findById(String id) {
        return UserAuthDto.of(this.userAuthPort.findById(id));
    }
}
