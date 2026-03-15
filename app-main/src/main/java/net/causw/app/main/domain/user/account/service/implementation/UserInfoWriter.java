package net.causw.app.main.domain.user.account.service.implementation;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.userInfo.UserCareer;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.entity.userInfo.UserProject;
import net.causw.app.main.domain.user.account.repository.userInfo.UserCareerRepository;
import net.causw.app.main.domain.user.account.repository.userInfo.UserInfoRepository;
import net.causw.app.main.domain.user.account.repository.userInfo.UserProjectRepository;
import net.causw.app.main.domain.user.account.service.dto.request.UserCareerCommand;
import net.causw.app.main.domain.user.account.service.dto.request.UserProjectCommand;
import net.causw.app.main.shared.entity.BaseEntity;
import net.causw.app.main.shared.exception.errorcode.UserInfoErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class UserInfoWriter {

	private final UserInfoRepository userInfoRepository;
	private final UserCareerRepository userCareerRepository;
	private final UserProjectRepository userProjectRepository;

	public UserInfo save(UserInfo userInfo) {
		return userInfoRepository.save(userInfo);
	}

	public UserCareer saveCareer(UserCareer userCareer) {
		return userCareerRepository.save(userCareer);
	}

	public UserProject saveProject(UserProject userProject) {
		return userProjectRepository.save(userProject);
	}

	public UserCareer getCareer(String id) {
		return userCareerRepository.findById(id)
			.orElseThrow(UserInfoErrorCode.USER_CAREER_NOT_FOUND::toBaseException);
	}

	public UserProject getProject(String id) {
		return userProjectRepository.findById(id)
			.orElseThrow(UserInfoErrorCode.USER_PROJECT_NOT_FOUND::toBaseException);
	}

	public List<String> findCareerByUserInfoId(String userInfoId) {
		return userCareerRepository
			.findAllCareerByUserInfoIdOrderByStartYearDescStartMonthDesc(userInfoId).stream()
			.map(BaseEntity::getId)
			.toList();
	}

	public List<String> findProjectByUserInfoId(String userInfoId) {
		return userProjectRepository
			.findAllProjectByUserInfoIdOrderByStartYearDescStartMonthDesc(userInfoId).stream()
			.map(BaseEntity::getId)
			.toList();
	}

	public void deleteCareerByIds(List<String> ids) {
		userCareerRepository.deleteAllByIdInBatch(ids);
	}

	public void deleteProjectByIds(List<String> ids) {
		userProjectRepository.deleteAllByIdInBatch(ids);
	}

	/**
	 *
	 * @param dtoList
	 * @param userInfo
	 */
	public void syncCareers(List<UserCareerCommand> dtoList, UserInfo userInfo) {
		if (dtoList == null)
			return;
		userInfo.validateCareerCount(dtoList.size());
		Set<String> requests = new HashSet<>();
		int currentYear = LocalDate.now().getYear();

		for (UserCareerCommand dto : dtoList) {
			dto.validateDate(currentYear);
			if (dto.id() == null) {
				UserCareer created = saveCareer(UserCareer.of(userInfo,
					dto.startYear(), dto.startMonth(),
					dto.endYear(), dto.endMonth(),
					dto.description()));
				requests.add(created.getId());
			} else {
				UserCareer exists = getCareer(dto.id());
				exists.update(dto.startYear(), dto.startMonth(),
					dto.endYear(), dto.endMonth(), dto.description());
				requests.add(dto.id());
			}
		}

		List<String> toDelete = findCareerByUserInfoId(userInfo.getId()).stream()
			.filter(id -> !requests.contains(id))
			.toList();
		deleteCareerByIds(toDelete);
	}

	public void syncProjects(List<UserProjectCommand> dtoList, UserInfo userInfo) {
		if (dtoList == null)
			return;
		userInfo.validateProjectCount(dtoList.size());
		Set<String> requests = new HashSet<>();
		int currentYear = LocalDate.now().getYear();

		for (UserProjectCommand dto : dtoList) {
			dto.validateDate(currentYear);
			if (dto.id() == null) {
				UserProject created = saveProject(UserProject.of(userInfo,
					dto.startYear(), dto.startMonth(),
					dto.endYear(), dto.endMonth(),
					dto.description()));
				requests.add(created.getId());
			} else {
				UserProject exists = getProject(dto.id());
				exists.update(dto.startYear(), dto.startMonth(),
					dto.endYear(), dto.endMonth(), dto.description());
				requests.add(dto.id());
			}
		}

		List<String> toDelete = findProjectByUserInfoId(userInfo.getId()).stream()
			.filter(id -> !requests.contains(id))
			.toList();
		deleteProjectByIds(toDelete);
	}
}
