package net.causw.app.main.domain.admin.audit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import net.causw.app.main.domain.admin.audit.api.v2.dto.request.AdminAuditLogRequest;
import net.causw.app.main.domain.admin.audit.enums.AdminAuditLogCategory;
import net.causw.app.main.domain.admin.audit.repository.AdminAuditLogQueryRepository;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogCondition;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.GlobalErrorCode;

@ExtendWith(MockitoExtension.class)
class AdminAuditLogServiceTest {

	@InjectMocks
	private AdminAuditLogService adminAuditLogService;

	@Mock
	private AdminAuditLogQueryRepository adminAuditLogQueryRepository;

	@Nested
	@DisplayName("감사 로그 목록 조회 (getAuditLogs)")
	class GetAuditLogsTest {

		@Test
		@DisplayName("실패: 시작일이 종료일보다 늦으면 잘못된 요청 예외가 발생한다")
		void givenFromAfterTo_whenGetAuditLogs_thenThrowBadRequest() {
			// given
			AdminAuditLogRequest request = new AdminAuditLogRequest(
				LocalDateTime.of(2026, 6, 14, 0, 0),
				LocalDateTime.of(2026, 6, 13, 0, 0),
				AdminAuditLogCategory.USER,
				null,
				null);

			// when & then
			assertThatThrownBy(() -> adminAuditLogService.getAuditLogs(request, PageRequest.of(0, 10)))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.BAD_REQUEST);
		}

		@Test
		@DisplayName("성공: 공백 키워드는 필터 없음으로 정규화한다")
		void givenBlankKeyword_whenGetAuditLogs_thenNormalizeKeywordToNull() {
			// given
			Pageable pageable = PageRequest.of(0, 10);
			AdminAuditLogRequest request = new AdminAuditLogRequest(
				null,
				null,
				AdminAuditLogCategory.USER,
				null,
				"   ");
			given(adminAuditLogQueryRepository.findAuditLogs(org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.eq(pageable)))
				.willReturn(Page.empty(pageable));

			// when
			adminAuditLogService.getAuditLogs(request, pageable);

			// then
			ArgumentCaptor<AdminAuditLogCondition> captor = ArgumentCaptor.forClass(AdminAuditLogCondition.class);
			verify(adminAuditLogQueryRepository).findAuditLogs(captor.capture(),
				org.mockito.ArgumentMatchers.eq(pageable));
			assertThat(captor.getValue().keyword()).isNull();
		}

		@Test
		@DisplayName("성공: 카테고리 미지정 요청은 null 조건으로 조회한다")
		void givenNullCategory_whenGetAuditLogs_thenPassNullCategory() {
			// given
			Pageable pageable = PageRequest.of(0, 10);
			AdminAuditLogRequest request = new AdminAuditLogRequest(null, null, null, null, null);
			given(adminAuditLogQueryRepository.findAuditLogs(org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.eq(pageable)))
				.willReturn(Page.empty(pageable));

			// when
			adminAuditLogService.getAuditLogs(request, pageable);

			// then
			ArgumentCaptor<AdminAuditLogCondition> captor = ArgumentCaptor.forClass(AdminAuditLogCondition.class);
			verify(adminAuditLogQueryRepository).findAuditLogs(captor.capture(),
				org.mockito.ArgumentMatchers.eq(pageable));
			assertThat(captor.getValue().category()).isNull();
		}

		@Test
		@DisplayName("성공: 공백 액션 타입은 필터 없음으로 정규화한다")
		void givenBlankActionType_whenGetAuditLogs_thenNormalizeActionTypeToNull() {
			// given
			Pageable pageable = PageRequest.of(0, 10);
			AdminAuditLogRequest request = new AdminAuditLogRequest(
				null,
				null,
				AdminAuditLogCategory.USER,
				"   ",
				null);
			given(adminAuditLogQueryRepository.findAuditLogs(org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.eq(pageable)))
				.willReturn(Page.empty(pageable));

			// when
			adminAuditLogService.getAuditLogs(request, pageable);

			// then
			ArgumentCaptor<AdminAuditLogCondition> captor = ArgumentCaptor.forClass(AdminAuditLogCondition.class);
			verify(adminAuditLogQueryRepository).findAuditLogs(captor.capture(),
				org.mockito.ArgumentMatchers.eq(pageable));
			assertThat(captor.getValue().actionType()).isNull();
		}

