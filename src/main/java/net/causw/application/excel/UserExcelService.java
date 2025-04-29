package net.causw.application.excel;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.domain.aop.annotation.MeasureTime;
import net.causw.domain.model.enums.user.GraduationType;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.enums.user.UserState;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;

import java.util.List;

@MeasureTime
@Service
public class UserExcelService extends ExcelAbstractService<UserResponseDto> {

    private static final Function<UserResponseDto, List<String>> cellMappingFunction =
        user -> List.of(
            Optional.ofNullable(user.getEmail()).orElse(""),
            Optional.ofNullable(user.getName()).orElse(""),
            Optional.ofNullable(user.getStudentId()).orElse(""),
            Optional.ofNullable(user.getAdmissionYear())
                    .map(String::valueOf)
                    .orElse(""),
            Optional.ofNullable(user.getRoles())
                    .map(roles -> roles.stream()
                            .map(Role::getDescription)
                            .collect(Collectors.joining(",")))
                    .orElse(""),
            Optional.ofNullable(user.getState())
                    .map(UserState::getDescription)
                    .orElse(""),
            Optional.ofNullable(user.getNickname()).orElse(""),
            Optional.ofNullable(user.getMajor()).orElse(""),
            Optional.ofNullable(user.getAcademicStatus())
                    .map(AcademicStatus::getValue)
                    .orElse(""),
            Optional.ofNullable(user.getCurrentCompletedSemester())
                    .map(String::valueOf)
                    .orElse(""),
            Optional.ofNullable(user.getGraduationYear())
                    .map(String::valueOf)
                    .orElse(""),
            Optional.ofNullable(user.getGraduationType())
                    .map(GraduationType::getValue)
                    .orElse(""),
            Optional.ofNullable(user.getPhoneNumber()).orElse(""),
            Optional.ofNullable(user.getCircleNameIfLeader())
                    .map(circles -> String.join(",", circles))
                    .orElse(""),
            Optional.ofNullable(user.getRejectionOrDropReason()).orElse(""),
            Optional.ofNullable(user.getCreatedAt())
                    .map(String::valueOf)
                    .orElse(""),
            Optional.ofNullable(user.getUpdatedAt())
                    .map(String::valueOf)
                    .orElse("")
    );

    @Override
    public void createDataRows(Sheet sheet, List<UserResponseDto> dataList) {
        int rowNum = 1;
        for (UserResponseDto user : dataList) {
            Row row = sheet.createRow(rowNum++);

            int colNum = 0;
            List<String> cellValues = cellMappingFunction.apply(user);
            for (String value : cellValues) {
                Cell cell = row.createCell(colNum++);
                cell.setCellValue(value);
            }
        }
    }
}
