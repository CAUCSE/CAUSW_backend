package net.causw.app.main.service.userCouncilFee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import jakarta.servlet.http.HttpServletResponse;

import java.util.LinkedHashMap;
import java.util.List;

import net.causw.app.main.repository.userCouncilFee.UserCouncilFeeRepository;
import net.causw.app.main.domain.model.entity.semester.Semester;
import net.causw.app.main.domain.model.entity.userCouncilFee.UserCouncilFee;
import net.causw.app.main.dto.userCouncilFee.UserCouncilFeeResponseDto;
import net.causw.app.main.service.excel.CouncilFeeExcelService;
import net.causw.app.main.service.semester.SemesterService;
import net.causw.app.main.util.ObjectFixtures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserCouncilFeeServiceTest {

	static final String sheetName = "학생회비 납부자 현황";
	static final Semester semester = ObjectFixtures.getSemester();
	@InjectMocks
	UserCouncilFeeService userCouncilFeeService;
	@Mock
	UserCouncilFeeRepository userCouncilFeeRepository;
	@Mock
	CouncilFeeExcelService councilFeeExcelService;
	@Mock
	SemesterService semesterService;
	@Mock
	HttpServletResponse response;

	@BeforeEach
	public void setUp() {
		given(semesterService.getCurrentSemesterEntity()).willReturn(semester);
	}

	@Nested
	class ExportUserCouncilFeeToExcelTest {

		@DisplayName("Excel로 데이터 내보내기 성공 - 가입 학생회비 납부자 목록")
		@Test
		void testExportJoinedUserCouncilFeeToExcelTest() {
			//given
			UserCouncilFee userCouncilFee = ObjectFixtures.getUserCouncilFee(true);
			given(userCouncilFeeRepository.findAll()).willReturn(List.of(userCouncilFee));

			//when
			userCouncilFeeService.exportUserCouncilFeeToExcel(response);

			//then
			LinkedHashMap<String, List<UserCouncilFeeResponseDto>> exportedUserCouncilFeeDataMap
				= captureGeneratedExcelData();
			List<UserCouncilFeeResponseDto> exportedUserCouncilFeeList
				= exportedUserCouncilFeeDataMap.get(sheetName);

			verifyUserCouncilFeeResponseDto(
				exportedUserCouncilFeeList, userCouncilFee.getUser().getStudentId());
		}

		@DisplayName("Excel로 데이터 내보내기 성공 - 미가입 학생회비 납부자 목록")
		@Test
		void testExportNotJoinedUserCouncilFeeToExcelTest() {
			//given
			UserCouncilFee userCouncilFee = ObjectFixtures.getUserCouncilFee(false);
			given(userCouncilFeeRepository.findAll()).willReturn(List.of(userCouncilFee));

			//when
			userCouncilFeeService.exportUserCouncilFeeToExcel(response);

			//then
			LinkedHashMap<String, List<UserCouncilFeeResponseDto>> exportedUserCouncilFeeDataMap
				= captureGeneratedExcelData();
			List<UserCouncilFeeResponseDto> exportedUserCouncilFeeList
				= exportedUserCouncilFeeDataMap.get(sheetName);

			verifyUserCouncilFeeResponseDto(
				exportedUserCouncilFeeList, userCouncilFee.getCouncilFeeFakeUser().getStudentId());
		}

		private LinkedHashMap<String, List<UserCouncilFeeResponseDto>> captureGeneratedExcelData() {
			ArgumentCaptor<LinkedHashMap<String, List<UserCouncilFeeResponseDto>>> captor =
				ArgumentCaptor.forClass(LinkedHashMap.class);
			verify(councilFeeExcelService, times(1))
				.generateExcel(eq(response), anyString(), anyList(), captor.capture());

			return captor.getValue();
		}

		private void verifyUserCouncilFeeResponseDto(
			List<UserCouncilFeeResponseDto> exportedUserCouncilFeeList,
			String studentId
		) {
			for (UserCouncilFeeResponseDto actual : exportedUserCouncilFeeList) {
				assertThat(actual).isNotNull();
				assertThat(actual.getStudentId())
					.as("실제 UserCouncilFeeResponseDto의 studentId가 존재해야 합니다.")
					.isNotNull();
				assertThat(actual.getStudentId())
					.as("실제 studentId와 입력한 studentId가 일치해야 합니다.")
					.isEqualTo(studentId);
			}
		}
	}
}