		@Test
		@DisplayName("성공: 액션 타입 문자열을 대문자 문자열 조건으로 정규화한다")
		void givenActionType_whenGetAuditLogs_thenNormalizeActionTypeToUppercaseStringCondition() {
			// given
			Pageable pageable = PageRequest.of(0, 10);
			AdminAuditLogRequest request = new AdminAuditLogRequest(
				null,
				null,
				AdminAuditLogCategory.USER,
				"drop",
				null);
			given(adminAuditLogQueryRepository.findAuditLogs(org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.eq(pageable)))
				.willReturn(Page.empty(pageable));

			// when
			adminAuditLogService.getAuditLogs(request, pageable);

			// then
			ArgumentCaptor<AdminAuditLogCondition> captor = ArgumentCaptor.forClass(AdminAuditLogCondition.class);
			verify(adminAuditLogQueryRepository).findAuditLogs(captor.capture(),
				org.mockito.ArgumentMatchers.eq(pageable));
			assertThat(captor.getValue().actionType()).isEqualTo("DROP");
		}

		@ParameterizedTest
		@CsvSource({
			"LOCKER, assign, ASSIGN",
			"LOCKER, extend, EXTEND",
			"LOCKER, release, RELEASE",
			"LOCKER, enable, ENABLE",
			"LOCKER, disable, DISABLE",
			"LOCKER, release_expired, RELEASE_EXPIRED",
			"ACADEMIC, admission_accept, ADMISSION_ACCEPT",
			"ACADEMIC, admission_reject, ADMISSION_REJECT",
			"ACADEMIC, academic_record_accept, ACADEMIC_RECORD_ACCEPT",
			"ACADEMIC, academic_record_reject, ACADEMIC_RECORD_REJECT"
		})
		@DisplayName("성공: 카테고리별 신규 액션 타입을 대문자 문자열 조건으로 정규화한다")
		void givenAdditionalCategoryActionType_whenGetAuditLogs_thenNormalizeActionTypeToUppercaseStringCondition(
			AdminAuditLogCategory category,
			String actionType,
			String expectedActionType) {
			// given
			Pageable pageable = PageRequest.of(0, 10);
			AdminAuditLogRequest request = new AdminAuditLogRequest(
				null,
				null,
				category,
				actionType,
				null);
			given(adminAuditLogQueryRepository.findAuditLogs(org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.eq(pageable)))
				.willReturn(Page.empty(pageable));

			// when
			adminAuditLogService.getAuditLogs(request, pageable);

			// then
			ArgumentCaptor<AdminAuditLogCondition> captor = ArgumentCaptor.forClass(AdminAuditLogCondition.class);
			verify(adminAuditLogQueryRepository).findAuditLogs(captor.capture(),
				org.mockito.ArgumentMatchers.eq(pageable));
			assertThat(captor.getValue().category()).isEqualTo(category);
			assertThat(captor.getValue().actionType()).isEqualTo(expectedActionType);
		}

		@Test
		@DisplayName("실패: 다른 카테고리의 액션 타입은 잘못된 요청 예외가 발생하고 저장소를 조회하지 않는다")
		void givenActionTypeForOtherCategory_whenGetAuditLogs_thenThrowBadRequestWithoutRepositoryCall() {
			// given
			Pageable pageable = PageRequest.of(0, 10);
			AdminAuditLogRequest request = new AdminAuditLogRequest(
				null,
				null,
				AdminAuditLogCategory.LOCKER,
				"ADMISSION_ACCEPT",
				null);

			// when & then
			assertThatThrownBy(() -> adminAuditLogService.getAuditLogs(request, pageable))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.BAD_REQUEST);
			verify(adminAuditLogQueryRepository, never()).findAuditLogs(org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.any());
		}

		@Test
		@DisplayName("실패: 존재하지 않는 액션 타입은 잘못된 요청 예외가 발생하고 저장소를 조회하지 않는다")
		void givenInvalidActionType_whenGetAuditLogs_thenThrowBadRequestWithoutRepositoryCall() {
			// given
			Pageable pageable = PageRequest.of(0, 10);
			AdminAuditLogRequest request = new AdminAuditLogRequest(
				null,
				null,
				AdminAuditLogCategory.USER,
				"unknown",
				null);

			// when & then
			assertThatThrownBy(() -> adminAuditLogService.getAuditLogs(request, pageable))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.BAD_REQUEST);
			verify(adminAuditLogQueryRepository, never()).findAuditLogs(org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.any());
		}
	}
}
