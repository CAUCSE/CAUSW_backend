package net.causw.app.main.domain.user.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.user.UserAdmission;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.v2.dto.AdmissionListCondition;
import net.causw.app.main.domain.user.account.service.v2.dto.AdmissionResult;
import net.causw.app.main.domain.user.account.service.v2.implementation.AdmissionReader;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
class AdmissionAdminServiceTest {

	@Mock
	private AdmissionReader admissionReader;

	@InjectMocks
	private AdmissionAdminService admissionAdminService;

	/* =========================
	 * getAdmissionList 테스트
	 * ========================= */
	@Nested
	@DisplayName("getAdmissionList - 관리자 재학인증 신청 목록 조회")
	class GetAdmissionList {

		@Test
		@DisplayName("조건 없이 전체 신청 목록을 조회한다")
		void givenNoCondition_whenGetAdmissionList_thenReturnAllAdmissions() {
			// given
			AdmissionListCondition condition = new AdmissionListCondition(null, null);
			Pageable pageable = PageRequest.of(0, 10);

			User user1 = ObjectFixtures.getUserWithId("user-1");
			User user2 = ObjectFixtures.getUserWithId("user-2");
			UserAdmission admission1 = ObjectFixtures.getUserAdmissionWithId("admission-1", user1);
			UserAdmission admission2 = ObjectFixtures.getUserAdmissionWithId("admission-2", user2);

			Page<UserAdmission> admissionPage = new PageImpl<>(
				List.of(admission1, admission2), pageable, 2);

			when(admissionReader.findAdmissionList(null, null, pageable))
				.thenReturn(admissionPage);

			// when
			Page<AdmissionResult> result = admissionAdminService.getAdmissionList(condition, pageable);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getTotalElements()).isEqualTo(2);
			assertThat(result.getContent()).hasSize(2);
			assertThat(result.getContent().get(0).id()).isEqualTo("admission-1");
			assertThat(result.getContent().get(1).id()).isEqualTo("admission-2");

			verify(admissionReader).findAdmissionList(null, null, pageable);
		}

		@Test
		@DisplayName("키워드로 필터링하여 신청 목록을 조회한다")
		void givenKeyword_whenGetAdmissionList_thenReturnFilteredAdmissions() {
			// given
			AdmissionListCondition condition = new AdmissionListCondition("name", null);
			Pageable pageable = PageRequest.of(0, 10);

			User user = ObjectFixtures.getUserWithId("user-1");
			UserAdmission admission = ObjectFixtures.getUserAdmissionWithId("admission-1", user);

			Page<UserAdmission> admissionPage = new PageImpl<>(
				List.of(admission), pageable, 1);

			when(admissionReader.findAdmissionList("name", null, pageable))
				.thenReturn(admissionPage);

			// when
			Page<AdmissionResult> result = admissionAdminService.getAdmissionList(condition, pageable);

			// then
			assertThat(result.getTotalElements()).isEqualTo(1);
			assertThat(result.getContent().get(0).userName()).isEqualTo(user.getName());

			verify(admissionReader).findAdmissionList("name", null, pageable);
		}

		@Test
		@DisplayName("사용자 상태로 필터링하여 신청 목록을 조회한다")
		void givenUserState_whenGetAdmissionList_thenReturnFilteredAdmissions() {
			// given
			AdmissionListCondition condition = new AdmissionListCondition(null, UserState.AWAIT);
			Pageable pageable = PageRequest.of(0, 10);

			User user = ObjectFixtures.getUserWithId("user-1");
			UserAdmission admission = ObjectFixtures.getUserAdmissionWithId("admission-1", user);

			Page<UserAdmission> admissionPage = new PageImpl<>(
				List.of(admission), pageable, 1);

			when(admissionReader.findAdmissionList(null, UserState.AWAIT, pageable))
				.thenReturn(admissionPage);

			// when
			Page<AdmissionResult> result = admissionAdminService.getAdmissionList(condition, pageable);

			// then
			assertThat(result.getTotalElements()).isEqualTo(1);
			assertThat(result.getContent().get(0).userState()).isEqualTo(UserState.AWAIT);

			verify(admissionReader).findAdmissionList(null, UserState.AWAIT, pageable);
		}

		@Test
		@DisplayName("결과가 없으면 빈 페이지를 반환한다")
		void givenNoResult_whenGetAdmissionList_thenReturnEmptyPage() {
			// given
			AdmissionListCondition condition = new AdmissionListCondition("없는이름", null);
			Pageable pageable = PageRequest.of(0, 10);

			Page<UserAdmission> emptyPage = new PageImpl<>(List.of(), pageable, 0);

			when(admissionReader.findAdmissionList("없는이름", null, pageable))
				.thenReturn(emptyPage);

			// when
			Page<AdmissionResult> result = admissionAdminService.getAdmissionList(condition, pageable);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getTotalElements()).isZero();
			assertThat(result.getContent()).isEmpty();

