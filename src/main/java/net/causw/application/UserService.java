package net.causw.application;

import net.causw.application.dto.UserCreateRequestDto;
import net.causw.application.dto.UserDetailDto;
import net.causw.application.spi.UserPort;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserPort userPort;

    public UserService(UserPort userPort) {
        this.userPort = userPort;
    }

    public UserDetailDto findById(String id) {
        return this.userPort.findById(id);
    }

    public UserDetailDto create(UserCreateRequestDto user) {
        return this.userPort.create(user);
    }
}
