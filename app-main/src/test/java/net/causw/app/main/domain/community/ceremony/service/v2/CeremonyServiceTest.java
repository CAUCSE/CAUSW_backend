package net.causw.app.main.domain.community.ceremony.service.v2;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import net.causw.app.main.domain.asset.file.service.v2.UuidFileService;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.request.CeremonyCreateRequest;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.response.CeremonySummaryResponse;
import net.causw.app.main.domain.community.ceremony.service.dto.request.CeremonyCreateCommand;
import net.causw.app.main.domain.community.ceremony.service.dto.response.CeremonySummaryResult;
import net.causw.app.main.domain.community.ceremony.service.mapper.CeremonyCreateMapper;
import net.causw.app.main.domain.community.ceremony.service.mapper.CeremonyMapper;
import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyContext;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;
import net.causw.app.main.domain.community.ceremony.service.CeremonyService;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyCreator;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyReader;
import net.causw.app.main.domain.community.ceremony.util.CeremonyValidator;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.CeremonyErrorCode;
import net.causw.app.main.shared.pageable.PageableFactory;
import net.causw.global.constant.StaticValue;

@ExtendWith(MockitoExtension.class)
public class CeremonyServiceTest {

	@InjectMocks
	CeremonyService ceremonyService;

	@Mock
	UuidFileService uuidFileService;
	@Mock
	CeremonyCreator ceremonyCreator;
	@Mock
	CeremonyReader ceremonyReader;
	@Mock
	CeremonyCreateMapper ceremonyCreateMapper;
	@Mock
	CeremonyMapper ceremonyMapper;
	@Mock
	CeremonyValidator ceremonyValidator;
	@Mock
	PageableFactory pageableFactory;

	Pageable pageable;

	@Nested
	@DisplayName("경조사 생성 테스트")
	class CreateCeremonyValidationTest {

		private User user;
		private CeremonyCreateCommand command;

		@BeforeEach
		void setUp() {
			user = mock(User.class);
			command = mock(CeremonyCreateCommand.class);
		}

		@Test
		@DisplayName("분류 직접 입력일 때 ceremony_custom_category가 입력되지 않으면 예외 반환")
		void givenCategoryIsETC_whenCreateCategoryWithCustomCategoryIsNull_thenThrowsException() {
			// given
			doThrow(CeremonyErrorCode.CUSTOM_CATEGORY_REQUIRED.toBaseException())
				.when(ceremonyValidator).validateForCreate(command);

			// when & then
			assertThatThrownBy(() -> ceremonyService.createCeremony(user, command, null))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasFieldOrPropertyWithValue("errorCode", CeremonyErrorCode.CUSTOM_CATEGORY_REQUIRED);
			then(uuidFileService).should(never()).saveFileList(any(), any());
			then(ceremonyCreateMapper).should(never()).fromRequest(any(), any(), any(), any());
			then(ceremonyCreator).should(never()).save(any());
		}

		@Test
		@DisplayName("관계 가족 선택 시 상세 관계 선택 안 하면 Validator가 예외 반환")
		void givenFamilyRelationIsNull_whenCreateCategoryWithRelationIsFamily_thenThrowsException() {
			//given
			doThrow(CeremonyErrorCode.FAMILY_RELATION_REQUIRED.toBaseException())
				.when(ceremonyValidator).validateForCreate(command);

			assertThatThrownBy(() -> ceremonyService.createCeremony(user, command, null))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasFieldOrPropertyWithValue("errorCode", CeremonyErrorCode.FAMILY_RELATION_REQUIRED);
			then(uuidFileService).should(never()).saveFileList(any(), any());
			then(ceremonyCreateMapper).should(never()).fromRequest(any(), any(), any(), any());
			then(ceremonyCreator).should(never()).save(any());
		}

