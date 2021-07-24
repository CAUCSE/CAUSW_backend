package net.causw.adapter.db.port;

import net.causw.adapter.db.User;
import net.causw.adapter.db.UserRepository;
import net.causw.application.dto.UserCreateRequestDto;
import net.causw.application.dto.UserDetailDto;
import net.causw.application.dto.UserFullDto;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserState;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserPortImpl implements UserPort {
    private final UserRepository userRepository;

    public UserPortImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetailDto findById(String id) {
        return UserDetailDto.from(this.userRepository.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        ));
    }

    @Override
    public Optional<UserFullDto> findByEmail(String email) {
        return this.userRepository.findByEmail(email).map(UserFullDto::from);
    }

    @Override
    public UserDetailDto create(UserCreateRequestDto user) {
        return UserDetailDto.from(this.userRepository.save(User.of(
                user.getEmail(),
                user.getName(),
                user.getPassword(),
                user.getAdmissionYear(),
                Role.NONE,
                UserState.WAIT
        )));
    }
}
