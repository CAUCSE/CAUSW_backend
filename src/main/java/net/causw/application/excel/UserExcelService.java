package net.causw.application.excel;

import net.causw.application.dto.user.UserResponseDto;
import net.causw.domain.aop.annotation.MeasureTime;
import net.causw.domain.model.enums.Role;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;

import java.util.List;

@MeasureTime
@Service
public class UserExcelService extends ExcelAbstractService<UserResponseDto> {

    @Override
    public void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);

        Cell cell = headerRow.createCell(0);
        cell.setCellValue("아이디(이메일)");

        cell = headerRow.createCell(1);
        cell.setCellValue("이름");

        cell = headerRow.createCell(2);
        cell.setCellValue("학번");

        cell = headerRow.createCell(3);
        cell.setCellValue("입학년도");

        cell = headerRow.createCell(4);
        cell.setCellValue("역할");

        cell = headerRow.createCell(5);
        cell.setCellValue("상태");

        cell = headerRow.createCell(6);
        cell.setCellValue("동아리명 목록(동아리장일 경우)");

        cell = headerRow.createCell(7);
        cell.setCellValue("닉네임");

        cell = headerRow.createCell(8);
        cell.setCellValue("학부/학과");

        cell = headerRow.createCell(9);
        cell.setCellValue("학적 상태");

        cell = headerRow.createCell(10);
        cell.setCellValue("현재 등록 완료된 학기");

        cell = headerRow.createCell(11);
        cell.setCellValue("졸업 년도");

        cell = headerRow.createCell(12);
        cell.setCellValue("졸업 시기");

        cell = headerRow.createCell(13);
        cell.setCellValue("전화번호");

        cell = headerRow.createCell(14);
        cell.setCellValue("가입 거절/추방 사유");

        cell = headerRow.createCell(15);
        cell.setCellValue("동문네트워크 가입일");

        cell = headerRow.createCell(16);
        cell.setCellValue("사용자 정보 최종 수정일");
    }

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
