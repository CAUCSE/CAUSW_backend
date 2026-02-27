package net.causw.app.main.shared.seed;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.UserAcademicRecordApplicationAttachImage;
import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.api.v1.dto.UserCreateRequestDto;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.user.UserAdmissionLog;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserAdmissionLogAction;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile("seed")
@RequiredArgsConstructor
@Slf4j
public class UserSeeder {

	private final EntityManager em;
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;

	@Transactional
	public void seed(int count) {

		boolean exist = userRepository.existsBy();

		if (exist) {
			log.warn("🚫 Seed skipped: users already exist");
			return;
		}
		// getOrCreateAdmin();
		process(count);
	}

	private void process(int count) {
		int batchSize = 500;

		String encodedPassword = passwordEncoder.encode("password00!!");
		for (int i = 1; i <= count; i++) {

			User user = createActiveCommonUser(i, encodedPassword);

			UserAdmissionLog log = createUserAdmissionLog(user);
			createEnrolledAcademicRecord(user, 2026);

			// batch flush
			if (i % batchSize == 0) {
				em.flush();
				em.clear();
				System.out.println("Seeded users: " + i);
			}
		}

		em.flush();
		em.clear();
	}

	private UserAdmissionLog createUserAdmissionLog(User user) {
		UserAdmissionLog log = UserAdmissionLog.ofV1(
			user.getEmail(),
			user.getName(),
			"admin@seed.test",
			"시드 관리자",
			UserAdmissionLogAction.ACCEPT,
			List.of(),
			"시드 데이터 - 가입 승인 완료",
			null);
		em.persist(log);

		return log;
	}

	private User createActiveCommonUser(int i, String encodedPassword) {
		UserCreateRequestDto dto = UserCreateRequestDto.builder()
			.email("seed" + i + "@cau.ac.kr")
			.name("시드유저" + i)
			.password("password00!!") // 정규식 통과
			.studentId("2020" + String.format("%04d", i))
			.admissionYear(2020)
			.nickname("seed_" + i)
			.major("소프트웨어학부")
			.department(Department.SCHOOL_OF_SW)
			.phoneNumber(String.format("010-%04d-%04d", 2000 + i / 10000, i % 10000))
			.build();

		User user = User.from(dto, encodedPassword);
		user.setRoles(Set.of(Role.COMMON));
		user.setState(UserState.ACTIVE);
		em.persist(user);

		// UUID File (가짜 파일)
		UuidFile file = UuidFile.of(UUID.randomUUID().toString(), "seed/profile/" + i + ".png",
			"https://cdn.seed.test/profile/" + i + ".png", "profile_" + i + ".png", "png", FilePath.ETC);
		em.persist(file);
		// User File 관계 테이블
		UserProfileImage profile = UserProfileImage.of(user, file);
		em.persist(profile);

		return user;
	}

	private void createEnrolledAcademicRecord(
		User user,
		int completedSemester) {
		// 1. User 상태 확정
		user.setAcademicStatus(AcademicStatus.ENROLLED);
		user.setCurrentCompletedSemester(completedSemester);
		user.setState(UserState.ACTIVE);

		// 2. 학적 증빙 신청서 (이미 ACCEPT 된 상태)
		UserAcademicRecordApplication application = UserAcademicRecordApplication.create(
			user,
			AcademicRecordRequestStatus.ACCEPT,
			AcademicStatus.ENROLLED,
			completedSemester,
			"시드 데이터 - 재학 인증 완료");

		em.persist(application);

		// 3. 첨부 이미지 1개
		UuidFile recordFile = UuidFile.of(
			UUID.randomUUID().toString(),
			"seed/academic-record/" + user.getId() + ".png",
			"https://cdn.seed.test/academic-record/" + user.getId() + ".png",
			"academic_record.png",
			"png",
			FilePath.ETC);
		em.persist(recordFile);

		UserAcademicRecordApplicationAttachImage attach = UserAcademicRecordApplicationAttachImage.of(application,
			recordFile);
		em.persist(attach);
	}
}