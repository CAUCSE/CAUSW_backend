package net.causw.application.excel;

import net.causw.application.dto.userCouncilFee.UserCouncilFeeResponseDto;
import net.causw.domain.aop.annotation.MeasureTime;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;

import java.util.List;
@MeasureTime
@Service
public class CouncilFeeExcelService extends ExcelAbstractService<UserCouncilFeeResponseDto> {

    @Override
    public void createDataRows(Sheet sheet, List<UserCouncilFeeResponseDto> dataList) {
        int rowNum = 1;
        for (UserCouncilFeeResponseDto userCouncilFeeResponseDto : dataList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(
                    (userCouncilFeeResponseDto.getIsJoinedService() != null) ?
                            (userCouncilFeeResponseDto.getIsJoinedService() ?
                                    "O" :
                                    "X") :
                            ""
            );
            row.createCell(1).setCellValue(
                    userCouncilFeeResponseDto.getEmail() != null ? userCouncilFeeResponseDto.getEmail() : ""
            );
            row.createCell(2).setCellValue(
                    userCouncilFeeResponseDto.getUserName() != null ? userCouncilFeeResponseDto.getUserName() : ""
            );
            row.createCell(3).setCellValue(
                    userCouncilFeeResponseDto.getStudentId() != null ? userCouncilFeeResponseDto.getStudentId() : ""
            );
            row.createCell(4).setCellValue(
                    userCouncilFeeResponseDto.getAdmissionYear() != null ? userCouncilFeeResponseDto.getAdmissionYear().toString() : ""
            );
            row.createCell(5).setCellValue(
                    userCouncilFeeResponseDto.getMajor() != null ? userCouncilFeeResponseDto.getMajor() : ""
            );
            row.createCell(6).setCellValue(
                    userCouncilFeeResponseDto.getAcademicStatus() != null ? userCouncilFeeResponseDto.getAcademicStatus().toString() : ""
            );
            row.createCell(7).setCellValue(
                    userCouncilFeeResponseDto.getCurrentCompletedSemester() != null ? userCouncilFeeResponseDto.getCurrentCompletedSemester().toString() : ""
            );
            row.createCell(8).setCellValue(
                    userCouncilFeeResponseDto.getGraduationYear() != null ? userCouncilFeeResponseDto.getGraduationYear().toString() : ""
            );
            row.createCell(9).setCellValue(
                    userCouncilFeeResponseDto.getGraduationType() != null ? userCouncilFeeResponseDto.getGraduationType().toString() : ""
            );
            row.createCell(10).setCellValue(
                    userCouncilFeeResponseDto.getPhoneNumber() != null ? userCouncilFeeResponseDto.getPhoneNumber() : ""
            );
            row.createCell(11).setCellValue(
                    userCouncilFeeResponseDto.getJoinedAt() != null ? userCouncilFeeResponseDto.getJoinedAt().toString() : ""
            );
            row.createCell(12).setCellValue(
                    userCouncilFeeResponseDto.getPaidAt() != null ? userCouncilFeeResponseDto.getPaidAt().toString() : ""
            );
            row.createCell(13).setCellValue(
                    userCouncilFeeResponseDto.getNumOfPaidSemester() != null ? userCouncilFeeResponseDto.getNumOfPaidSemester().toString() : ""
            );
            row.createCell(14).setCellValue(
                    (userCouncilFeeResponseDto.getIsRefunded() != null) ?
                            (userCouncilFeeResponseDto.getIsRefunded() ?
                                    "O" :
                                    "X") :
                            ""
            );
            row.createCell(15).setCellValue(
                    userCouncilFeeResponseDto.getRefundedAt() != null ? userCouncilFeeResponseDto.getRefundedAt().toString() : ""
            );
            row.createCell(16).setCellValue(
                    userCouncilFeeResponseDto.getRestOfSemester() != null ? userCouncilFeeResponseDto.getRestOfSemester().toString() : ""
            );
            row.createCell(17).setCellValue(
                    (userCouncilFeeResponseDto.getIsAppliedThisSemester() != null) ?
                            (userCouncilFeeResponseDto.getIsAppliedThisSemester() ?
                                    "O" :
                                    "X") :
                            ""
            );
        }
    }

}
