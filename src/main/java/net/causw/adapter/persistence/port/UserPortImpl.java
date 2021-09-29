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
    public List<UserDomainModel> findByName(String name) {
        return this.userRepository.findByName(name)
                .stream()
                .map(this::entityToDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserDomainModel> findByEmail(String email) {
        return this.userRepository.findByEmail(email).map(this::entityToDomainModel);
    }

    @Override
    public UserDomainModel create(UserDomainModel userDomainModel) {
        return this.entityToDomainModel(this.userRepository.save(User.from(userDomainModel)));
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
        return this.userRepository.findByRole(role)
                .stream()
                .map(this::entityToDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserDomainModel> updatePassword(String id, String password) {
        return this.userRepository.findById(id).map(
                srcUser -> {
                    srcUser.setPassword(password);

                    return this. entityToDomainModel(this.userRepository.save(srcUser));
                }
        );
    }

    @Override
    public Optional<UserDomainModel> updateState(String id, UserState state) {
        return this.userRepository.findById(id).map(
                srcUser -> {
                    srcUser.setState(state);

                    return this.entityToDomainModel(this.userRepository.save(srcUser));
                }
        );
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
