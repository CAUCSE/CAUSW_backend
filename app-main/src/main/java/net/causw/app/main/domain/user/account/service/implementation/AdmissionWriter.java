package net.causw.app.main.domain.user.account.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.UserAdmissionAttachImage;
import net.causw.app.main.domain.asset.file.service.v2.implementation.FileWriter;
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
	private final FileWriter fileWriter;

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

	/**
	 * 제공된 사용자 목록에 해당하는 가입 승인 정보와 관련 증빙 서류 파일을 삭제합니다.
	 * <p>
	 * 계정 영구 삭제 시 가입 신청 기록 및 첨부된 이미지 파일을
	 * 서버 저장소에서 완전히 제거하기 위해 사용합니다.
	 * </p>
	 *
	 * @param users 가입 승인 정보를 삭제할 사용자 엔티티 목록
	 */
	public void deleteAdmissionByUsers(List<User> users) {
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

		List<UuidFile> uuidFiles = admissions.stream()
			.flatMap(admission -> admission.getUserAdmissionAttachImageList().stream())
			.map(UserAdmissionAttachImage::getUuidFile)
			.toList();

		userAdmissionRepository.deleteAll(admissions);

		if (!uuidFiles.isEmpty()) {
			fileWriter.deleteList(uuidFiles);
		}
	}
}
