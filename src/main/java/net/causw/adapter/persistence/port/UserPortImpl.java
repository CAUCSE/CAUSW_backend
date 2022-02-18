package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.PageableFactory;
import net.causw.adapter.persistence.User;
import net.causw.adapter.persistence.UserRepository;
import net.causw.application.spi.UserPort;
import net.causw.domain.model.Role;
import net.causw.domain.model.StaticValue;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.model.UserState;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserPortImpl extends DomainModelMapper implements UserPort {
    private final UserRepository userRepository;
    private final PageableFactory pageableFactory;

    public UserPortImpl(
            UserRepository userRepository,
            PageableFactory pageableFactory
    ) {
        this.userRepository = userRepository;
        this.pageableFactory = pageableFactory;
    }

    @Override
    public Optional<UserDomainModel> findForPassword(String email, String name, String studentId) {
        return this.userRepository.findByEmailAndNameAndStudentId(email, name, studentId).map(this::entityToDomainModel);
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
    public Page<UserDomainModel> findByState(UserState state, Integer pageNum) {
        Page<User> users = this.userRepository.findByStateOrderByCreatedAtAsc(
                state,
                this.pageableFactory.create(pageNum, StaticValue.USER_LIST_PAGE_SIZE)
        );
        return users
                .map(this::entityToDomainModel);
    }

    @Override
    public Optional<UserDomainModel> updatePassword(String id, String password) {
        return this.userRepository.findById(id).map(
                srcUser -> {
                    srcUser.setPassword(password);

                    return this.entityToDomainModel(this.userRepository.save(srcUser));
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
}
