package net.causw.application.excel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.application.dto.userCouncilFee.UserCouncilFeeResponseDto;
import net.causw.domain.exceptions.InternalServerException;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockHttpServletResponse;

public class ExcelAbstractServiceTest {

  static final List<String> headerStringList = List.of("아이디(이메일)", "이름", "학번");
  static final String fileName = "fileName";
  static final String sheetName = "sheetName";

  static Stream<Arguments> provideExcelServices(){
    return Stream.of(
        Arguments.arguments(new UserExcelService(), List.of(mock(UserResponseDto.class))),
        Arguments.arguments(new CouncilFeeExcelService(), List.of(mock(UserCouncilFeeResponseDto.class))));
  }

  @ParameterizedTest
  @MethodSource("provideExcelServices")
  @DisplayName("Excel 파일 생성 성공 - 정상적인 데이터")
  void testGenerateExcelSuccess(
      ExcelAbstractService<T> service, List<T> dataList) throws IOException {
    // given
    int expectedRowNum = dataList.size() + 1, expectedColNum = headerStringList.size();
    MockHttpServletResponse response = new MockHttpServletResponse();
    LinkedHashMap<String, List<T>> sheetDataMap = new LinkedHashMap<>();
    sheetDataMap.put(sheetName, dataList);

    // when
    service.generateExcel(response, fileName, headerStringList, sheetDataMap);

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

    verifySheet(createdSheet, expectedRowNum);
    verifyHeaderRow(createdSheet.getRow(0), expectedColNum);
    verifyDataRow(createdSheet.getRow(1));
  }

  @ParameterizedTest
  @MethodSource("provideExcelServices")
  @DisplayName("Excel 파일 생성 실패 - 빈 헤더 리스트")
  void testGenerateExcelWithEmptyHeaderListFailure(ExcelAbstractService<T> service, List<T> dataList) {
    // given
    List<String> headerStringList = List.of();
    MockHttpServletResponse response = new MockHttpServletResponse();
    LinkedHashMap<String, List<T>> sheetDataMap = new LinkedHashMap<>();
    sheetDataMap.put(sheetName, dataList);

    // when & then
    assertThatThrownBy(() ->
        service.generateExcel(response, fileName, headerStringList, sheetDataMap))
        .as("헤더가 비어 있으면 예외가 발생해야 합니다.")
        .isInstanceOf(InternalServerException.class);
  }

  @ParameterizedTest
  @MethodSource("provideExcelServices")
  @DisplayName("Excel 파일 생성 실패 - null 헤더 리스트")
  void testGenerateExcelWithNullHeaderListFailure(ExcelAbstractService<T> service, List<T> dataList) {
    // given
    List<String> headerStringList = null;
    MockHttpServletResponse response = new MockHttpServletResponse();
    LinkedHashMap<String, List<T>> sheetDataMap = new LinkedHashMap<>();
    sheetDataMap.put(sheetName, dataList);

    // when & then
    assertThatThrownBy(() ->
        service.generateExcel(response, fileName, headerStringList, sheetDataMap))
        .as("헤더가 null이면 예외가 발생해야 합니다.")
        .isInstanceOf(InternalServerException.class);
  }

  @ParameterizedTest
  @MethodSource("provideExcelServices")
  @DisplayName("Excel 시트 데이터 생성 성공 - 정상적인 데이터")
  void testCreateSheetSuccess(ExcelAbstractService<T> service, List<T> dataList) {
    // given
    int expectedRowNum = dataList.size() + 1, expectedColNum = headerStringList.size();
    Workbook workbook = new XSSFWorkbook();

    // when
    service.createSheet(workbook, sheetName, headerStringList, dataList);

    // then
    Sheet createdSheet = workbook.getSheetAt(0);

    verifySheet(createdSheet, expectedRowNum);
    verifyHeaderRow(createdSheet.getRow(0), expectedColNum);
    verifyDataRow(createdSheet.getRow(1));
  }

  @ParameterizedTest
  @MethodSource("provideExcelServices")
  @DisplayName("Excel 시트 데이터 생성 성공 - 빈 데이터")
  void testGenerateExcelWithEmptyUserListSuccess(ExcelAbstractService<T> service) {
    // given
    List<T> dataList = List.of();
    int expectedRowNum = 1, expectedColNum = headerStringList.size();
    Workbook workbook = new XSSFWorkbook();

    // when
    service.createSheet(workbook, sheetName, headerStringList, dataList);

    // then
    Sheet createdSheet = workbook.getSheetAt(0);

    verifySheet(createdSheet, expectedRowNum);
    verifyHeaderRow(createdSheet.getRow(0), expectedColNum);
  }

  private void verifySheet(Sheet sheet, int expectedRowNum) {
    assertThat(sheet).isNotNull();
    assertThat(sheet.getPhysicalNumberOfRows())
        .as("헤더를 포함한 행의 개수가 예측값과 같아야 합니다.")
        .isEqualTo(expectedRowNum);

    sheet.iterator().forEachRemaining(
        row -> assertThat(row)
              .as("모든 행은 null이 아니어야 합니다.")
              .isNotNull()
    );
  }

  private void verifyHeaderRow(Row row, int expectedColNum) {
    assertThat(row).isNotNull();
    assertThat(row.getPhysicalNumberOfCells())
        .as("셀의 개수가 예측값과 같아야 합니다.")
        .isEqualTo(expectedColNum);

    row.iterator().forEachRemaining(
        cell -> {
          assertThat(cell)
              .as("모든 셀은 null이 아니어야 합니다.")
              .isNotNull();
          assertThat(cell.getCellType())
              .as("모든 셀은 STRING 타입이어야 합니다.")
              .isNotNull().isEqualTo(CellType.STRING);
          assertThat(cell.getStringCellValue())
              .as("실제 엑셀의 헤더와 headerStringList의 내용이 일치해야 합니다.")
              .isNotNull().isEqualTo(headerStringList.get(cell.getColumnIndex()));
        }
    );
  }

  private void verifyDataRow(Row row) {
    assertThat(row).isNotNull();

    row.iterator().forEachRemaining(
        cell -> {
          assertThat(cell)
              .as("모든 셀은 null이 아니어야 합니다.")
              .isNotNull();
          assertThat(cell.getCellType())
              .as("모든 셀은 STRING 타입이어야 합니다.")
              .isNotNull().isEqualTo(CellType.STRING);
        }
    );
  }
}
