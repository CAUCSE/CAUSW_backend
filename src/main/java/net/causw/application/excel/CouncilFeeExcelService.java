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

    private static final List<Function<UserCouncilFeeResponseDto, String>> cellMappingFunctions = List.of(
        userCouncilFee -> Optional.ofNullable(userCouncilFee.getIsJoinedService())
                                  .map(isRefunded -> isRefunded ? "O" : "X")
                                  .orElse(""),
        userCouncilFee -> Optional.ofNullable(userCouncilFee.getEmail()).orElse(""),
        userCouncilFee -> Optional.ofNullable(userCouncilFee.getUserName()).orElse(""),
        userCouncilFee -> Optional.ofNullable(userCouncilFee.getStudentId()).orElse(""),
        userCouncilFee -> Optional.ofNullable(userCouncilFee.getAdmissionYear())
                                  .map(String::valueOf)
                                  .orElse(""),
        userCouncilFee -> Optional.ofNullable(userCouncilFee.getNickname()).orElse(""),
        userCouncilFee -> Optional.ofNullable(userCouncilFee.getMajor()).orElse(""),
        userCouncilFee -> Optional.ofNullable(userCouncilFee.getAcademicStatus())
                                  .map(AcademicStatus::getValue)
                                  .orElse(""),
        userCouncilFee -> Optional.ofNullable(userCouncilFee.getCurrentCompletedSemester())
                                  .map(String::valueOf)
                                  .orElse(""),
        userCouncilFee -> Optional.ofNullable(userCouncilFee.getGraduationYear())
                                  .map(String::valueOf)
                                  .orElse(""),
        userCouncilFee -> Optional.ofNullable(userCouncilFee.getGraduationType())
                                  .map(GraduationType::getValue)
                                  .orElse(""),
        userCouncilFee -> Optional.ofNullable(userCouncilFee.getPhoneNumber()).orElse(""),
        userCouncilFee -> Optional.ofNullable(userCouncilFee.getJoinedAt())
                                  .map(String::valueOf)
                                  .orElse(""),
        userCouncilFee -> Optional.ofNullable(userCouncilFee.getPaidAt())
                                  .map(String::valueOf)
                                  .orElse(""),
        userCouncilFee -> Optional.ofNullable(userCouncilFee.getNumOfPaidSemester())
                                  .map(String::valueOf)
                                  .orElse(""),
        userCouncilFee -> Optional.ofNullable(userCouncilFee.getIsRefunded())
                                  .map(isRefunded -> isRefunded ? "O" : "X")
                                  .orElse(""),
        userCouncilFee -> Optional.ofNullable(userCouncilFee.getIsRefunded())
                                  .filter(isRefunded -> isRefunded)
                                  .map(isRefunded -> Optional.ofNullable(userCouncilFee.getRefundedAt())
                                      .map(String::valueOf)
                                      .orElse(""))
                                  .orElse(""),
        userCouncilFee -> Optional.ofNullable(userCouncilFee.getRestOfSemester())
                                  .map(String::valueOf)
                                  .orElse(""),
        userCouncilFee -> Optional.ofNullable(userCouncilFee.getIsAppliedThisSemester())
                                  .map(isRefunded -> isRefunded ? "O" : "X")
                                  .orElse("")
    );

    @Override
    public void createDataRows(Sheet sheet, List<UserCouncilFeeResponseDto> dataList) {
        int rowNum = 1;
        for (UserCouncilFeeResponseDto userCouncilFee : dataList) {
            Row row = sheet.createRow(rowNum++);

            int colNum = 0;
            for (Function<UserCouncilFeeResponseDto, String> func : cellMappingFunctions) {
                Cell cell = row.createCell(colNum++);
                cell.setCellValue(func.apply(userCouncilFee));
            }
        }
    }

}
