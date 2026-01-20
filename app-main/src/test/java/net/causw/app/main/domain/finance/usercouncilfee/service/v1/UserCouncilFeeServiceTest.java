package net.causw.app.main.domain.finance.usercouncilfee.service.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;

import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.campus.semester.entity.Semester;
import net.causw.app.main.domain.campus.semester.service.v1.SemesterService;
import net.causw.app.main.domain.finance.usercouncilfee.api.v1.dto.UserCouncilFeeResponseDto;
import net.causw.app.main.domain.finance.usercouncilfee.entity.UserCouncilFee;
import net.causw.app.main.domain.finance.usercouncilfee.repository.UserCouncilFeeRepository;
import net.causw.app.main.domain.finance.usercouncilfee.service.v1.UserCouncilFeeService;
import net.causw.app.main.domain.integration.export.service.v1.CouncilFeeExcelService;
import net.causw.app.main.util.ObjectFixtures;

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class UserCouncilFeeServiceTest {

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

	static final String sheetName = "학생회비 납부자 현황";
	static final Semester semester = ObjectFixtures.getSemester();

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
			LinkedHashMap<String, List<UserCouncilFeeResponseDto>> exportedUserCouncilFeeDataMap = captureGeneratedExcelData();
			List<UserCouncilFeeResponseDto> exportedUserCouncilFeeList = exportedUserCouncilFeeDataMap.get(sheetName);

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
			LinkedHashMap<String, List<UserCouncilFeeResponseDto>> exportedUserCouncilFeeDataMap = captureGeneratedExcelData();
			List<UserCouncilFeeResponseDto> exportedUserCouncilFeeList = exportedUserCouncilFeeDataMap.get(sheetName);

			verifyUserCouncilFeeResponseDto(
				exportedUserCouncilFeeList, userCouncilFee.getCouncilFeeFakeUser().getStudentId());
		}

		private LinkedHashMap<String, List<UserCouncilFeeResponseDto>> captureGeneratedExcelData() {
			ArgumentCaptor<LinkedHashMap<String, List<UserCouncilFeeResponseDto>>> captor = ArgumentCaptor
				.forClass(LinkedHashMap.class);
			verify(councilFeeExcelService, times(1))
				.generateExcel(eq(response), anyString(), anyList(), captor.capture());

			return captor.getValue();
		}

		private void verifyUserCouncilFeeResponseDto(
			List<UserCouncilFeeResponseDto> exportedUserCouncilFeeList,
			String studentId) {
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
