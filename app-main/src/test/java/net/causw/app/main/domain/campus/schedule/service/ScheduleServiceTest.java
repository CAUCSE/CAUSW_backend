package net.causw.app.main.domain.campus.schedule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.campus.schedule.entity.Schedule;
import net.causw.app.main.domain.campus.schedule.entity.enums.ScheduleType;
import net.causw.app.main.domain.campus.schedule.enums.ScheduleCategory;
import net.causw.app.main.domain.campus.schedule.service.dto.ScheduleCalendarDto;
import net.causw.app.main.domain.campus.schedule.service.dto.ScheduleDto;
import net.causw.app.main.domain.campus.schedule.service.implementation.ScheduleMaskingResolver;
import net.causw.app.main.domain.campus.schedule.service.implementation.ScheduleReader;
import net.causw.app.main.domain.campus.schedule.service.implementation.ScheduleWriter;
import net.causw.app.main.domain.campus.schedule.util.ScheduleMapper;
import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyCategory;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyType;
import net.causw.app.main.domain.community.ceremony.enums.RelationType;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.ScheduleErrorCode;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTest {

	@InjectMocks
	private ScheduleService scheduleService;

	@Mock
	private ScheduleWriter scheduleWriter;

	@Mock
	private ScheduleReader scheduleReader;

	@Mock
	private ScheduleMaskingResolver scheduleMaskingResolver;

	@Mock
	private CeremonyReader ceremonyReader;

	private User mockUser;
	private Schedule mockSchedule;
	private ScheduleDto mockScheduleDto;

	@BeforeEach
	void setUp() {
		mockUser = ObjectFixtures.getCertifiedUser();
		mockSchedule = ObjectFixtures.getSchedule(mockUser);
		mockScheduleDto = ObjectFixtures.getScheduleDto(mockUser);
	}

	@Nested
	@DisplayName("일정 생성 테스트")
	class CreateScheduleTest {

		@Test
		@DisplayName("일정 생성 성공")
		void createScheduleSuccess() {
			// given
			given(scheduleWriter.create(any(ScheduleDto.class))).willReturn(mockSchedule);

			// when
			ScheduleDto result = scheduleService.save(mockScheduleDto);

			// then
			assertThat(result).isNotNull();
			assertThat(result)
				.usingRecursiveComparison()
				.comparingOnlyFields("title", "start", "end", "type", "creator")
				.isEqualTo(mockScheduleDto);

			verify(scheduleWriter).create(any(ScheduleDto.class));
		}
	}

	@Nested
	@DisplayName("일정 수정 테스트")
	class UpdateScheduleTest {

		@Test
		@DisplayName("일정 수정 성공")
		void updateScheduleSuccess() {
			// given
			String scheduleId = "schedule-id";
			User updatedUser = ObjectFixtures.getCertifiedUserWithId("updated-user");
			ScheduleDto updateDto = ScheduleDto.builder()
				.title("수정된 일정")
				.type(ScheduleType.DEPARTMENT)
				.start(LocalDateTime.of(2026, 6, 15, 0, 0))
				.end(LocalDateTime.of(2026, 6, 21, 23, 59))
				.creator(updatedUser)
				.build();
			Schedule updatedSchedule = ScheduleMapper.from(updateDto);

			given(scheduleReader.findById(scheduleId)).willReturn(mockSchedule);
			given(scheduleWriter.update(mockSchedule, updateDto)).willReturn(updatedSchedule);

			// when
			ScheduleDto result = scheduleService.update(scheduleId, updateDto);

			// then
			assertThat(result).isNotNull();
			assertThat(result)
				.usingRecursiveComparison()
				.comparingOnlyFields("title", "start", "end", "type", "creator")
				.isEqualTo(updateDto);

			verify(scheduleReader).findById(scheduleId);
			verify(scheduleWriter).update(mockSchedule, updateDto);
		}

		@Test
		@DisplayName("존재하지 않는 일정 수정 시도")
		void updateNonExistentSchedule() {
			// given
			String scheduleId = "non-existent-id";
			ScheduleDto updateDto = ScheduleDto.builder()
				.title("수정할 일정")
				.type(ScheduleType.ACADEMIC)
				.start(LocalDateTime.of(2026, 6, 15, 0, 0))
				.end(LocalDateTime.of(2026, 6, 21, 23, 59))
				.build();

			given(scheduleReader.findById(scheduleId))
				.willThrow(ScheduleErrorCode.SCHEDULE_NOT_FOUND.toBaseException());

			// when & then
			assertThatThrownBy(() -> scheduleService.update(scheduleId, updateDto))
				.isInstanceOf(BaseRunTimeV2Exception.class);

			verify(scheduleReader).findById(scheduleId);
			verify(scheduleWriter, never()).update(any(Schedule.class), any(ScheduleDto.class));
		}
	}

	@Nested
	@DisplayName("일정 삭제 테스트")
	class DeleteScheduleTest {

		@Test
		@DisplayName("일정 삭제 성공")
		void deleteScheduleSuccess() {
			// given
			String scheduleId = "schedule-id";

			// when
			scheduleService.delete(scheduleId);

			// then
			verify(scheduleWriter).deleteById(scheduleId);
		}
	}

	@Nested
	@DisplayName("일정 조회 테스트")
	class FindScheduleTest {

		@Test
		@DisplayName("ID로 일정 조회 성공")
		void findByIdSuccess() {
			// given
			String scheduleId = "schedule-id";
			given(scheduleReader.findById(scheduleId)).willReturn(mockSchedule);

			// when
			ScheduleDto result = scheduleService.findById(scheduleId);

			// then
			assertThat(result).isNotNull();
			assertThat(result)
				.usingRecursiveComparison()
				.comparingOnlyFields("title", "start", "end", "type", "creator")
				.isEqualTo(mockSchedule);

			verify(scheduleReader).findById(scheduleId);
		}

		@Test
		@DisplayName("존재하지 않는 ID로 조회 시도")
		void findByIdNotFound() {
			// given
			String scheduleId = "non-existent-id";
			given(scheduleReader.findById(scheduleId))
				.willThrow(ScheduleErrorCode.SCHEDULE_NOT_FOUND.toBaseException());

			// when & then
			assertThatThrownBy(() -> scheduleService.findById(scheduleId))
				.isInstanceOf(BaseRunTimeV2Exception.class);

			verify(scheduleReader).findById(scheduleId);
		}

		@Test
		@DisplayName("조건에 따른 일정 목록 조회 성공")
		void findByConditionSuccess() {
			// given
			LocalDateTime from = LocalDateTime.of(2026, 4, 1, 0, 0);
			LocalDateTime to = LocalDateTime.of(2026, 4, 30, 23, 59);
			List<ScheduleType> types = List.of(ScheduleType.ACADEMIC, ScheduleType.DEPARTMENT);

			Schedule schedule1 = Schedule.of(
				"중간고사",
				ScheduleType.ACADEMIC,
				LocalDateTime.of(2026, 4, 15, 0, 0),
				LocalDateTime.of(2026, 4, 21, 23, 59),
				mockUser,
				null);

			Schedule schedule2 = Schedule.of(
				"학술제",
				ScheduleType.DEPARTMENT,
				LocalDateTime.of(2026, 4, 25, 0, 0),
				LocalDateTime.of(2026, 4, 27, 23, 59),
				mockUser,
				null);

			List<Schedule> mockSchedules = List.of(schedule1, schedule2);
			given(scheduleReader.findByCondition(from, to, types)).willReturn(mockSchedules);

			// when
			List<ScheduleDto> result = scheduleService.findByCondition(from, to, types);

			// then
			assertThat(result).isNotNull();
			assertThat(result).hasSize(2);
			assertThat(result)
				.allSatisfy(dto -> {
					assertThat(dto.start()).isAfterOrEqualTo(from);
					assertThat(dto.end()).isBeforeOrEqualTo(to);
					assertThat(types).contains(dto.type());
				});

			verify(scheduleReader).findByCondition(from, to, types);
		}

		@Test
		@DisplayName("조건에 맞는 일정이 없을 때 빈 리스트 반환")
		void findByConditionEmpty() {
			// given
			LocalDateTime from = LocalDateTime.of(2026, 5, 1, 0, 0);
			LocalDateTime to = LocalDateTime.of(2026, 5, 31, 23, 59);
			List<ScheduleType> types = List.of(ScheduleType.ACADEMIC);

			given(scheduleReader.findByCondition(from, to, types)).willReturn(List.of());

			// when
			List<ScheduleDto> result = scheduleService.findByCondition(from, to, types);

			// then
			assertThat(result).isNotNull();
			assertThat(result).isEmpty();

			verify(scheduleReader).findByCondition(from, to, types);
		}

		@Test
		@DisplayName("타입 필터 없이 전체 일정 조회")
		void findByConditionWithoutTypeFilter() {
			// given
			LocalDateTime from = LocalDateTime.of(2026, 4, 1, 0, 0);
			LocalDateTime to = LocalDateTime.of(2026, 4, 30, 23, 59);

			Schedule schedule1 = Schedule.of(
				"중간고사",
				ScheduleType.ACADEMIC,
				LocalDateTime.of(2026, 4, 15, 0, 0),
				LocalDateTime.of(2026, 4, 21, 23, 59),
				mockUser,
				null);

			Schedule schedule2 = Schedule.of(
				"동아리 활동",
				ScheduleType.CCSSAA,
				LocalDateTime.of(2026, 4, 25, 0, 0),
				LocalDateTime.of(2026, 4, 27, 23, 59),
				mockUser,
				null);

			List<Schedule> mockSchedules = List.of(schedule1, schedule2);
			given(scheduleReader.findByCondition(from, to, null)).willReturn(mockSchedules);

			// when
			List<ScheduleDto> result = scheduleService.findByCondition(from, to, null);

			// then
			assertThat(result).isNotNull();
			assertThat(result).hasSize(2);
			assertThat(result)
				.allSatisfy(dto -> {
					assertThat(dto.start()).isAfterOrEqualTo(from);
					assertThat(dto.end()).isBeforeOrEqualTo(to);
				});

			verify(scheduleReader).findByCondition(from, to, null);
		}
	}

	@Nested
	@DisplayName("마스킹 조회 위임 테스트")
	class MaskingDelegationTest {

		@Test
		@DisplayName("findByConditionWithMasking은 ScheduleMaskingResolver에 권한 검증을 위임한다")
		void findByConditionWithMaskingDelegatesToResolver() {
			// given
			LocalDateTime from = LocalDateTime.of(2026, 4, 1, 0, 0);
			LocalDateTime to = LocalDateTime.of(2026, 4, 30, 23, 59);

			List<ScheduleCategory> categories = List.of(ScheduleCategory.ACADEMIC);
			List<ScheduleType> scheduleTypes = List.of(ScheduleType.ACADEMIC);

			User viewer = ObjectFixtures.getCertifiedUserWithId("viewer-id");

			Schedule schedule = Schedule.of(
				"일정",
				ScheduleType.ACADEMIC,
				LocalDateTime.of(2026, 4, 15, 0, 0),
				LocalDateTime.of(2026, 4, 21, 23, 59),
				mockUser,
				"post-1");

			given(scheduleReader.findByCondition(from, to, scheduleTypes))
				.willReturn(List.of(schedule));

			given(scheduleMaskingResolver.resolveReadablePostIds(anyList(), eq(viewer)))
				.willReturn(Set.of("post-1"));

			given(scheduleMaskingResolver.maskIfUnreadable(
				any(ScheduleDto.class),
				eq(Set.of("post-1"))))
				.willAnswer(inv -> inv.getArgument(0));

			// when
			List<ScheduleCalendarDto> result = scheduleService.findByConditionWithMasking(from, to, categories, viewer);

			// then
			assertThat(result).hasSize(1);
			verify(scheduleMaskingResolver).resolveReadablePostIds(anyList(), eq(viewer));
			verify(scheduleMaskingResolver).maskIfUnreadable(any(ScheduleDto.class), any());
			verify(ceremonyReader, never()).findAcceptedByDateRange(any(), any());
		}

		@Test
		@DisplayName("findByIdWithMasking은 ScheduleMaskingResolver에 권한 검증을 위임한다")
		void findByIdWithMaskingDelegatesToResolver() {
			// given
			String scheduleId = "schedule-id";
			User viewer = ObjectFixtures.getCertifiedUserWithId("viewer-id");

			given(scheduleReader.findById(scheduleId)).willReturn(mockSchedule);
			given(scheduleMaskingResolver.resolveReadablePostIds(anyList(), eq(viewer))).willReturn(Set.of());
			given(scheduleMaskingResolver.maskIfUnreadable(any(ScheduleDto.class), any()))
				.willAnswer(inv -> inv.getArgument(0));

			// when
			scheduleService.findByIdWithMasking(scheduleId, viewer);

			// then
			verify(scheduleReader).findById(scheduleId);
			verify(scheduleMaskingResolver).resolveReadablePostIds(anyList(), eq(viewer));
			verify(scheduleMaskingResolver).maskIfUnreadable(any(ScheduleDto.class), any());
		}

		@Test
		@DisplayName("CEREMONY만 필터링하면 일반 일정은 조회하지 않고 경조사만 조회한다")
		void findByConditionWithMaskingCeremonyOnly() {
			// given
			LocalDateTime from = LocalDateTime.of(2026, 4, 1, 0, 0);
			LocalDateTime to = LocalDateTime.of(2026, 4, 30, 23, 59);
			List<ScheduleCategory> categories = List.of(ScheduleCategory.CEREMONY);
			User viewer = ObjectFixtures.getCertifiedUserWithId("viewer-id");

			given(ceremonyReader.findAcceptedByDateRange(
				from.toLocalDate(),
				to.toLocalDate()))
				.willReturn(List.of());

			// when
			List<ScheduleCalendarDto> result = scheduleService.findByConditionWithMasking(from, to, categories, viewer);

			// then
			assertThat(result).isEmpty();

			verify(scheduleReader, never())
				.findByCondition(any(), any(), any());

			verify(ceremonyReader)
				.findAcceptedByDateRange(from.toLocalDate(), to.toLocalDate());
		}
	}

	@Test
	@DisplayName("승인된 경조사는 CEREMONY 카테고리의 캘린더 일정으로 변환된다")
	void findByConditionWithMaskingMapsCeremonyToCalendarDto() {
		// given
		LocalDateTime from = LocalDateTime.of(2026, 4, 1, 0, 0);
		LocalDateTime to = LocalDateTime.of(2026, 4, 30, 23, 59);
		List<ScheduleCategory> categories = List.of(ScheduleCategory.CEREMONY);
		User viewer = ObjectFixtures.getCertifiedUserWithId("viewer-id");

		User ceremonyUser = ObjectFixtures.getCertifiedUser();

		Ceremony ceremony = Ceremony.of(
			ceremonyUser,
			CeremonyType.CELEBRATION,
			CeremonyCategory.MARRIAGE,
			null,
			LocalDate.of(2026, 4, 10),
			LocalDate.of(2026, 4, 10),
			LocalTime.of(14, 0),
			LocalTime.of(16, 0),
			RelationType.ME,
			null,
			null,
			null,
			null,
			"경조사 설명",
			null,
			null,
			null,
			null,
			null,
			true,
			List.of());

		given(ceremonyReader.findAcceptedByDateRange(
			from.toLocalDate(),
			to.toLocalDate()))
			.willReturn(List.of(ceremony));

		// when
		List<ScheduleCalendarDto> result = scheduleService.findByConditionWithMasking(from, to, categories, viewer);

		// then
		assertThat(result).hasSize(1);

		ScheduleCalendarDto calendarDto = result.getFirst();

		assertThat(calendarDto.type()).isEqualTo(ScheduleCategory.CEREMONY);
		assertThat(calendarDto.start()).isEqualTo(LocalDateTime.of(2026, 4, 10, 14, 0));
		assertThat(calendarDto.end()).isEqualTo(LocalDateTime.of(2026, 4, 10, 16, 0));
		assertThat(calendarDto.targetPostId()).isNull();
	}

	@Test
	@DisplayName("일반 일정과 경조사를 함께 조회하면 시작 시간순으로 정렬된다")
	void findByConditionWithMaskingMergesAndSortsSchedulesAndCeremonies() {
		// given
		LocalDateTime from = LocalDateTime.of(2026, 4, 1, 0, 0);
		LocalDateTime to = LocalDateTime.of(2026, 4, 30, 23, 59);

		List<ScheduleCategory> categories = List.of(
			ScheduleCategory.ACADEMIC,
			ScheduleCategory.CEREMONY);

		List<ScheduleType> scheduleTypes = List.of(ScheduleType.ACADEMIC);
		User viewer = ObjectFixtures.getCertifiedUserWithId("viewer-id");

		// 일반 일정: 4월 20일
		Schedule schedule = Schedule.of(
			"중간고사",
			ScheduleType.ACADEMIC,
			LocalDateTime.of(2026, 4, 20, 0, 0),
			LocalDateTime.of(2026, 4, 21, 23, 59),
			mockUser,
			null);

		// 경조사: 4월 10일
		User ceremonyUser = ObjectFixtures.getCertifiedUser();

		Ceremony ceremony = Ceremony.of(
			ceremonyUser,
			CeremonyType.CELEBRATION,
			CeremonyCategory.MARRIAGE,
			null,
			LocalDate.of(2026, 4, 10),
			LocalDate.of(2026, 4, 10),
			LocalTime.of(14, 0),
			LocalTime.of(16, 0),
			RelationType.ME,
			null,
			null,
			null,
			null,
			"경조사 설명",
			null,
			null,
			null,
			null,
			null,
			true,
			List.of());

		given(scheduleReader.findByCondition(from, to, scheduleTypes))
			.willReturn(List.of(schedule));

		given(scheduleMaskingResolver.resolveReadablePostIds(anyList(), eq(viewer)))
			.willReturn(Set.of());

		given(scheduleMaskingResolver.maskIfUnreadable(
			any(ScheduleDto.class),
			eq(Set.of())))
			.willAnswer(invocation -> invocation.getArgument(0));

		given(ceremonyReader.findAcceptedByDateRange(
			from.toLocalDate(),
			to.toLocalDate()))
			.willReturn(List.of(ceremony));

		// when
		List<ScheduleCalendarDto> result = scheduleService.findByConditionWithMasking(from, to, categories, viewer);

		// then
		assertThat(result).hasSize(2);

		assertThat(result.get(0).type())
			.isEqualTo(ScheduleCategory.CEREMONY);

		assertThat(result.get(1).type())
			.isEqualTo(ScheduleCategory.ACADEMIC);

		assertThat(result.get(0).start())
			.isBefore(result.get(1).start());

		verify(scheduleReader)
			.findByCondition(from, to, scheduleTypes);

		verify(ceremonyReader)
			.findAcceptedByDateRange(from.toLocalDate(), to.toLocalDate());
	}

	@Test
	@DisplayName("카테고리 필터가 없으면 일반 일정과 경조사를 모두 조회한다")
	void findByConditionWithMaskingWithoutCategoryFilterIncludesAll() {
		// given
		LocalDateTime from = LocalDateTime.of(2026, 4, 1, 0, 0);
		LocalDateTime to = LocalDateTime.of(2026, 4, 30, 23, 59);
		User viewer = ObjectFixtures.getCertifiedUserWithId("viewer-id");

		Schedule schedule = Schedule.of(
			"중간고사",
			ScheduleType.ACADEMIC,
			LocalDateTime.of(2026, 4, 20, 0, 0),
			LocalDateTime.of(2026, 4, 21, 23, 59),
			mockUser,
			null);

		User ceremonyUser = ObjectFixtures.getCertifiedUser();

		Ceremony ceremony = Ceremony.of(
			ceremonyUser,
			CeremonyType.CELEBRATION,
			CeremonyCategory.MARRIAGE,
			null,
			LocalDate.of(2026, 4, 10),
			LocalDate.of(2026, 4, 10),
			LocalTime.of(14, 0),
			LocalTime.of(16, 0),
			RelationType.ME,
			null,
			null,
			null,
			null,
			"경조사 설명",
			null,
			null,
			null,
			null,
			null,
			true,
			List.of());

		given(scheduleReader.findByCondition(from, to, null))
			.willReturn(List.of(schedule));

		given(scheduleMaskingResolver.resolveReadablePostIds(anyList(), eq(viewer)))
			.willReturn(Set.of());

		given(scheduleMaskingResolver.maskIfUnreadable(
			any(ScheduleDto.class),
			eq(Set.of())))
			.willAnswer(invocation -> invocation.getArgument(0));

		given(ceremonyReader.findAcceptedByDateRange(
			from.toLocalDate(),
			to.toLocalDate()))
			.willReturn(List.of(ceremony));

		// when
		List<ScheduleCalendarDto> result = scheduleService.findByConditionWithMasking(from, to, null, viewer);

		// then
		assertThat(result).hasSize(2);

		assertThat(result)
			.extracting(ScheduleCalendarDto::type)
			.containsExactly(
				ScheduleCategory.CEREMONY,
				ScheduleCategory.ACADEMIC);

		verify(scheduleReader)
			.findByCondition(from, to, null);

		verify(ceremonyReader)
			.findAcceptedByDateRange(from.toLocalDate(), to.toLocalDate());
	}
}
