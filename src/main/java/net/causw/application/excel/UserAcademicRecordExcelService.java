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
