package net.causw.app.main.domain.user.account.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.userInfo.UserCareer;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.entity.userInfo.UserProject;
import net.causw.app.main.domain.user.account.repository.userInfo.UserCareerRepository;
import net.causw.app.main.domain.user.account.repository.userInfo.UserInfoRepository;
import net.causw.app.main.domain.user.account.repository.userInfo.UserProjectRepository;
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
}