		@Test
		@DisplayName("관계 동문 선택 시 동문 이름 선택 안 하면 Validator가 예외 반환")
		void givenAlumniNameIsNull_whenCreateCategoryWithRelationInstead_thenThrowsException() {
			//given
			doThrow(CeremonyErrorCode.ALUMNI_NAME_REQUIRED.toBaseException())
				.when(ceremonyValidator).validateForCreate(command);

			assertThatThrownBy(() -> ceremonyService.createCeremony(user, command, null))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasFieldOrPropertyWithValue("errorCode", CeremonyErrorCode.ALUMNI_NAME_REQUIRED);
			then(uuidFileService).should(never()).saveFileList(any(), any());
			then(ceremonyCreateMapper).should(never()).fromRequest(any(), any(), any(), any());
			then(ceremonyCreator).should(never()).save(any());
		}

		@Test
		@DisplayName("관계 동문 선택 시 동문 학번 입력 안 하면 Validator가 예외 반환")
		void givenAlumniAdmissionYearIsNull_whenCreateCategoryWithRelationInstead_thenThrowsException() {
			//given
			doThrow(CeremonyErrorCode.ALUMNI_ADMISSION_YEAR_REQUIRED.toBaseException())
				.when(ceremonyValidator).validateForCreate(command);

			assertThatThrownBy(() -> ceremonyService.createCeremony(user, command, null))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasFieldOrPropertyWithValue("errorCode", CeremonyErrorCode.ALUMNI_ADMISSION_YEAR_REQUIRED);
			then(uuidFileService).should(never()).saveFileList(any(), any());
			then(ceremonyCreateMapper).should(never()).fromRequest(any(), any(), any(), any());
			then(ceremonyCreator).should(never()).save(any());
		}

		@DisplayName("날짜 또는 시간 안 맞으면 Validator가 예외 반환")
		@Test
		void givenDateTimeValidatorFail_whenCreateCeremony_thenThrowsException() {
			// given
			doThrow(CeremonyErrorCode.START_TIME_REQUIRED.toBaseException())
				.when(ceremonyValidator).validateForCreate(command);

			// when & then
			assertThatThrownBy(() -> ceremonyService.createCeremony(user, command, null))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasFieldOrPropertyWithValue("errorCode", CeremonyErrorCode.START_TIME_REQUIRED);
			then(uuidFileService).should(never()).saveFileList(any(), any());
			then(ceremonyCreateMapper).should(never()).fromRequest(any(), any(), any(), any());
			then(ceremonyCreator).should(never()).save(any());
		}

		@Test
		@DisplayName("알림 설정, 대상 안 맞으면 Validator가 예외 반환")
		void givenNotificationValidatorFail_whenCreateCeremony_thenThrowsException() {
			// given
			doThrow(CeremonyErrorCode.TARGET_ADMISSION_YEARS_REQUIRED.toBaseException())
				.when(ceremonyValidator).validateForCreate(command);

			// when & then
			assertThatThrownBy(() -> ceremonyService.createCeremony(user, command, null))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasFieldOrPropertyWithValue("errorCode", CeremonyErrorCode.TARGET_ADMISSION_YEARS_REQUIRED);
			then(uuidFileService).should(never()).saveFileList(any(), any());
			then(ceremonyCreateMapper).should(never()).fromRequest(any(), any(), any(), any());
			then(ceremonyCreator).should(never()).save(any());
		}
	}

	@Nested
	@DisplayName("경조사 조회 테스트")
	class GetCeremonyTest {

		User user;
		User applicant;
		Ceremony ceremony;
		User other;
		CeremonyCreateRequest dto;

		@BeforeEach
		void setUp() {
			user = mock(User.class);
			applicant = mock(User.class);
			ceremony = mock(Ceremony.class);
			other = mock(User.class);
			dto = mock(CeremonyCreateRequest.class);
		}

