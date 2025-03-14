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

    private static final List<Function<UserResponseDto, String>> cellMappingFunctions = List.of(
        user -> Optional.ofNullable(user.getEmail()).orElse(""),
        user -> Optional.ofNullable(user.getName()).orElse(""),
        user -> Optional.ofNullable(user.getStudentId()).orElse(""),
        user -> Optional.ofNullable(user.getAdmissionYear())
                        .map(String::valueOf)
                        .orElse(""),
        user -> Optional.ofNullable(user.getRoles())
                        .map(roles -> roles.stream()
                            .filter(role -> role != Role.NONE)
                            .map(Role::getDescription)
                            .collect(Collectors.joining(",")))
                        .orElse(""),
        user -> Optional.ofNullable(user.getState())
                        .map(UserState::getDescription)
                        .orElse(""),
        user -> Optional.ofNullable(user.getNickname()).orElse(""),
        user -> Optional.ofNullable(user.getMajor()).orElse(""),
        user -> Optional.ofNullable(user.getAcademicStatus())
                        .map(AcademicStatus::getValue)
                        .orElse(""),
        user -> Optional.ofNullable(user.getCurrentCompletedSemester())
                        .map(String::valueOf)
                        .orElse(""),
        user -> Optional.ofNullable(user.getGraduationYear())
                        .map(String::valueOf)
                        .orElse(""),
        user -> Optional.ofNullable(user.getGraduationType())
                        .map(GraduationType::getValue)
                        .orElse(""),
        user -> Optional.ofNullable(user.getPhoneNumber()).orElse(""),
        user -> Optional.ofNullable(user.getRejectionOrDropReason()).orElse(""),
        user -> Optional.ofNullable(user.getCreatedAt())
                        .map(String::valueOf)
                        .orElse(""),
        user -> Optional.ofNullable(user.getUpdatedAt())
                        .map(String::valueOf)
                        .orElse("")
    );

    @Override
    public void createDataRows(Sheet sheet, List<UserResponseDto> dataList) {
        int rowNum = 1;
        for (UserResponseDto user : dataList) {
            Row row = sheet.createRow(rowNum++);

            int colNum = 0;
            for (Function<UserResponseDto, String> func : cellMappingFunctions) {
                Cell cell = row.createCell(colNum++);
                cell.setCellValue(func.apply(user));
            }
        }
    }
}
