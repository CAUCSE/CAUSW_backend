package net.causw.app.main.domain.community.ceremony.service.v2;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.asset.file.service.v2.UuidFileService;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.request.CreateCeremonyRequestDto;
import net.causw.app.main.domain.community.ceremony.api.v2.mapper.CeremonyCreateMapper;
import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyContext;
import net.causw.app.main.domain.community.ceremony.service.CeremonyService;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyCreator;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyReader;
import net.causw.app.main.domain.community.ceremony.util.CeremonyValidator;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.CeremonyErrorCode;

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
	CeremonyValidator ceremonyValidator;

	@Nested
	@DisplayName("경조사 생성 테스트")
	class CreateCeremonyValidationTest {

		private User user;
		private CreateCeremonyRequestDto dto;

		@BeforeEach
		void setUp() {
			user = mock(User.class);
			dto = mock(CreateCeremonyRequestDto.class);
		}

		@Test
		@DisplayName("관계 가족 선택 시 상세 관계 선택 안 하면 Validator가 예외 반환")
		void givenFamilyRelationIsNull_whenCreateCategoryWithRelationIsFamily_thenThrowsException() {
			//given
			doThrow(CeremonyErrorCode.FAMILY_RELATION_REQUIRED.toBaseException())
				.when(ceremonyValidator).validateForCreate(dto);

			assertThatThrownBy(() -> ceremonyService.createCeremony(user, dto, null))
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
				.when(ceremonyValidator).validateForCreate(dto);

			assertThatThrownBy(() -> ceremonyService.createCeremony(user, dto, null))
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
			doThrow(CeremonyErrorCode.ALUMNI_NAME_REQUIRED.toBaseException())
				.when(ceremonyValidator).validateForCreate(dto);

			assertThatThrownBy(() -> ceremonyService.createCeremony(user, dto, null))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasFieldOrPropertyWithValue("errorCode", CeremonyErrorCode.ALUMNI_NAME_REQUIRED);
			then(uuidFileService).should(never()).saveFileList(any(), any());
			then(ceremonyCreateMapper).should(never()).fromRequest(any(), any(), any(), any());
			then(ceremonyCreator).should(never()).save(any());
		}

		@DisplayName("날짜 또는 시간 안 맞으면 Validator가 예외 반환")
		@Test
		void givenDateTimeValidatorFail_whenCreateCeremony_thenThrowsException() {
			// given
			doThrow(CeremonyErrorCode.START_TIME_REQUIRED.toBaseException())
				.when(ceremonyValidator).validateForCreate(dto);

			// when & then
			assertThatThrownBy(() -> ceremonyService.createCeremony(user, dto, null))
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
				.when(ceremonyValidator).validateForCreate(dto);

			// when & then
			assertThatThrownBy(() -> ceremonyService.createCeremony(user, dto, null))
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
		CreateCeremonyRequestDto dto;

		@BeforeEach
		void setUp() {
			user = mock(User.class);
			applicant = mock(User.class);
			ceremony = mock(Ceremony.class);
			other = mock(User.class);
			dto = mock(CreateCeremonyRequestDto.class);
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
	}
}