		@DisplayName("존재하지 않는 경조사 조회하면 실패")
		@Test
		void givenCeremonyId_whenCeremonyDoesNotExist_thenThrowsException() {
			// given
			given(ceremonyReader.findById("ceremonyId")).willReturn(Optional.empty());

			// when, then
			assertThatThrownBy(() -> ceremonyService.getCeremony("ceremonyId", CeremonyContext.GENERAL, user))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessageContaining(CeremonyErrorCode.CEREMONY_NOT_FOUND.getMessage())
				.extracting("errorCode")
				.isEqualTo(CeremonyErrorCode.CEREMONY_NOT_FOUND);

			verify(ceremonyReader, times(1)).findById("ceremonyId");
		}

		@DisplayName("내 경조사 보기로 다른 사용자의 경조사 접근하면 실패")
		@Test
		void givenContextIsMy_whenUserIsNotApplicant_thenThrowsException() {
			// given
			given(applicant.getId()).willReturn("신청자uuid");
			given(ceremony.getUser()).willReturn(applicant);
			given(ceremonyReader.findById("ceremonyId")).willReturn(Optional.of(ceremony));

			// when, then
			assertThatThrownBy(() -> ceremonyService.getCeremony("ceremonyId", CeremonyContext.MY, other))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessageContaining(CeremonyErrorCode.ACCESS_ONLY_APPLICANT.getMessage())
				.extracting("errorCode")
				.isEqualTo(CeremonyErrorCode.ACCESS_ONLY_APPLICANT);

			verify(ceremonyReader, times(1)).findById("ceremonyId");
		}

		@DisplayName("승인되지 않은 경조사 general 상세 보기면 예외 반환")
		@Test
		void givenContextIsGeneral_whenCeremonyStateIsNotAccept_thenThrowsException() {
			// given
			given(ceremony.getCeremonyState()).willReturn(CeremonyState.AWAIT);
			given(ceremonyReader.findById("ceremonyId")).willReturn(Optional.of(ceremony));

			// when, then
			assertThatThrownBy(() -> ceremonyService.getCeremony("ceremonyId", CeremonyContext.GENERAL, other))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessageContaining(CeremonyErrorCode.CEREMONY_NOT_FOUND.getMessage())
				.extracting("errorCode")
				.isEqualTo(CeremonyErrorCode.CEREMONY_NOT_FOUND);

			verify(ceremonyReader, times(1)).findById("ceremonyId");
		}
	}

	@Nested
	@DisplayName("경조사 조회 리스트 테스트")
	class GetOngoingCeremonyPageTest {

		@BeforeEach
		void setUp() {
			pageable = mock(Pageable.class);
			given(pageableFactory.create(anyInt(), eq(StaticValue.DEFAULT_PAGE_SIZE))).willReturn(pageable);
		}

		@Test
		@DisplayName("경조사 리스트 조회 시 type이 null/empty/ALL이어도 성공(서비스는 그대로 reader에 전달)")
		void givenTypeNullOrEmptyOrAll_whenGetOngoing_thenCallsReaderAndMaps() {
			Ceremony c1 = mock(Ceremony.class);
			Ceremony c2 = mock(Ceremony.class);
			Page<Ceremony> ceremonyPage = new PageImpl<>(List.of(c1, c2));

			// given
			given(ceremonyReader.findOngoingOrderByStartedAtDesc(isNull(), any(), any(), eq(pageable)))
				.willReturn(ceremonyPage);
			given(ceremonyMapper.toSummaryResult(any(Ceremony.class)))
				.willReturn(mock(CeremonySummaryResult.class));

			// when
			Page<CeremonySummaryResult> result = ceremonyService.getOngoingCeremonyPage(null, 1);

			// then
			assertThat(result.getTotalElements()).isEqualTo(2);

			then(pageableFactory).should(times(1)).create(eq(1), eq(StaticValue.DEFAULT_PAGE_SIZE));
			then(ceremonyReader).should(times(1))
				.findOngoingOrderByStartedAtDesc(isNull(), any(), any(), eq(pageable));
			then(ceremonyMapper).should(times(2))
				.toSummaryResult(any(Ceremony.class));
		}

