package net.causw.adapter.persistence.port;

import net.causw.application.dto.UserCreateRequestDto;
import net.causw.application.dto.UserDetailDto;
import net.causw.application.dto.UserFullDto;
import net.causw.application.spi.UserPort;
import net.causw.domain.model.Role;
import net.causw.adapter.persistence.User;
import net.causw.adapter.persistence.UserRepository;
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
    public Optional<UserDetailDto> findById(String id) {
        return this.userRepository.findById(id).map(UserDetailDto::from);
    }

    @Override
    public Optional<UserDetailDto> findByName(String name) {
        return this.userRepository.findByName(name).map(UserDetailDto::from);
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
                user.getStudentId(),
                user.getAdmissionYear(),
                Role.NONE,
                UserState.WAIT
        )));
    }
}
