package net.causw.application.excel;

import net.causw.application.dto.circle.ExportCircleMemberToExcelResponseDto;
import net.causw.domain.aop.annotation.MeasureTime;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;

import java.util.List;

@MeasureTime
@Service
public class CircleExcelService extends ExcelAbstractService<ExportCircleMemberToExcelResponseDto> {

    @Override
    public void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);

        Cell cell = headerRow.createCell(0);
        cell.setCellValue("아이디(이메일)");

        cell = headerRow.createCell(1);
        cell.setCellValue("이름");

        cell = headerRow.createCell(2);
        cell.setCellValue("닉네임");

        cell = headerRow.createCell(3);
        cell.setCellValue("입학년도");

        cell = headerRow.createCell(4);
        cell.setCellValue("학번");

        cell = headerRow.createCell(5);
        cell.setCellValue("학부/학과");

        cell = headerRow.createCell(6);
        cell.setCellValue("연락처");

        cell = headerRow.createCell(7);
        cell.setCellValue("학적 상태");

        cell = headerRow.createCell(8);
        cell.setCellValue("현재 등록 완료된 학기");

        cell = headerRow.createCell(9);
        cell.setCellValue("졸업 년도");

        cell = headerRow.createCell(10);
        cell.setCellValue("졸업 시기");

        cell = headerRow.createCell(11);
        cell.setCellValue("동문네트워크 가입일");

        cell = headerRow.createCell(12);
        cell.setCellValue("본 학기 학생회비 납부 여부");

        cell = headerRow.createCell(13);
        cell.setCellValue("학생회비 납부 시점");

        cell = headerRow.createCell(14);
        cell.setCellValue("학생회비 납부 차수");

        cell = headerRow.createCell(15);
        cell.setCellValue("적용 학생회비 학기");

        cell = headerRow.createCell(16);
        cell.setCellValue("잔여 학생회비 적용 학기");

        cell = headerRow.createCell(17);
        cell.setCellValue("학생회비 환불 여부");
    }


    @Override
    public void createDataRows(Sheet sheet, List<ExportCircleMemberToExcelResponseDto> exportCircleMemberToExcelResponseDtoList) {
        int rowNum = 1;
        for (ExportCircleMemberToExcelResponseDto exportCircleMemberToExcelResponseDto : exportCircleMemberToExcelResponseDtoList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(
                    exportCircleMemberToExcelResponseDto.getEmail() != null ? exportCircleMemberToExcelResponseDto.getEmail() : ""
            );
            row.createCell(1).setCellValue(
                    exportCircleMemberToExcelResponseDto.getName() != null ? exportCircleMemberToExcelResponseDto.getName() : ""
            );
            row.createCell(2).setCellValue(
                    exportCircleMemberToExcelResponseDto.getNickname() != null ? exportCircleMemberToExcelResponseDto.getNickname() : ""
            );
            row.createCell(3).setCellValue(
                    exportCircleMemberToExcelResponseDto.getAdmissionYear() != null ? exportCircleMemberToExcelResponseDto.getAdmissionYear().toString() : ""
            );
            row.createCell(4).setCellValue(
                    exportCircleMemberToExcelResponseDto.getStudentId() != null ? exportCircleMemberToExcelResponseDto.getStudentId() : ""
            );
            row.createCell(5).setCellValue(
                    exportCircleMemberToExcelResponseDto.getMajor() != null ? exportCircleMemberToExcelResponseDto.getMajor() : ""
            );
            row.createCell(6).setCellValue(
                    exportCircleMemberToExcelResponseDto.getPhoneNumber() != null ? exportCircleMemberToExcelResponseDto.getPhoneNumber() : ""
            );
            row.createCell(7).setCellValue(
                    exportCircleMemberToExcelResponseDto.getAcademicStatus() != null ? exportCircleMemberToExcelResponseDto.getAcademicStatus().getValue() : ""
            );
            row.createCell(8).setCellValue(
                    exportCircleMemberToExcelResponseDto.getCurrentSemester() != null ? exportCircleMemberToExcelResponseDto.getCurrentSemester().toString() : ""
            );
            row.createCell(9).setCellValue(
                    exportCircleMemberToExcelResponseDto.getGraduationYear() != null ? exportCircleMemberToExcelResponseDto.getGraduationYear().toString() : ""
            );
            row.createCell(10).setCellValue(
                    exportCircleMemberToExcelResponseDto.getGraduationType() != null ? exportCircleMemberToExcelResponseDto.getGraduationType().getValue() : ""
            );
            row.createCell(11).setCellValue(
                    exportCircleMemberToExcelResponseDto.getCreatedAt() != null ? exportCircleMemberToExcelResponseDto.getCreatedAt().toString() : ""
            );
            row.createCell(12).setCellValue(
                    exportCircleMemberToExcelResponseDto.getIsAppliedThisSemester() != null ?
                            exportCircleMemberToExcelResponseDto.getIsAppliedThisSemester() ? "O" : "X"
                            : ""
            );
            row.createCell(13).setCellValue(
                    exportCircleMemberToExcelResponseDto.getPaidAt() != null ? exportCircleMemberToExcelResponseDto.getPaidAt().toString() : ""
            );
            row.createCell(14).setCellValue(
                    exportCircleMemberToExcelResponseDto.getPaidSemester() != null ? exportCircleMemberToExcelResponseDto.getPaidSemester().toString() : ""
            );
            row.createCell(15).setCellValue(
                    exportCircleMemberToExcelResponseDto.getAppliedSemester() != null ? exportCircleMemberToExcelResponseDto.getAppliedSemester().toString() : ""
            );
            row.createCell(16).setCellValue(
                    exportCircleMemberToExcelResponseDto.getRestOfSemester() != null ? exportCircleMemberToExcelResponseDto.getRestOfSemester().toString() : ""
            );
            row.createCell(17).setCellValue(
                    exportCircleMemberToExcelResponseDto.getIsRefunded() != null ?
                            exportCircleMemberToExcelResponseDto.getIsRefunded() ? "O" : "X"
                            : ""
            );

        }
    }


}
