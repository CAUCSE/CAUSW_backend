package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.User;
import net.causw.adapter.persistence.UserRepository;
import net.causw.application.dto.UserCreateRequestDto;
import net.causw.application.dto.UserFullDto;
import net.causw.application.dto.UserUpdateRequestDto;
import net.causw.application.spi.UserPort;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserState;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserPortImpl implements UserPort {
    private final UserRepository userRepository;

    public UserPortImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<UserFullDto> findById(String id) {
        return this.userRepository.findById(id).map(UserFullDto::from);
    }

    @Override
    public Optional<UserFullDto> findByName(String name) {
        return this.userRepository.findByName(name).map(UserFullDto::from);
    }

    @Override
    public Optional<UserFullDto> findByEmail(String email) {
        return this.userRepository.findByEmail(email).map(UserFullDto::from);
    }

    @Override
    public UserFullDto create(UserCreateRequestDto userCreateRequestDto) {
        return UserFullDto.from(this.userRepository.save(User.of(
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
    public Optional<UserFullDto> update(String id, UserUpdateRequestDto userUpdateRequestDto) {
        return this.userRepository.findById(id).map(
                srcUser -> {
                    srcUser.setEmail(userUpdateRequestDto.getEmail());
                    srcUser.setName(userUpdateRequestDto.getName());
                    srcUser.setStudentId(userUpdateRequestDto.getStudentId());
                    srcUser.setAdmissionYear(userUpdateRequestDto.getAdmissionYear());

                    return UserFullDto.from(this.userRepository.save(srcUser));
                }
        );
    }

    @Override
    public Optional<UserFullDto> updateRole(String id, Role role) {
        return this.userRepository.findById(id).map(
                srcUser -> {
                    srcUser.setRole(role);

                    return UserFullDto.from(this.userRepository.save(srcUser));
                }
        );
    }

    @Override
    public List<UserFullDto> findByRole(Role role) {
        return this.userRepository.findByRole(role).stream().map(UserFullDto::from).collect(Collectors.toList());
    }
}
