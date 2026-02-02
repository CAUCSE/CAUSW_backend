package net.causw.app.main.domain.campus.schedule.service.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;

import java.time.LocalDateTime;
import java.util.List;

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
import net.causw.app.main.domain.campus.schedule.service.v2.ScheduleService;
import net.causw.app.main.domain.campus.schedule.service.v2.dto.ScheduleDto;
import net.causw.app.main.domain.campus.schedule.service.v2.implementation.ScheduleReader;
import net.causw.app.main.domain.campus.schedule.service.v2.implementation.ScheduleWriter;
import net.causw.app.main.domain.campus.schedule.util.ScheduleMapper;
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
				mockUser);

			Schedule schedule2 = Schedule.of(
				"학술제",
				ScheduleType.DEPARTMENT,
				LocalDateTime.of(2026, 4, 25, 0, 0),
				LocalDateTime.of(2026, 4, 27, 23, 59),
				mockUser);

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
				mockUser);

			Schedule schedule2 = Schedule.of(
				"동아리 활동",
				ScheduleType.CCSSAA,
				LocalDateTime.of(2026, 4, 25, 0, 0),
				LocalDateTime.of(2026, 4, 27, 23, 59),
				mockUser);

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
}
