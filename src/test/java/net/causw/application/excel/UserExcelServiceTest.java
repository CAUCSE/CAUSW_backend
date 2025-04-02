package net.causw.application.excel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.application.dto.util.dtoMapper.UserDtoMapper;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.InternalServerException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class UserExcelServiceTest {

  @InjectMocks
  private UserExcelService userExcelService;

  User user;
  List<UserResponseDto> userList;
  List<String> headerStringList;
  String fileName;
  String sheetName;

  @BeforeEach
  void setUp() {
    user = mock(User.class);
    userList = List.of(UserDtoMapper.INSTANCE.toUserResponseDto(user));
    headerStringList = List.of("아이디(이메일)", "이름", "학번");
    fileName = LocalDateTime.now() + "_사용자명단";
    sheetName = "가입 대기 유저";
  }

  @Test
  @DisplayName("Excel 파일 생성 성공 - 정상적인 데이터")
  void testGenerateExcelSuccess() throws IOException {
    // given
    MockHttpServletResponse response = new MockHttpServletResponse();
    LinkedHashMap<String, List<UserResponseDto>> sheetDataMap = new LinkedHashMap<>();
    sheetDataMap.put(sheetName, userList);

    // when
    userExcelService.generateExcel(response, fileName, headerStringList, sheetDataMap);

    // then
    byte[] content = response.getContentAsByteArray();
    assertThat(content)
        .as("Excel 파일 내용이 비어 있지 않아야 합니다.")
        .isNotEmpty();

    Workbook createdWorkbook = new XSSFWorkbook(new ByteArrayInputStream(content));
    assertThat(createdWorkbook.getNumberOfSheets())
        .as("Excel 파일에 sheetDataMap의 사이즈와 같은 개수의 시트가 존재해야 합니다.")
        .isEqualTo(sheetDataMap.size());

    Sheet createdSheet = createdWorkbook.getSheetAt(0);
    assertThat(createdSheet.getSheetName())
        .as("시트 이름이 비어 있지 않아야 합니다.")
        .isNotBlank();
    assertThat(createdSheet.getSheetName())
        .as("시트 이름이 sheetDataMap에 포함되어야 합니다.")
        .isIn(sheetDataMap.keySet());
  }

  @Test
  @DisplayName("Excel 파일 생성 실패 - 빈 헤더 리스트")
  void testGenerateExcelWithEmptyHeaderListFailure() {
    // given
    List<String> headerStringList = List.of();
    MockHttpServletResponse response = new MockHttpServletResponse();
    LinkedHashMap<String, List<UserResponseDto>> sheetDataMap = new LinkedHashMap<>();
    sheetDataMap.put(sheetName, userList);

    // when
    // then
    assertThatThrownBy(() ->
        userExcelService.generateExcel(response, fileName, headerStringList, sheetDataMap))
        .as("헤더가 비어 있으면 예외가 발생해야 합니다.")
        .isInstanceOf(InternalServerException.class);
  }

  @Test
  @DisplayName("Excel 파일 생성 실패 - null 헤더 리스트")
  void testGenerateExcelWithNullHeaderListFailure() {
    // given
    List<String> headerStringList = null;
    MockHttpServletResponse response = new MockHttpServletResponse();
    LinkedHashMap<String, List<UserResponseDto>> sheetDataMap = new LinkedHashMap<>();
    sheetDataMap.put(sheetName, userList);

    // when
    // then
    assertThatThrownBy(() ->
        userExcelService.generateExcel(response, fileName, headerStringList, sheetDataMap))
        .as("헤더가 null이면 예외가 발생해야 합니다.")
        .isInstanceOf(InternalServerException.class);
  }

  @DisplayName("Excel 시트 데이터 생성 성공 - 정상적인 데이터")
  @Test
  void testCreateSheetSuccess() {
    // given
    int expectedRowNum = userList.size() + 1, expectedColNum = headerStringList.size();
    Workbook workbook = new XSSFWorkbook();

    // when
    userExcelService.createSheet(workbook, sheetName, headerStringList, userList);

    // then
    Sheet createdSheet = workbook.getSheetAt(0);

    verifyRow(createdSheet,expectedRowNum);
    verifyCell(createdSheet, expectedColNum);
  }

  @Test
  @DisplayName("Excel 시트 데이터 생성 성공 - 빈 데이터")
  void testGenerateExcelWithEmptyUserListSuccess() throws IOException {
    // given
    List<UserResponseDto> userList = List.of();
    int expectedRowNum = 1, expectedColNum = headerStringList.size();
    Workbook workbook = new XSSFWorkbook();

    // when
    userExcelService.createSheet(workbook, sheetName, headerStringList, userList);

    // then
    Sheet createdSheet = workbook.getSheetAt(0);

    verifyRow(createdSheet,expectedRowNum);
    verifyCell(createdSheet, expectedColNum);
  }

  private void verifyRow(Sheet sheet, int expectedRowNum) {
    assertThat(sheet.getPhysicalNumberOfRows())
        .as("헤더를 포함한 행의 개수는 %s이어야 합니다.", expectedRowNum)
        .isEqualTo(expectedRowNum);
  }

  private void verifyCell(Sheet sheet, int expectedColNum) {
    Row header = sheet.getRow(0);

    assertThat(header.getPhysicalNumberOfCells())
        .as("열의 개수는 headerStringList의 크기와 같아야 합니다.")
        .isEqualTo(expectedColNum);
    assertThat(sheet)
        .as("모든 셀은 null이 아니어야 합니다.")
        .usingRecursiveAssertion().isNotNull();
  }
}

