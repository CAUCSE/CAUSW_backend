package net.causw.application.excel;

import net.causw.application.dto.form.response.reply.ReplyQuestionResponseDto;
import net.causw.application.dto.form.response.reply.ReplyResponseDto;
import net.causw.domain.aop.annotation.MeasureTime;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;

import java.util.List;

@MeasureTime
@Service
public class FormExcelService extends ExcelAbstractService<ReplyResponseDto> {

    @Override
    public void createDataRows(Sheet sheet, List<ReplyResponseDto> replyResponseDtoList) {
        int rowNum = 1;

        for (ReplyResponseDto replyResponseDto : replyResponseDtoList) {
            Row row = sheet.createRow(rowNum++);

            int cellNum = 0;

            row.createCell(cellNum++).setCellValue(
                    replyResponseDto.getCreatedAt() != null ? replyResponseDto.getCreatedAt().toString() : ""
            );

            row.createCell(cellNum++).setCellValue(
                    replyResponseDto.getReplyUserResponseDto() != null ?
                            replyResponseDto.getReplyUserResponseDto().getEmail() != null ?
                                    replyResponseDto.getReplyUserResponseDto().getEmail() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    replyResponseDto.getReplyUserResponseDto() != null ?
                            replyResponseDto.getReplyUserResponseDto().getName() != null ?
                                    replyResponseDto.getReplyUserResponseDto().getName() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    replyResponseDto.getReplyUserResponseDto() != null ?
                            replyResponseDto.getReplyUserResponseDto().getNickName() != null ?
                                    replyResponseDto.getReplyUserResponseDto().getNickName() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    replyResponseDto.getReplyUserResponseDto() != null ?
                            replyResponseDto.getReplyUserResponseDto().getAdmissionYear() != null ?
                                    replyResponseDto.getReplyUserResponseDto().getAdmissionYear().toString() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    replyResponseDto.getReplyUserResponseDto() != null ?
                            replyResponseDto.getReplyUserResponseDto().getStudentId() != null ?
                                    replyResponseDto.getReplyUserResponseDto().getStudentId() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    replyResponseDto.getReplyUserResponseDto() != null ?
                            replyResponseDto.getReplyUserResponseDto().getMajor() != null ?
                                    replyResponseDto.getReplyUserResponseDto().getMajor() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    replyResponseDto.getReplyUserResponseDto() != null ?
                            replyResponseDto.getReplyUserResponseDto().getPhoneNumber() != null ?
                                    replyResponseDto.getReplyUserResponseDto().getPhoneNumber().toString() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    replyResponseDto.getReplyUserResponseDto() != null ?
                            replyResponseDto.getReplyUserResponseDto().getAcademicStatus() != null ?
                                    replyResponseDto.getReplyUserResponseDto().getAcademicStatus().getValue() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    replyResponseDto.getReplyUserResponseDto() != null ?
                            replyResponseDto.getReplyUserResponseDto().getCurrentCompletedSemester() != null ?
                                    replyResponseDto.getReplyUserResponseDto().getCurrentCompletedSemester().toString() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    replyResponseDto.getReplyUserResponseDto() != null ?
                            replyResponseDto.getReplyUserResponseDto().getGraduationYear() != null ?
                                    replyResponseDto.getReplyUserResponseDto().getGraduationYear().toString() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    replyResponseDto.getReplyUserResponseDto() != null ?
                            replyResponseDto.getReplyUserResponseDto().getGraduationType() != null ?
                                    replyResponseDto.getReplyUserResponseDto().getGraduationType().getValue() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    replyResponseDto.getReplyUserResponseDto() != null ?
                            replyResponseDto.getReplyUserResponseDto().getCreatedAt() != null ?
                                    replyResponseDto.getReplyUserResponseDto().getCreatedAt().toString() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    replyResponseDto.getReplyUserResponseDto() != null ?
                            replyResponseDto.getReplyUserResponseDto().getIsAppliedThisSemester() != null ?
                                    replyResponseDto.getReplyUserResponseDto().getIsAppliedThisSemester() ? "O" : "X"
                                    : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    replyResponseDto.getReplyUserResponseDto() != null ?
                            replyResponseDto.getReplyUserResponseDto().getPaidAt() != null ?
                                    replyResponseDto.getReplyUserResponseDto().getPaidAt().toString() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    replyResponseDto.getReplyUserResponseDto() != null ?
                            replyResponseDto.getReplyUserResponseDto().getNumOfPaidSemester() != null ?
                                    replyResponseDto.getReplyUserResponseDto().getNumOfPaidSemester().toString() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    replyResponseDto.getReplyUserResponseDto() != null ?
                            replyResponseDto.getReplyUserResponseDto().getRestOfSemester() != null ?
                                    replyResponseDto.getReplyUserResponseDto().getRestOfSemester().toString() : ""
                            : ""
            );

            row.createCell(cellNum++).setCellValue(
                    replyResponseDto.getReplyUserResponseDto() != null ?
                            replyResponseDto.getReplyUserResponseDto().getIsRefunded() != null ?
                                    replyResponseDto.getReplyUserResponseDto().getIsRefunded() ? "O" : "X"
                                    : ""
                            : ""
            );

            for (ReplyQuestionResponseDto replyQuestionResponseDto : replyResponseDto.getReplyQuestionResponseDtoList()) {

            }
        }
    }

}
