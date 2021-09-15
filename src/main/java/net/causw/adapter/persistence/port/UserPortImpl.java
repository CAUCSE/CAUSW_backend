package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.User;
import net.causw.adapter.persistence.UserRepository;
import net.causw.application.spi.UserPort;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserDomainModel;
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
    public Optional<UserDomainModel> findById(String id) {
        return this.userRepository.findById(id).map(this::entityToDomainModel);
    }

    @Override
    public Optional<UserDomainModel> findByName(String name) {
        return this.userRepository.findByName(name).map(this::entityToDomainModel);
    }

    @Override
    public Optional<UserDomainModel> findByEmail(String email) {
        return this.userRepository.findByEmail(email).map(this::entityToDomainModel);
    }

    @Override
    public UserDomainModel create(UserDomainModel userDomainModel) {
        // TODO : Remove following -> Default로 Role.NONE 지정
        Role role = Role.NONE;
        if (userDomainModel.getEmail().equals("admin@gmail.com")) {
            role = Role.ADMIN;
        }

        return this.entityToDomainModel(this.userRepository.save(User.of(
                userDomainModel.getEmail(),
                userDomainModel.getName(),
                userDomainModel.getPassword(),
                userDomainModel.getStudentId(),
                userDomainModel.getAdmissionYear(),
                role,
                UserState.ACTIVE  // TODO : User Auth 개발 후 UserState.WAIT 으로 바꿀 것!!!
        )));
    }

    @Override
    public Optional<UserDomainModel> update(String id, UserDomainModel userDomainModel) {
        return this.userRepository.findById(id).map(
                srcUser -> {
                    srcUser.setEmail(userDomainModel.getEmail());
                    srcUser.setName(userDomainModel.getName());
                    srcUser.setStudentId(userDomainModel.getStudentId());
                    srcUser.setAdmissionYear(userDomainModel.getAdmissionYear());

                    return this.entityToDomainModel(this.userRepository.save(srcUser));
                }
        );
    }

    @Override
    public Optional<UserDomainModel> updateRole(String id, Role role) {
        return this.userRepository.findById(id).map(
                srcUser -> {
                    srcUser.setRole(role);

                    return this.entityToDomainModel(this.userRepository.save(srcUser));
                }
        );
    }

    @Override
    public List<UserDomainModel> findByRole(Role role) {
        return this.userRepository.findByRole(role).stream().map(this::entityToDomainModel).collect(Collectors.toList());
    }

    private UserDomainModel entityToDomainModel(User user) {
        return UserDomainModel.of(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPassword(),
                user.getStudentId(),
                user.getAdmissionYear(),
                user.getRole(),
                user.getProfileImage(),
                user.getState()
        );
    }
}
