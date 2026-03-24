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

import lombok.RequiredArgsConstructor;

/**
 * 동문 수첩(UserInfo) 및 관련 엔티티(경력, 프로젝트)의 저장·삭제·동기화 전담.
 */
@Component
@RequiredArgsConstructor
@Transactional
public class UserInfoWriter {

	private final UserInfoRepository userInfoRepository;
	private final UserInfoReader userInfoReader;
	private final UserCareerRepository userCareerRepository;
	private final UserProjectRepository userProjectRepository;

	/**
	 * 동문 수첩 프로필 저장
	 * @param userInfo 저장할 동문 수첩 프로필
	 * @return 저장된 동문 수첩 프로필
	 */
	public UserInfo save(UserInfo userInfo) {
		return userInfoRepository.save(userInfo);
	}

	/**
	 * 사용자가 입력한 커리어 리스트와 기존 저장 데이터를 비교하여 동기화한다.
	 * 신규는 생성, 기존은 수정, 요청에 없는 항목은 삭제한다.
	 *
	 * @param dtoList 사용자가 입력한 커리어 리스트
	 * @param userInfo 커리어가 속한 동문 수첩 프로필
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
				UserCareer exists = userInfoReader.getCareer(dto.id());
				exists.update(dto.startYear(), dto.startMonth(),
					dto.endYear(), dto.endMonth(), dto.description());
				requests.add(dto.id());
			}
		}

		List<String> toDelete = userInfoReader.findCareerIdsByUserInfoId(userInfo.getId()).stream()
			.filter(id -> !requests.contains(id))
			.toList();
		deleteCareerByIds(toDelete);
	}

	/**
	 * 사용자가 입력한 프로젝트 리스트와 기존 저장 데이터를 비교하여 동기화한다.
	 * 신규는 생성, 기존은 수정, 요청에 없는 항목은 삭제한다.
	 *
	 * @param dtoList 사용자가 입력한 프로젝트 리스트
	 * @param userInfo 프로젝트가 속한 동문 수첩 프로필
	 */
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
				UserProject exists = userInfoReader.getProject(dto.id());
				exists.update(dto.startYear(), dto.startMonth(),
					dto.endYear(), dto.endMonth(), dto.description());
				requests.add(dto.id());
			}
		}

		List<String> toDelete = userInfoReader.findProjectIdsByUserInfoId(userInfo.getId()).stream()
			.filter(id -> !requests.contains(id))
			.toList();
		deleteProjectByIds(toDelete);
	}

	/**
	 * 경력 저장
	 * @param userCareer 저장할 경력
	 * @return 저장된 경력
	 */
	private UserCareer saveCareer(UserCareer userCareer) {
		return userCareerRepository.save(userCareer);
	}

	/**
	 * 프로젝트 저장
	 * @param userProject 저장할 프로젝트
	 * @return 저장된 프로젝트
	 */
	private UserProject saveProject(UserProject userProject) {
		return userProjectRepository.save(userProject);
	}

	/**
	 * 경력 ID 목록 일괄 삭제
	 * @param ids 삭제할 경력 ID 목록
	 */
	private void deleteCareerByIds(List<String> ids) {
		userCareerRepository.deleteAllByIdInBatch(ids);
	}

	/**
	 * 프로젝트 ID 목록 일괄 삭제
	 * @param ids 삭제할 프로젝트 ID 목록
	 */
	private void deleteProjectByIds(List<String> ids) {
		userProjectRepository.deleteAllByIdInBatch(ids);
	}
}
