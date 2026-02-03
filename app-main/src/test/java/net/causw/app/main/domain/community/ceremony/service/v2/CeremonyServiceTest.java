package net.causw.app.main.domain.community.ceremony.service.v2;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;

import java.time.LocalTime;
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

import net.causw.app.main.domain.community.ceremony.api.v2.dto.CreateCeremonyRequestDto;
import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.enums.AlumniRelation;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyContext;
import net.causw.app.main.domain.community.ceremony.enums.RelationType;
import net.causw.app.main.domain.community.ceremony.service.CeremonyService;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyCreator;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
public class CeremonyServiceTest {

	@InjectMocks
	CeremonyService ceremonyService;

	@Mock
	CeremonyCreator ceremonyCreator;

	@Mock
	CeremonyReader ceremonyReader;

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

		@DisplayName("전체 알림 전송이 false인 경우, 대상 학번이 Null이면 실패")
		@Test
		void givenIsSetAllFalse_whenTargetAdmissionYearsIsNull_thenThrowsException() {
			// given
			given(dto.getIsSetAll()).willReturn(false);
			given(dto.getTargetAdmissionYears()).willReturn(null);

			// when & then
			assertThatThrownBy(() -> ceremonyService.createCeremony(user, dto, null))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(MessageUtil.CEREMONY_TARGET_ADMISSION_YEARS_REQUIRED)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_PARAMETER);

			verify(ceremonyCreator, times(0)).save(any(Ceremony.class));
		}

		@DisplayName("전체 알림 전송이 false인 경우, 대상 학번이 Empty이면 실패")
		@Test
		void givenIsSetAllFalse_whenTargetAdmissionYearsIsEmpty_thenThrowsException() {
			//given
			given(dto.getIsSetAll()).willReturn(false);
			given(dto.getTargetAdmissionYears()).willReturn(List.of());

			// when, then
			assertThatThrownBy(() -> ceremonyService.createCeremony(user, dto, List.of()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(MessageUtil.CEREMONY_TARGET_ADMISSION_YEARS_REQUIRED)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_PARAMETER);

			verify(ceremonyCreator, times(0)).save(any(Ceremony.class));
		}

		@DisplayName("학번 형식이 올바른지 검증 (2자리 숫자)")
		@Test
		void givenAlumniAdmissionYear_whenInvalidFormat_thenThrowsException() {
			// given
			given(dto.getIsSetAll()).willReturn(false);
			given(dto.getTargetAdmissionYears()).willReturn(List.of("123"));

			// when, then
			assertThatThrownBy(() -> ceremonyService.createCeremony(user, dto, List.of()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(MessageUtil.CEREMONY_INVALID_ADMISSION_YEAR_FORMAT)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_PARAMETER);

			verify(ceremonyCreator, times(0)).save(any(Ceremony.class));
		}

		@DisplayName("관계=FAMILY인데 FamilyRelation이 null이면 실패")
		@Test
		void givenRelationTypeIsFamily_whenFamilyRelationIsNull_thenThrowsException() {
			// given
			given(dto.getIsSetAll()).willReturn(true);

			given(dto.getRelationType()).willReturn(RelationType.FAMILY);
			given(dto.getFamilyRelation()).willReturn(null);

			// when, then
			assertThatThrownBy(() -> ceremonyService.createCeremony(user, dto, List.of()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(MessageUtil.CEREMONY_FAMILY_RELATION_REQUIRED)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_USER_DATA_REQUEST);

			verify(ceremonyCreator, times(0)).save(any(Ceremony.class));
		}

		@DisplayName("관계=ALUMNI인데 AlumniRelation이 null이면 실패")
		@Test
		void givenRelationTypeIsAlumni_whenAlumniRelationIsNull_thenThrowsException() {
			// given
			given(dto.getIsSetAll()).willReturn(true);

			given(dto.getRelationType()).willReturn(RelationType.ALUMNI);
			given(dto.getAlumniRelation()).willReturn(null);

			// when, then
			assertThatThrownBy(() -> ceremonyService.createCeremony(user, dto, List.of()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(MessageUtil.CEREMONY_ALUMNI_RELATION_REQUIRED)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_USER_DATA_REQUEST);

			verify(ceremonyCreator, times(0)).save(any(Ceremony.class));
		}

		@DisplayName("관계=ALUMNI인데 AlumniName이 null이면 실패")
		@Test
		void givenRelationTypeIsAlumni_whenAlumniNameIsNull_thenThrowsException() {
			// given
			given(dto.getIsSetAll()).willReturn(true);

			given(dto.getRelationType()).willReturn(RelationType.ALUMNI);
			given(dto.getAlumniRelation()).willReturn(AlumniRelation.ALUMNI);
			given(dto.getAlumniName()).willReturn(null);

			// when, then
			assertThatThrownBy(() -> ceremonyService.createCeremony(user, dto, List.of()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(MessageUtil.CEREMONY_ALUMNI_NAME_REQUIRED)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_USER_DATA_REQUEST);

			verify(ceremonyCreator, times(0)).save(any(Ceremony.class));
		}

		@DisplayName("관계=ALUMNI인데 AlumniAdmissionYear이 null이면 실패")
		@Test
		void givenRelationTypeIsAlumni_whenAlumniAdmissionYearIsNull_thenThrowsException() {
			// given
			given(dto.getIsSetAll()).willReturn(true);

			given(dto.getRelationType()).willReturn(RelationType.ALUMNI);
			given(dto.getAlumniRelation()).willReturn(AlumniRelation.ALUMNI);
			given(dto.getAlumniName()).willReturn("동문이름");
			given(dto.getAlumniAdmissionYear()).willReturn(null);

			// when, then
			assertThatThrownBy(() -> ceremonyService.createCeremony(user, dto, List.of()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(MessageUtil.CEREMONY_ALUMNI_ADMISSION_YEAR_REQUIRED)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_USER_DATA_REQUEST);

			verify(ceremonyCreator, times(0)).save(any(Ceremony.class));
		}

		@DisplayName("EndTime 입력 시 EndDate가 null이면 실패")
		@Test
		void givenEndTime_whenEndDateIsNull_thenThrowsException() {
			// given
			given(dto.getIsSetAll()).willReturn(true);

			given(dto.getRelationType()).willReturn(RelationType.ME);

			given(dto.getEndDate()).willReturn(null);
			given(dto.getEndTime()).willReturn(LocalTime.parse("23:59"));

			// when, then
			assertThatThrownBy(() -> ceremonyService.createCeremony(user, dto, List.of()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(MessageUtil.CEREMONY_ENDDATE_REQUIRED)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_USER_DATA_REQUEST);

			verify(ceremonyCreator, times(0)).save(any(Ceremony.class));
		}

		// 전체 알림 전송이 true인 경우, 대상 학번은 빈 리스트(null)로 설정
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
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(MessageUtil.CEREMONY_NOT_FOUND)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.ROW_DOES_NOT_EXIST);

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
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(MessageUtil.CEREMONY_ACCESS_MY_ONLY)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_PARAMETER);

			verify(ceremonyReader, times(1)).findById("ceremonyId");
		}
	}

}