			verify(admissionReader).findAdmissionList("없는이름", null, pageable);
		}

		@Test
		@DisplayName("AdmissionResult로 올바르게 매핑되는지 확인한다")
		void givenAdmission_whenGetAdmissionList_thenResultFieldsMappedCorrectly() {
			// given
			AdmissionListCondition condition = new AdmissionListCondition(null, null);
			Pageable pageable = PageRequest.of(0, 10);

			User user = ObjectFixtures.getUserWithId("user-1");
			UserAdmission admission = ObjectFixtures.getUserAdmissionWithId("admission-1", user);

			Page<UserAdmission> admissionPage = new PageImpl<>(
				List.of(admission), pageable, 1);

			when(admissionReader.findAdmissionList(null, null, pageable))
				.thenReturn(admissionPage);

			// when
			Page<AdmissionResult> result = admissionAdminService.getAdmissionList(condition, pageable);

			// then
			AdmissionResult item = result.getContent().get(0);
			assertThat(item.id()).isEqualTo("admission-1");
			assertThat(item.userName()).isEqualTo(user.getName());
			assertThat(item.userEmail()).isEqualTo(user.getEmail());
			assertThat(item.requestedDepartment()).isEqualTo(Department.SCHOOL_OF_SW);
			assertThat(item.requestedAdmissionYear()).isEqualTo(2023);
			assertThat(item.requestedStudentId()).isEqualTo("20231234");
			assertThat(item.requestedAcademicStatus()).isEqualTo(AcademicStatus.ENROLLED);
			assertThat(item.description()).isEqualTo("재학증명서 첨부합니다");
			assertThat(item.attachImageUrls()).hasSize(1);
			assertThat(item.userState()).isEqualTo(UserState.AWAIT);
		}
	}

	/* =========================
	 * getAdmissionDetail 테스트
	 * ========================= */
	@Nested
	@DisplayName("getAdmissionDetail - 관리자 재학인증 신청 상세 조회")
	class GetAdmissionDetail {

		@Test
		@DisplayName("존재하는 신청 ID로 상세를 조회한다")
		void givenValidAdmissionId_whenGetAdmissionDetail_thenReturnAdmissionResult() {
			// given
			String admissionId = "admission-1";
			User user = ObjectFixtures.getUserWithId("user-1");
			UserAdmission admission = ObjectFixtures.getUserAdmissionWithId(admissionId, user);

			when(admissionReader.findAdmissionDetail(admissionId)).thenReturn(admission);

			// when
			AdmissionResult result = admissionAdminService.getAdmissionDetail(admissionId);

			// then
			assertThat(result).isNotNull();
			assertThat(result.id()).isEqualTo(admissionId);
			assertThat(result.userName()).isEqualTo(user.getName());
			assertThat(result.userEmail()).isEqualTo(user.getEmail());
			assertThat(result.requestedDepartment()).isEqualTo(Department.SCHOOL_OF_SW);
			assertThat(result.requestedAdmissionYear()).isEqualTo(2023);
			assertThat(result.requestedStudentId()).isEqualTo("20231234");
			assertThat(result.requestedAcademicStatus()).isEqualTo(AcademicStatus.ENROLLED);
			assertThat(result.description()).isEqualTo("재학증명서 첨부합니다");
			assertThat(result.attachImageUrls()).hasSize(1);
			assertThat(result.attachImageUrls().get(0)).contains("storage.example.com");
			assertThat(result.userState()).isEqualTo(UserState.AWAIT);

			verify(admissionReader).findAdmissionDetail(admissionId);
		}

		@Test
		@DisplayName("존재하지 않는 신청 ID로 조회하면 ADMISSION_NOT_FOUND 예외가 발생한다")
		void givenInvalidAdmissionId_whenGetAdmissionDetail_thenThrowNotFoundException() {
			// given
			String admissionId = "non-existent-id";

			when(admissionReader.findAdmissionDetail(admissionId))
				.thenThrow(UserErrorCode.ADMISSION_NOT_FOUND.toBaseException());

			// when & then
			assertThatThrownBy(() -> admissionAdminService.getAdmissionDetail(admissionId))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
				.isEqualTo(UserErrorCode.ADMISSION_NOT_FOUND);

			verify(admissionReader).findAdmissionDetail(admissionId);
		}
	}
}