		@Test
		@DisplayName("경조사 리스트 조회 시 type이 존재하면 성공(서비스는 그대로 reader에 전달)")
		void givenValidType_whenGetOngoing_thenCallsReaderWithTypeAndMaps() {
			Ceremony c1 = mock(Ceremony.class);
			Page<Ceremony> ceremonyPage = new PageImpl<>(List.of(c1));

			// given
			given(ceremonyReader.findOngoingOrderByStartedAtDesc(eq("celebration"), any(), any(), eq(pageable)))
				.willReturn(ceremonyPage);
			given(ceremonyMapper.toSummaryResult(any(Ceremony.class)))
				.willReturn(mock(CeremonySummaryResult.class));

			// when
			Page<CeremonySummaryResult> result = ceremonyService.getOngoingCeremonyPage("celebration", 0);

			// then
			assertThat(result.getTotalElements()).isEqualTo(1);

			then(pageableFactory).should(times(1)).create(eq(0), eq(StaticValue.DEFAULT_PAGE_SIZE));
			then(ceremonyReader).should(times(1))
				.findOngoingOrderByStartedAtDesc(eq("celebration"), any(), any(), eq(pageable));
			then(ceremonyMapper).should(times(1))
				.toSummaryResult(any(Ceremony.class));
		}

		@Test
		@DisplayName("경조사 리스트 조회 시 reader가 예외를 던지면 그대로 전파")
		void givenReaderThrows_whenGetOngoing_thenThrows() {
			// given
			given(ceremonyReader.findOngoingOrderByStartedAtDesc(eq("nope"), any(), any(), eq(pageable)))
				.willThrow(new RuntimeException("boom"));

			// when & then
			assertThatThrownBy(() -> ceremonyService.getOngoingCeremonyPage("nope", 0))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("boom");

			then(pageableFactory).should(times(1)).create(eq(0), eq(StaticValue.DEFAULT_PAGE_SIZE));
			then(ceremonyReader).should(times(1))
				.findOngoingOrderByStartedAtDesc(eq("nope"), any(), any(), eq(pageable));

			then(ceremonyMapper).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("내 경조사 리스트 조회 시 state가 CLOSE면 예외 반환")
		void givenStateClose_whenGetMy_thenThrowsException() {
			// when & then
			assertThatThrownBy(() -> ceremonyService.getMyCeremonyPage("userId", CeremonyState.CLOSE, 0))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessageContaining(CeremonyErrorCode.INVALID_CEREMONY_STATE.getMessage())
				.extracting("errorCode")
				.isEqualTo(CeremonyErrorCode.INVALID_CEREMONY_STATE);

			then(ceremonyReader).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("내 경조사 리스트 조회 시 CLOSE가 아니면 성공")
		void givenStateAccept_whenGetMy_thenMapsToMySummary() {
			Ceremony c1 = mock(Ceremony.class);
			Ceremony c2 = mock(Ceremony.class);
			Page<Ceremony> ceremonyPage = new PageImpl<>(List.of(c1, c2));

			// given
			given(ceremonyReader.findByUserIdAndCeremonyStateOrderByStartedAtDesc(
				eq("userId"), eq(CeremonyState.ACCEPT), eq(pageable)))
				.willReturn(ceremonyPage);

			given(ceremonyMapper.toMySummaryResult(any(Ceremony.class)))
				.willReturn(mock(CeremonySummaryResult.class));

			// when
			Page<CeremonySummaryResult> result = ceremonyService.getMyCeremonyPage("userId", CeremonyState.ACCEPT,
				1);

			// then
			assertThat(result.getTotalElements()).isEqualTo(2);

			then(pageableFactory).should(times(1)).create(eq(1), eq(StaticValue.DEFAULT_PAGE_SIZE));
			then(ceremonyReader).should(times(1))
				.findByUserIdAndCeremonyStateOrderByStartedAtDesc(eq("userId"), eq(CeremonyState.ACCEPT), eq(pageable));
			then(ceremonyMapper).should(times(2))
				.toMySummaryResult(any(Ceremony.class));
		}
	}
}