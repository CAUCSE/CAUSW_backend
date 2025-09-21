package net.causw.app.main.service.user.useCase;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.model.entity.circle.Circle;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.app.main.domain.validation.UserRoleIsNoneValidator;
import net.causw.app.main.domain.validation.UserRoleValidator;
import net.causw.app.main.domain.validation.UserStateValidator;
import net.causw.app.main.domain.validation.ValidatorBucket;
import net.causw.app.main.dto.user.UserPrivilegedResponseDto;
import net.causw.app.main.dto.user.UserResponseDto;
import net.causw.app.main.dto.util.dtoMapper.UserDtoMapper;
import net.causw.app.main.service.circle.CircleEntityService;
import net.causw.app.main.service.user.UserEntityService;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.InternalServerException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindPrivilegedUsersUseCaseService {

	private final CircleEntityService circleEntityService;
	private final UserEntityService userEntityService;


	@Transactional(readOnly = true)
	public UserPrivilegedResponseDto execute(User requestUser) {
		validateRequestUser(requestUser);

		List<Role> targetRoles = Role.getPrivilegedRoles();
		List<User> privilegedUsers = userEntityService.findAllActiveUsersByRoles(targetRoles);

		List<String> leaderUserIds = new ArrayList<>();
		for (User u : privilegedUsers) {
			if (u.getRoles().contains(Role.LEADER_CIRCLE)) {
				leaderUserIds.add(u.getId());
			}
		}

		Map<String, List<Circle>> circlesByLeaderId = new HashMap<>();
		if (!leaderUserIds.isEmpty()) {
			List<Circle> circles = circleEntityService.findCirclesByLeaderIds(leaderUserIds);

			for (Circle circle : circles) {
				if (circle.getLeader().isPresent()) {
					String leaderId = circle.getLeader().get().getId();
					circlesByLeaderId.computeIfAbsent(leaderId, k -> new ArrayList<>()).add(circle);
				}
			}

			validateCircleLeaderUsersWithOutCircle(leaderUserIds, circlesByLeaderId);
		}

		Map<Role, List<User>> usersByRoleRaw = new EnumMap<>(Role.class);
		for (User u : privilegedUsers) {
			for (Role role : u.getRoles()) {
				usersByRoleRaw.computeIfAbsent(role, k -> new ArrayList<>()).add(u);
			}
		}

		Map<Role, List<UserResponseDto>> usersByRole = new EnumMap<>(Role.class);
		for (Role role : targetRoles) {
			List<UserResponseDto> dtoList = new ArrayList<>();
			List<User> roleUsers = usersByRoleRaw.getOrDefault(role, List.of());

			for (User u : roleUsers) {
				if (role == Role.LEADER_CIRCLE) {
					List<Circle> userCircles = circlesByLeaderId.getOrDefault(u.getId(), List.of());
					List<String> circleIds = new ArrayList<>();
					List<String> circleNames = new ArrayList<>();
					for (Circle c : userCircles) {
						circleIds.add(c.getId());
						circleNames.add(c.getName());
					}
					dtoList.add(UserDtoMapper.INSTANCE.toUserResponseDto(u, circleIds, circleNames));
				} else {
					dtoList.add(UserDtoMapper.INSTANCE.toUserResponseDto(u, List.of(), List.of()));
				}
			}

			usersByRole.put(role, dtoList);
		}

		return UserDtoMapper.INSTANCE.toUserPrivilegedResponseDto(
			usersByRole.getOrDefault(Role.PRESIDENT, List.of()),
			usersByRole.getOrDefault(Role.VICE_PRESIDENT, List.of()),
			usersByRole.getOrDefault(Role.COUNCIL, List.of()),
			usersByRole.getOrDefault(Role.LEADER_1, List.of()),
			usersByRole.getOrDefault(Role.LEADER_2, List.of()),
			usersByRole.getOrDefault(Role.LEADER_3, List.of()),
			usersByRole.getOrDefault(Role.LEADER_4, List.of()),
			usersByRole.getOrDefault(Role.LEADER_CIRCLE, List.of()),
			usersByRole.getOrDefault(Role.LEADER_ALUMNI, List.of()),
			usersByRole.getOrDefault(Role.ALUMNI_MANAGER, List.of())
		);
	}


	private void validateRequestUser(User requester) {
		Set<Role> roles = requester.getRoles();
		ValidatorBucket.of()
			.consistOf(UserStateValidator.of(requester.getState()))
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.consistOf(UserRoleValidator.of(roles, Set.of()))
			.validate();
	}

	/**
	 * cirlce 이 존재하지 않는 circleLeader 발견 시 오류 발생
	 * @param leaderUserIds 동아리 리더 유저 아이디 리스트
	 */
	private void validateCircleLeaderUsersWithOutCircle(
		List<String> leaderUserIds,
		Map<String, List<Circle>> circlesByLeaderId
	) {
		List<String> leadersWithoutCircle = leaderUserIds.stream()
			.filter(leaderId -> !circlesByLeaderId.containsKey(leaderId) || circlesByLeaderId.get(leaderId).isEmpty())
			.toList();

		if (!leadersWithoutCircle.isEmpty()) {
			throw new InternalServerException(
				ErrorCode.INTERNAL_SERVER,
				MessageUtil.NO_ASSIGNED_CIRCLE_FOR_LEADER
			);
		}
	}

}
