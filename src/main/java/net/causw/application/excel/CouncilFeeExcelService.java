package net.causw.application.excel;

import java.util.Optional;
import java.util.function.Function;
import net.causw.application.dto.userCouncilFee.UserCouncilFeeResponseDto;
import net.causw.domain.aop.annotation.MeasureTime;
import net.causw.domain.model.enums.user.GraduationType;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;

import java.util.List;
@MeasureTime
@Service
public class CouncilFeeExcelService extends ExcelAbstractService<UserCouncilFeeResponseDto> {

    private static final Function<UserCouncilFeeResponseDto, List<String>> cellMappingFunction =
        userCouncilFee -> List.of(
        Optional.ofNullable(userCouncilFee.getIsJoinedService())
                                  .map(isRefunded -> isRefunded ? "O" : "X")
                                  .orElse(""),
        Optional.ofNullable(userCouncilFee.getEmail()).orElse(""),
        Optional.ofNullable(userCouncilFee.getUserName()).orElse(""),
        Optional.ofNullable(userCouncilFee.getStudentId()).orElse(""),
        Optional.ofNullable(userCouncilFee.getAdmissionYear())
                .map(String::valueOf)
                .orElse(""),
        Optional.ofNullable(userCouncilFee.getNickname()).orElse(""),
        Optional.ofNullable(userCouncilFee.getMajor()).orElse(""),
        Optional.ofNullable(userCouncilFee.getAcademicStatus())
                .map(AcademicStatus::getValue)
                .orElse(""),
        Optional.ofNullable(userCouncilFee.getCurrentCompletedSemester())
                .map(String::valueOf)
                .orElse(""),
        Optional.ofNullable(userCouncilFee.getGraduationYear())
                .map(String::valueOf)
                .orElse(""),
        Optional.ofNullable(userCouncilFee.getGraduationType())
                .map(GraduationType::getValue)
                .orElse(""),
        Optional.ofNullable(userCouncilFee.getPhoneNumber()).orElse(""),
        Optional.ofNullable(userCouncilFee.getJoinedAt())
                .map(String::valueOf)
                .orElse(""),
        Optional.ofNullable(userCouncilFee.getPaidAt())
                .map(String::valueOf)
                .orElse(""),
        Optional.ofNullable(userCouncilFee.getNumOfPaidSemester())
                .map(String::valueOf)
                .orElse(""),
        Optional.ofNullable(userCouncilFee.getIsRefunded())
                .map(isRefunded -> isRefunded ? "O" : "X")
                .orElse(""),
        Optional.ofNullable(userCouncilFee.getIsRefunded())
                .filter(isRefunded -> isRefunded)
                .map(isRefunded ->
                    Optional.ofNullable(userCouncilFee.getRefundedAt())
                            .map(String::valueOf)
                            .orElse(""))
                .orElse(""),
        Optional.ofNullable(userCouncilFee.getRestOfSemester())
                .map(String::valueOf)
                .orElse(""),
        Optional.ofNullable(userCouncilFee.getIsAppliedThisSemester())
                .map(isRefunded -> isRefunded ? "O" : "X")
                .orElse("")
    );

    @Override
    public void createDataRows(Sheet sheet, List<UserCouncilFeeResponseDto> dataList) {
        int rowNum = 1;
        for (UserCouncilFeeResponseDto userCouncilFee : dataList) {
            Row row = sheet.createRow(rowNum++);

            int colNum = 0;
            List<String> cellValues = cellMappingFunction.apply(userCouncilFee);
            for (String value : cellValues) {
                Cell cell = row.createCell(colNum++);
                cell.setCellValue(value);
            }
        }
    }

}
