package net.causw.application;

import net.causw.application.dto.UserDetailDto;
import net.causw.application.dto.UserSaveRequestDto;
import net.causw.domain.spi.UserPort;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserPort userPort;

    public UserService(UserPort userPort) {
        this.userPort = userPort;
    }

    public UserDetailDto findById(String id) {
        return UserDetailDto.of(this.userPort.findById(id));
    }

    public String save(UserSaveRequestDto requestDto) {
        return this.userPort.save(requestDto.toEntity()).getId();
    }
}
