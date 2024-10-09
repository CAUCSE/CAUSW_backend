package net.causw.application.excel;

import net.causw.application.dto.form.response.reply.excel.ExcelReplyQuestionResponseDto;
import net.causw.application.dto.form.response.reply.excel.ExcelReplyResponseDto;
import net.causw.domain.aop.annotation.MeasureTime;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.util.MessageUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;

import java.util.List;

@MeasureTime
@Service
public class FormExcelService extends ExcelAbstractService<ExcelReplyResponseDto> {

    @Override
    public void createDataRows(Sheet sheet, List<ExcelReplyResponseDto> excelReplyResponseDtoList) {
        int rowNum = 1;

        for (ExcelReplyResponseDto excelReplyResponseDto : excelReplyResponseDtoList) {
            Row row = sheet.createRow(rowNum++);

            int cellNum = 0;

            row.createCell(cellNum++).setCellValue(
                    excelReplyResponseDto.getCreatedAt() != null ? excelReplyResponseDto.getCreatedAt().toString() : ""
            );

            row.createCell(cellNum++).setCellValue(
                    excelReplyResponseDto.getReplyUserResponseDto() != null ?
                            excelReplyResponseDto.getReplyUserResponseDto().getEmail() != null ?
                                    excelReplyResponseDto.getReplyUserResponseDto().getEmail() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    excelReplyResponseDto.getReplyUserResponseDto() != null ?
                            excelReplyResponseDto.getReplyUserResponseDto().getName() != null ?
                                    excelReplyResponseDto.getReplyUserResponseDto().getName() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    excelReplyResponseDto.getReplyUserResponseDto() != null ?
                            excelReplyResponseDto.getReplyUserResponseDto().getNickName() != null ?
                                    excelReplyResponseDto.getReplyUserResponseDto().getNickName() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    excelReplyResponseDto.getReplyUserResponseDto() != null ?
                            excelReplyResponseDto.getReplyUserResponseDto().getAdmissionYear() != null ?
                                    excelReplyResponseDto.getReplyUserResponseDto().getAdmissionYear().toString() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    excelReplyResponseDto.getReplyUserResponseDto() != null ?
                            excelReplyResponseDto.getReplyUserResponseDto().getStudentId() != null ?
                                    excelReplyResponseDto.getReplyUserResponseDto().getStudentId() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    excelReplyResponseDto.getReplyUserResponseDto() != null ?
                            excelReplyResponseDto.getReplyUserResponseDto().getMajor() != null ?
                                    excelReplyResponseDto.getReplyUserResponseDto().getMajor() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    excelReplyResponseDto.getReplyUserResponseDto() != null ?
                            excelReplyResponseDto.getReplyUserResponseDto().getPhoneNumber() != null ?
                                    excelReplyResponseDto.getReplyUserResponseDto().getPhoneNumber().toString() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    excelReplyResponseDto.getReplyUserResponseDto() != null ?
                            excelReplyResponseDto.getReplyUserResponseDto().getAcademicStatus() != null ?
                                    excelReplyResponseDto.getReplyUserResponseDto().getAcademicStatus().getValue() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    excelReplyResponseDto.getReplyUserResponseDto() != null ?
                            excelReplyResponseDto.getReplyUserResponseDto().getCurrentCompletedSemester() != null ?
                                    excelReplyResponseDto.getReplyUserResponseDto().getCurrentCompletedSemester().toString() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    excelReplyResponseDto.getReplyUserResponseDto() != null ?
                            excelReplyResponseDto.getReplyUserResponseDto().getGraduationYear() != null ?
                                    excelReplyResponseDto.getReplyUserResponseDto().getGraduationYear().toString() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    excelReplyResponseDto.getReplyUserResponseDto() != null ?
                            excelReplyResponseDto.getReplyUserResponseDto().getGraduationType() != null ?
                                    excelReplyResponseDto.getReplyUserResponseDto().getGraduationType().getValue() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    excelReplyResponseDto.getReplyUserResponseDto() != null ?
                            excelReplyResponseDto.getReplyUserResponseDto().getCreatedAt() != null ?
                                    excelReplyResponseDto.getReplyUserResponseDto().getCreatedAt().toString() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    excelReplyResponseDto.getReplyUserResponseDto() != null ?
                            excelReplyResponseDto.getReplyUserResponseDto().getIsAppliedThisSemester() != null ?
                                    excelReplyResponseDto.getReplyUserResponseDto().getIsAppliedThisSemester() ? "O" : "X"
                                    : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    excelReplyResponseDto.getReplyUserResponseDto() != null ?
                            excelReplyResponseDto.getReplyUserResponseDto().getPaidAt() != null ?
                                    excelReplyResponseDto.getReplyUserResponseDto().getPaidAt().toString() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    excelReplyResponseDto.getReplyUserResponseDto() != null ?
                            excelReplyResponseDto.getReplyUserResponseDto().getNumOfPaidSemester() != null ?
                                    excelReplyResponseDto.getReplyUserResponseDto().getNumOfPaidSemester().toString() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    excelReplyResponseDto.getReplyUserResponseDto() != null ?
                            excelReplyResponseDto.getReplyUserResponseDto().getRestOfSemester() != null ?
                                    excelReplyResponseDto.getReplyUserResponseDto().getRestOfSemester().toString() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    excelReplyResponseDto.getReplyUserResponseDto() != null ?
                            excelReplyResponseDto.getReplyUserResponseDto().getIsRefunded() != null ?
                                    excelReplyResponseDto.getReplyUserResponseDto().getIsRefunded() ? "O" : "X"
                                    : ""
                            : ""
            );

            for (ExcelReplyQuestionResponseDto excelReplyQuestionResponseDto : excelReplyResponseDto.getExcelReplyQuestionResponseDtoList()) {
                if (excelReplyQuestionResponseDto.getQuestionAnswer() != null &&
                        excelReplyQuestionResponseDto.getSelectedOptionTextList().isEmpty()
                ) {
                    row.createCell(cellNum++).setCellValue(
                            excelReplyQuestionResponseDto.getQuestionAnswer()
                    );
                } else if (excelReplyQuestionResponseDto.getQuestionAnswer() == null &&
                        !excelReplyQuestionResponseDto.getSelectedOptionTextList().isEmpty()
                ) {
                    String selectedOptionTextList = String.join("\n", excelReplyQuestionResponseDto.getSelectedOptionTextList());
                    row.createCell(cellNum++).setCellValue(selectedOptionTextList);
                } else {
                    throw new InternalServerException(
                            ErrorCode.INTERNAL_SERVER,
                            MessageUtil.INTERNAL_SERVER_ERROR
                    );
                }
            }
        }
    }

}
