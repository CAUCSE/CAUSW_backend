package net.causw.adapter.persistence.port;

import net.causw.application.dto.UserCreateRequestDto;
import net.causw.application.dto.UserDetailDto;
import net.causw.application.dto.UserFullDto;
import net.causw.application.dto.UserUpdateRequestDto;
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
    public UserDetailDto create(UserCreateRequestDto userCreateRequestDto) {
        return UserDetailDto.from(this.userRepository.save(User.of(
                userCreateRequestDto.getEmail(),
                userCreateRequestDto.getName(),
                userCreateRequestDto.getPassword(),
                userCreateRequestDto.getStudentId(),
                userCreateRequestDto.getAdmissionYear(),
                Role.NONE,
                UserState.WAIT
        )));
    }

    @Override
    public Optional<UserDetailDto> update(String id, UserUpdateRequestDto userUpdateRequestDto) {
        return this.userRepository.findById(id).map(
                srcUser -> {
                    srcUser.setEmail(userUpdateRequestDto.getEmail());
                    srcUser.setName(userUpdateRequestDto.getName());
                    srcUser.setPassword(userUpdateRequestDto.getPassword());
                    srcUser.setStudentId(userUpdateRequestDto.getStudentId());
                    srcUser.setAdmissionYear(userUpdateRequestDto.getAdmissionYear());

                    return UserDetailDto.from(this.userRepository.save(srcUser));
                }
        );
    }
}
