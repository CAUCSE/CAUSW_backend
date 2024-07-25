package net.causw.adapter.persistence.port.user;

import net.causw.adapter.persistence.page.PageableFactory;
import net.causw.adapter.persistence.port.mapper.DomainModelMapper;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.repository.UserRepository;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.RedisUtils;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.model.user.UserDomainModel;
import net.causw.domain.model.enums.UserState;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserPortImpl extends DomainModelMapper implements UserPort {
    private final UserRepository userRepository;
    private final PageableFactory pageableFactory;

    private final RedisUtils redisUtils;

    public UserPortImpl(
            UserRepository userRepository,
            PageableFactory pageableFactory,
            RedisUtils redisUtils
    ) {

        this.userRepository = userRepository;
        this.pageableFactory = pageableFactory;
        this.redisUtils = redisUtils;
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
        return this.userRepository.findById(getUserIdFromRefreshToken(refreshToken)).map(this::entityToDomainModel);
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
                    Set<Role> roles = srcUser.getRoles();
                    if(roles.contains(Role.COMMON)){
                        roles.remove(Role.COMMON);
                    }
                    roles.add(newRole);
                    return this.entityToDomainModel(this.userRepository.save(srcUser));
                }
        );
    }

    @Override
    public Optional<UserDomainModel> removeRole(String id, Role targetRole) {
        return this.userRepository.findById(id).map(
                srcUser -> {
                    Set<Role> roles = srcUser.getRoles();
                    if(roles.contains(targetRole)){
                        roles.remove(targetRole);
                        roles.add(Role.COMMON);
                    }
                    srcUser.setRoles(roles);
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
    public Page<UserDomainModel> findByStateAndName(String state, String name, Integer pageNum) {

        if(state.equals("INACTIVE_N_DROP")){
            List<String> statesToSearch = Arrays.asList("INACTIVE", "DROP");
            return this.userRepository.findByStateInAndNameContaining(
                    statesToSearch,
                    name,
                    this.pageableFactory.create(pageNum, StaticValue.USER_LIST_PAGE_SIZE)
            ).map(this::entityToDomainModel);
        }


        return this.userRepository.findByStateAndName(
                state,
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
    public void updateRefreshToken(String id, String refreshToken) {
        redisUtils.setData(refreshToken,id,StaticValue.JWT_REFRESH_TOKEN_VALID_TIME);
    }

    @Override
    public String getUserIdFromRefreshToken(String refreshToken) {
        return Optional.ofNullable(redisUtils.getData(refreshToken))
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "RefreshToken 유효성 검증 실패"));
    }

    @Override
    public void signOut(String refreshToken, String accessToken) {
        redisUtils.addToBlacklist(accessToken);
        redisUtils.deleteData(refreshToken);
    }
}
