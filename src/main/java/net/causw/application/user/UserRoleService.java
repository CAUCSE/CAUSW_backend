package net.causw.application.user;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.repository.user.UserRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.application.dto.user.UserUpdateRoleRequestDto;
import net.causw.application.dto.util.dtoMapper.UserDtoMapper;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.enums.user.UserState;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.policy.domain.RolePolicy;
import net.causw.domain.validation.DelegatableRoleValidator;
import net.causw.domain.validation.GrantableRoleValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserRoleService {
    private final UserRepository userRepository;

    /**
     * 자신의 권한을 넘겨주는 권한 위임
     *
     * @param delegator 위임자
     * @param delegateeId 피위임자의 id
     * @param userUpdateRoleRequestDto 위임할 권한
     * @return 권한 위임이 완료된 피위임자
     */
    @Transactional
    public UserResponseDto delegateRole(
            User delegator,
            String delegateeId,
            UserUpdateRoleRequestDto userUpdateRoleRequestDto
    ) {
        // 피위임자의 Id로 피위임자 조회
        User delegatee = userRepository.findById(delegateeId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
        );

        // 권한을 모두 조회
        Set<Role> delegatorRoles = delegator.getRoles();
        Set<Role> delegateeRoles = delegatee.getRoles();
        Role delegatedRole = userUpdateRoleRequestDto.getRole();

        // 예외 처리
        DelegatableRoleValidator.of(
                delegatorRoles,
                delegatedRole,
                delegateeRoles
        ).validate();

        // 학생회장 권한 위임 시 부학생 및 학생회 권한 삭제
        if (delegatedRole.equals(Role.PRESIDENT)) {
            removeAllStudentCouncil();
        }

        // 고유 권한일 경우 모든 사용자로부터 권한 삭제
        else if (RolePolicy.getRoleUnique(delegatedRole)) {
            removeAllRole(delegatedRole);
        }

        // 위임자의 권한 삭제
        else {
            removeRole(delegator, delegatedRole);
        }

        // 피위임자에게 권한 설정
        return UserDtoMapper.INSTANCE.toUserResponseDto(
                updateRole(delegatee, delegatedRole), null, null);
    }

    /**
     * 타인의 권한을 설정하는 권한 부여
     * <pre>grantorId가 존재할 시 위임의 형태가 된다.<pre/>
     *
     * @param grantor 부여자
     * @param delegatorId 피위임자의 id
     * @param granteeId 수혜자의 id
     * @param userUpdateRoleRequestDto 부여할 권한
     * @return 권한 부여가 완료된 수혜자
     */
    @Transactional
    public UserResponseDto grantRole(
            User grantor, // 부여자
            String delegatorId, // 위임자
            String granteeId, // 수혜자
            UserUpdateRoleRequestDto userUpdateRoleRequestDto
    ) {
        // 위임자의 Id로 위임자 조회
        User delegator = StringUtils.isBlank(delegatorId) ? null : userRepository.findById(delegatorId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
        );

        // 수혜자의 Id로 수혜자 조회
        User grantee = userRepository.findById(granteeId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
        );

        // 권한을 모두 조회
        Role grantedRole = userUpdateRoleRequestDto.getRole();

        // 예외 처리
        GrantableRoleValidator.of(
                grantor.getRoles(),
                delegator,
                grantedRole,
                grantee
        ).validate();

        // 학생회장 권한 부여 시 부학생 및 학생회 권한 삭제
        if (grantedRole.equals(Role.PRESIDENT)) {
            removeAllStudentCouncil();
        }

        // 고유 권한일 경우 모든 사용자로부터 권한 삭제
        else if (RolePolicy.getRoleUnique(grantedRole)) {
            removeAllRole(grantedRole);
        }

        // 위임자가 있을 경우 위임자의 권한 삭제
        else if (delegator != null) {
            removeRole(delegator, grantedRole);
        }

        // 수혜자에게 권한 설정
        return UserDtoMapper.INSTANCE.toUserResponseDto(
                updateRole(grantee, grantedRole), null, null);
    }

    public User updateRole(User targetUser, Role newRole) {
        Set<Role> roles = new HashSet<>(targetUser.getRoles());

        roles.clear(); // 겸직 불가
        roles.add(newRole);

        targetUser.setRoles(roles);
        return userRepository.save(targetUser);
    }

    public void removeRole(User targetUser, Role targetRole) {
        Set<Role> roles = new HashSet<>(targetUser.getRoles());

        roles.remove(targetRole);

        // 권한이 비어 있을 시 COMMON을 부여 단 NONE을 삭제해 빈 경우 NONE을 유지
        if (roles.isEmpty()) {
            if (targetRole.equals(Role.NONE)) {
                roles.add(Role.NONE);
            }
            else {
                roles.add(Role.COMMON);
            }
        }

        targetUser.setRoles(roles);
        userRepository.save(targetUser);
    }

    // --- Private Methods ---
    private void removeAllRole(Role targetRole) {
        List<User> targetUsers = userRepository.findByRoleAndState(targetRole, UserState.ACTIVE);

        targetUsers.forEach(user -> removeRole(user, targetRole));
    }

    private void removeAllStudentCouncil() {
        // 학생회장 권한 위임 시 부학생 및 학생회 권한 삭제
        removeAllRole(Role.PRESIDENT);
        removeAllRole(Role.VICE_PRESIDENT);
        removeAllRole(Role.COUNCIL);
    }
}
