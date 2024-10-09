package net.causw.application.excel;

import net.causw.application.dto.user.UserResponseDto;
import net.causw.domain.aop.annotation.MeasureTime;
import net.causw.domain.model.enums.user.Role;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;

import java.util.List;

@MeasureTime
@Service
public class UserExcelService extends ExcelAbstractService<UserResponseDto> {

    @Override
    public void createDataRows(Sheet sheet, List<UserResponseDto> dataList) {
        int rowNum = 1;
        for (UserResponseDto user : dataList) {
            Row row = sheet.createRow(rowNum++);

            Cell cell = row.createCell(0);
            cell.setCellValue(
                    user.getEmail() != null ? user.getEmail() : ""
            );

            cell = row.createCell(1);
            cell.setCellValue(
                    user.getName() != null ? user.getName() : ""
            );

            cell = row.createCell(2);
            cell.setCellValue(
                    user.getStudentId() != null ? user.getStudentId() : ""
            );

            cell = row.createCell(3);
            cell.setCellValue(
                    user.getAdmissionYear() != null ? user.getAdmissionYear().toString() : ""
            );

            cell = row.createCell(4);
            cell.setCellValue(
                    user.getRoles() != null ? user.getRoles()
                            .stream()
                            .map(Role::getDescription).toString()
                            : ""
            );

            cell = row.createCell(5);
            cell.setCellValue(
                    user.getState() != null ? user.getState().getDescription() : ""
            );

            cell = row.createCell(6);
            cell.setCellValue(
                    user.getCircleNameIfLeader() != null ? user.getCircleNameIfLeader().toString() : ""
            );

            cell = row.createCell(7);
            cell.setCellValue(
                    user.getNickname() != null ? user.getNickname() : ""
            );

            cell = row.createCell(8);
            cell.setCellValue(
                    user.getMajor() != null ? user.getMajor() : ""
            );

            cell = row.createCell(9);
            cell.setCellValue(
                    user.getAcademicStatus() != null ? user.getAcademicStatus().getValue() : ""
            );

            cell = row.createCell(10);
            cell.setCellValue(
                    user.getCurrentCompletedSemester() != null ? user.getCurrentCompletedSemester().toString() : ""
            );

            cell = row.createCell(11);
            cell.setCellValue(
                    user.getGraduationYear() != null ? user.getGraduationYear().toString() : ""
            );

            cell = row.createCell(12);
            cell.setCellValue(
                    user.getGraduationType() != null ? user.getGraduationType().getValue() : ""
            );

            cell = row.createCell(13);
            cell.setCellValue(
                    user.getPhoneNumber() != null ? user.getPhoneNumber() : ""
            );

            cell = row.createCell(14);
            cell.setCellValue(
                    user.getRejectionOrDropReason() != null ? user.getRejectionOrDropReason() : ""
            );

            cell = row.createCell(15);
            cell.setCellValue(
                    user.getCreatedAt() != null ? user.getCreatedAt().toString() : ""
            );

            cell = row.createCell(16);
            cell.setCellValue(
                    user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : ""
            );
        }
    }
}
