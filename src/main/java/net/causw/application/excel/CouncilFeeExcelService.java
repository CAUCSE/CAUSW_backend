package net.causw.application.excel;

import net.causw.application.dto.userCouncilFee.UserCouncilFeeResponseDto;
import net.causw.domain.aop.annotation.MeasureTime;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;

import java.util.List;
@MeasureTime
@Service
public class CouncilFeeExcelService extends ExcelAbstractService<UserCouncilFeeResponseDto> {

    @Override
    public void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        Cell cell = headerRow.createCell(0);
        cell.setCellValue("동문네트워크 서비스 가입 여부");

        cell = headerRow.createCell(1);
        cell.setCellValue("이메일(아이디)");

        cell = headerRow.createCell(2);
        cell.setCellValue("이름");

        cell = headerRow.createCell(3);
        cell.setCellValue("학번");

        cell = headerRow.createCell(4);
        cell.setCellValue("입학년도");

        cell = headerRow.createCell(5);
        cell.setCellValue("전공");

        cell = headerRow.createCell(6);
        cell.setCellValue("학적상태");

        cell = headerRow.createCell(7);
        cell.setCellValue("등록 완료 학기");

        cell = headerRow.createCell(8);
        cell.setCellValue("졸업년도");

        cell = headerRow.createCell(9);
        cell.setCellValue("졸업 유형");

        cell = headerRow.createCell(10);
        cell.setCellValue("전화번호");

        cell = headerRow.createCell(11);
        cell.setCellValue("동문 네트워크 가입일");

        cell = headerRow.createCell(12);
        cell.setCellValue("납부 시점 학기");

        cell = headerRow.createCell(13);
        cell.setCellValue("납부한 학기 수");

        cell = headerRow.createCell(14);
        cell.setCellValue("환불 여부");

        cell = headerRow.createCell(15);
        cell.setCellValue("환불 시점");

        cell = headerRow.createCell(16);
        cell.setCellValue("잔여 학생회비 적용 학기");

        cell = headerRow.createCell(17);
        cell.setCellValue("본 학기 학생회비 적용 여부");
    }

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
