package net.causw.app.main.domain.user.account.service.v2.implementation;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.user.UserAdmission;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.repository.user.UserAdmissionRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class AdmissionWriter {

	private final UserAdmissionRepository userAdmissionRepository;

	/**
	 * v2 방식으로 UserAdmission을 생성합니다.
	 * 학적 정보(학과, 학번, 입학년도, 재학분류)가 포함됩니다.
	 */
	public UserAdmission create(
		User user,
		List<UuidFile> attachImageUuidFiles,
		String description,
		AcademicStatus targetAcademicStatus,
		String studentId,
		Integer admissionYear,
		Department department) {

		UserAdmission admission = UserAdmission.of(
			user,
			attachImageUuidFiles,
			description,
			targetAcademicStatus,
			studentId,
			admissionYear,
			department);

		return userAdmissionRepository.save(admission);
	}
}
