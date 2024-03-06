package net.causw.adapter.persistence.port.user;

import net.causw.adapter.persistence.page.PageableFactory;
import net.causw.adapter.persistence.port.mapper.DomainModelMapper;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.repository.UserRepository;
import net.causw.application.spi.UserPort;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.model.user.UserDomainModel;
import net.causw.domain.model.enums.UserState;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Arrays;
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
    public Optional<UserDomainModel> findByRefreshToken(String refreshToken) {
        return this.userRepository.findByRefreshToken(refreshToken).map(this::entityToDomainModel);
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
                    srcUser.setProfileImage(userDomainModel.getProfileImage());

                    return this.entityToDomainModel(this.userRepository.save(srcUser));
                }
        );
    }

    @Override
    public Optional<UserDomainModel> updateRole(String id, Role newRole) {
        return this.userRepository.findById(id).map(
                srcUser -> {
                    if(srcUser.getRole().equals(Role.COMMON)){
                        srcUser.setRole(newRole);
                    }
                    else if (newRole.equals(Role.LEADER_CIRCLE)) {
                        if(!srcUser.getRole().getValue().contains("LEADER_CIRCLE")){
                            String combinedRoleValue = srcUser.getRole().getValue() + "_N_" + "LEADER_CIRCLE";
                            Role combinedRole = Role.of(combinedRoleValue.toUpperCase());
                            srcUser.setRole(combinedRole);
                        }
                        else {
                            srcUser.setRole(srcUser.getRole());
                        }
                    }
                    else if(!newRole.equals(Role.LEADER_CIRCLE) && srcUser.getRole().equals(Role.LEADER_CIRCLE)){
                        if(newRole.equals(Role.COMMON)){
                            srcUser.setRole(newRole);
                        } else{
                            String combinedRoleValue = newRole.getValue() + "_N_" + "LEADER_CIRCLE";
                            Role combinedRole = Role.of(combinedRoleValue.toUpperCase());
                            srcUser.setRole(combinedRole);
                        }
                    }
                    else {
                        srcUser.setRole(newRole);
                    }
                    return this.entityToDomainModel(this.userRepository.save(srcUser));
                }
        );
    }
    @Override
    public Optional<UserDomainModel> removeRole(String id, Role targetRole) {
        return this.userRepository.findById(id).map(
                srcUser -> {
                    if(srcUser.getRole().equals(targetRole)){
                        srcUser.setRole(Role.COMMON);
                    }
                    else if (srcUser.getRole().getValue().contains(targetRole.getValue())) {
                        if(targetRole.equals(Role.LEADER_CIRCLE)){
                            String updatedRoleValue = srcUser.getRole().getValue().replace(targetRole.getValue(), "").replace("_N_","");
                            srcUser.setRole(Role.of(updatedRoleValue));
                        } else{ //학생회 겸 동아리장, 학년대표 겸 동아리장의 경우 타깃이 동아리 장만 남기는걸로 변경
                            srcUser.setRole(Role.LEADER_CIRCLE);
                        }
                    }
                    return this.entityToDomainModel(this.userRepository.save(srcUser));
                }
        );
    }

    @Override
    public List<UserDomainModel> findByRole(String role) {

        return Arrays.stream(Role.values())
                .filter(enumRole -> enumRole.getValue().contains(role))
                .flatMap(enumRole -> this.userRepository.findByRoleAndState(enumRole, UserState.ACTIVE).stream())
                .map(this::entityToDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public UserDomainModel findByRole(Role role) {
        return this.userRepository.findByRoleAndState(role, UserState.ACTIVE).stream()
                .map(this::entityToDomainModel)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Page<UserDomainModel> findByStateAndName(UserState state, String name, Integer pageNum) {
        return this.userRepository.findByStateAndName(
                state.getValue(),
                name,
                this.pageableFactory.create(pageNum, StaticValue.USER_LIST_PAGE_SIZE)
        ).map(this::entityToDomainModel);
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

    @Override
    public Optional<UserDomainModel> updateRefreshToken(String id, String refreshToken) {
        return this.userRepository.findById(id).map(
            srcUser -> {
                srcUser.setRefreshToken(refreshToken);
                return this.entityToDomainModel(this.userRepository.save(srcUser));
            }
        );
    }
}
