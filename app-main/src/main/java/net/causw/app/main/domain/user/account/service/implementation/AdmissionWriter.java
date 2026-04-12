package net.causw.app.main.domain.user.account.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.UserAdmissionAttachImage;
import net.causw.app.main.domain.asset.file.service.v2.UuidFileService;
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
	private final UuidFileService uuidFileService;

	/**
	 * v2 방식으로 UserAdmission을 생성합니다.
	 */
	public UserAdmission create(
		User user,
		List<UuidFile> attachImageUuidFiles,
		String description,
		AcademicStatus requestedAcademicStatus,
		String requestedStudentId,
		Integer requestedAdmissionYear,
		Department requestedDepartment,
		Integer requestedGraduationYear) {

		UserAdmission admission = UserAdmission.of(
			user,
			attachImageUuidFiles,
			description,
			requestedAcademicStatus,
			requestedStudentId,
			requestedAdmissionYear,
			requestedDepartment,
			requestedGraduationYear);

		return userAdmissionRepository.save(admission);
	}

	/**
	 * UserAdmission을 삭제합니다.
	 */
	public void delete(UserAdmission admission) {
		userAdmissionRepository.delete(admission);
	}

	public void deleteByUsers(List<User> users) {
		List<String> userIds = users.stream()
			.map(User::getId)
			.toList();

		if (userIds.isEmpty()) {
			return;
		}

		List<UserAdmission> admissions = userAdmissionRepository.findAllByUser_IdIn(userIds);
		if (admissions.isEmpty()) {
			return;
		}

		List<String> fileIds = admissions.stream()
			.flatMap(admission -> admission.getUserAdmissionAttachImageList().stream())
			.map(UserAdmissionAttachImage::getUuidFile)
			.map(UuidFile::getId)
			.toList();

		if (!fileIds.isEmpty()) {
			uuidFileService.deleteFileList(fileIds);
		}

		userAdmissionRepository.deleteAll(admissions);
	}
}
