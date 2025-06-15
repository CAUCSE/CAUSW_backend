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
import net.causw.domain.validation.GrantableRoleValidator;
import net.causw.domain.validation.UpdatableRoleValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class UserRoleService {
    private final UserRepository userRepository;

    @Transactional
    public UserResponseDto grantUserRole(
            User grantor, // 위임인
            String granteeId, // 피위임인
            UserUpdateRoleRequestDto userUpdateRoleRequestDto
    ) {
        // 피위임인의 Id로 피위임인 조회
        User grantee = userRepository.findById(granteeId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
        );

        // 권한을 모두 조회
        Set<Role> grantorRoles = grantor.getRoles();
        Set<Role> granteeRoles = grantee.getRoles();
        Role grantedRole = userUpdateRoleRequestDto.getRole();

        // 예외 처리
        GrantableRoleValidator.of(
                grantorRoles,
                grantedRole,
                granteeRoles
        ).validate();

        // 학생회장 권한 위임 시 부학생 및 학생회 권한 삭제
        if (grantedRole.equals(Role.PRESIDENT)) {
            removeAllRole(Role.VICE_PRESIDENT);
            removeAllRole(Role.COUNCIL);
        }

        if (grantedRole.isUnique()) {
            removeAllRole(grantedRole);
        }

        // 위임인의 권한 삭제
        removeRole(grantor, grantedRole);

        // 피위임인에게 권한 설정
        return UserDtoMapper.INSTANCE.toUserResponseDto(
                updateRole(grantee, grantedRole), null, null);
    }

    @Transactional
    public UserResponseDto updateUserRole(
            User user,
            String grantorId, // 위임인
            String granteeId, // 피위임인
            UserUpdateRoleRequestDto userUpdateRoleRequestDto
    ) {
        // 위임인의 Id로 위임인 조회
        User grantor = StringUtils.isBlank(grantorId) ? null : userRepository.findById(granteeId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
        );

        // 피위임인의 Id로 피위임인 조회
        User grantee = userRepository.findById(granteeId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
        );

        // 권한을 모두 조회
        Set<Role> grantorRoles = grantor == null ? null : grantor.getRoles();
        Set<Role> granteeRoles = grantee.getRoles();
        Role grantedRole = userUpdateRoleRequestDto.getRole();

        // 예외 처리
        UpdatableRoleValidator.of(
                user.getRoles(),
                grantorRoles,
                grantedRole,
                granteeRoles
        ).validate();

        // 학생회장 권한 위임 시 부학생 및 학생회 권한 삭제
        if (grantedRole.equals(Role.PRESIDENT)) {
            removeAllRole(Role.VICE_PRESIDENT);
            removeAllRole(Role.COUNCIL);
        }

        // 고유 권한일 경우 모든 사용자로부터 권한 삭제
        if (grantedRole.isUnique()) {
            removeAllRole(grantedRole);
        }

        // 고유 권한이 아니고 위임인이 있을 경우 위임인 권한 삭제(관리자 및 기본 권한 제외)
        else if (grantor != null && grantorRoles.contains(grantedRole)
                && !RolePolicy.NON_GRANTABLE_ROLES.contains(grantedRole)) {
            removeRole(grantor, grantedRole);
        }

        // 피위임인에게 권한 설정
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

    // private methods
    private void removeAllRole(Role targetRole) {
        List<User> targetUsers = userRepository.findByRoleAndState(targetRole, UserState.ACTIVE);

        targetUsers.forEach(user -> removeRole(user, targetRole));
    }
}
