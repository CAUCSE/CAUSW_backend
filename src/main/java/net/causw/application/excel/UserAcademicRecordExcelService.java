package net.causw.application.excel;

import net.causw.application.dto.userAcademicRecordApplication.UserAcademicRecordApplicationResponseDto;
import net.causw.application.dto.userAcademicRecordApplication.UserAcademicRecordInfoResponseDto;
import net.causw.domain.aop.annotation.MeasureTime;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import java.util.List;

@MeasureTime
@Service
public class UserAcademicRecordExcelService extends ExcelAbstractService<UserAcademicRecordInfoResponseDto> {

    public void createSheet(Workbook workbook, String sheetName, List<UserAcademicRecordInfoResponseDto> dataList) {
        Sheet sheet = workbook.createSheet(sheetName);
        createHeaderRow(sheet);
        createDataRows(sheet, dataList);
    }

    @Override
    public void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);

        Cell cell = headerRow.createCell(0);
        cell.setCellValue("이름");

        cell = headerRow.createCell(1);
        cell.setCellValue("학번");

        cell = headerRow.createCell(2);
        cell.setCellValue("학적 상태");

        cell = headerRow.createCell(3);
        cell.setCellValue("본 학기 기준 등록 완료 학기 차수");

        cell = headerRow.createCell(4);
        cell.setCellValue("비고");

        cell = headerRow.createCell(5);
        cell.setCellValue("변환 타겟 학적 상태");

        cell = headerRow.createCell(6);
        cell.setCellValue("유저 작성 특이사항(단, 관리자 임의 수정 시 \"관리자 수정\"이라 기입)");

        cell = headerRow.createCell(7);
        cell.setCellValue("변경 날짜");
    }

    @Override
    public void createDataRows(Sheet sheet, List<UserAcademicRecordInfoResponseDto> dataList) {
        int rowNum = 1;
        for (UserAcademicRecordInfoResponseDto userAcademicRecordInfoResponseDto : dataList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(
                    (userAcademicRecordInfoResponseDto.getUserName() != null) ?
                            (userAcademicRecordInfoResponseDto.getUserName()) : ""
            );

            row.createCell(1).setCellValue(
                    (userAcademicRecordInfoResponseDto.getStudentId() != null) ?
                            (userAcademicRecordInfoResponseDto.getStudentId()) : ""
            );

            row.createCell(2).setCellValue(
                    (userAcademicRecordInfoResponseDto.getAcademicStatus() != null) ?
                            (userAcademicRecordInfoResponseDto.getAcademicStatus().getValue()) : ""
            );

            row.createCell(3).setCellValue(
                    (userAcademicRecordInfoResponseDto.getCurrentCompleteSemester() != null) ?
                            (userAcademicRecordInfoResponseDto.getCurrentCompleteSemester().toString()) : ""
            );

            row.createCell(4).setCellValue(
                    (userAcademicRecordInfoResponseDto.getNote() != null) ?
                            (userAcademicRecordInfoResponseDto.getNote()) : ""
            );

            List<UserAcademicRecordApplicationResponseDto> userAcademicRecordApplicationListResponseDtoList = userAcademicRecordInfoResponseDto.getUserAcademicRecordApplicationResponseDtoList();

            for (UserAcademicRecordApplicationResponseDto userAcademicRecordApplicationResponseDto : userAcademicRecordApplicationListResponseDtoList) {
                row.createCell(5).setCellValue(
                        (userAcademicRecordApplicationResponseDto.getTargetAcademicStatus() != null) ?
                                (userAcademicRecordApplicationResponseDto.getTargetAcademicStatus().getValue()) : ""
                );

                row.createCell(6).setCellValue(
                        (userAcademicRecordApplicationResponseDto.getUserNote() != null) ?
                                (userAcademicRecordApplicationResponseDto.getUserNote()) : ""
                );

                row.createCell(7).setCellValue(
                        (userAcademicRecordApplicationResponseDto.getChangeDate() != null) ?
                                (userAcademicRecordApplicationResponseDto.getChangeDate().toString()) : ""
                );

                row = sheet.createRow(rowNum++);
            }
        }

    }

}
