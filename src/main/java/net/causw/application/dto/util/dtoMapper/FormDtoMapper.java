package net.causw.application.dto.util.dtoMapper;

import net.causw.adapter.persistence.form.Form;
import net.causw.adapter.persistence.form.FormQuestionOption;
import net.causw.adapter.persistence.form.FormQuestion;
import net.causw.adapter.persistence.form.ReplyQuestion;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.userCouncilFee.UserCouncilFee;
import net.causw.application.dto.form.response.reply.*;
import net.causw.application.dto.form.response.*;
import net.causw.domain.model.enums.form.RegisteredSemester;
import net.causw.domain.model.enums.form.RegisteredSemesterManager;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface FormDtoMapper {

    FormDtoMapper INSTANCE = Mappers.getMapper(FormDtoMapper.class);

    @Named("getEnrolledRegisteredSemesterList")
    default List<RegisteredSemester> getRegisteredSemesterList(Form form) {
        return form.getEnrolledRegisteredSemester().stream().toList();
    }

    @Named("getLeaveOfAbsenceRegisteredSemesterList")
    default List<RegisteredSemester> getLeaveOfAbsenceRegisteredSemesterList(Form form) {
        return form.getLeaveOfAbsenceRegisteredSemester().stream().toList();
    }

    @Named("getSelectedOptionList")
    default List<Integer> getSelectedOptionList(ReplyQuestion replyQuestion) {
        return replyQuestion.getSelectedOptionList();
    }

    @Mapping(target = "formId", source = "form.id")
    @Mapping(target = "title", source = "form.title")
    @Mapping(target = "isClosed", source = "form.isClosed")
    @Mapping(target = "isAllowedEnrolled", source = "form.isAllowedEnrolled")
    @Mapping(target = "enrolledSemesterList", source = "form", qualifiedByName = "getEnrolledRegisteredSemesterList")
    @Mapping(target = "isNeedCouncilFeePaid", source = "form.isNeedCouncilFeePaid")
    @Mapping(target = "isAllowedLeaveOfAbsence", source = "form.isAllowedLeaveOfAbsence")
    @Mapping(target = "leaveOfAbsenceSemesterList", source = "form", qualifiedByName = "getLeaveOfAbsenceRegisteredSemesterList")
    @Mapping(target = "isAllowedGraduation", source = "form.isAllowedGraduation")
    @Mapping(target = "questionResponseDtoList", source = "questionResponseDtoList")
    FormResponseDto toFormResponseDto(Form form, List<QuestionResponseDto> questionResponseDtoList);

    @Mapping(target = "questionId", source = "formQuestion.id")
    @Mapping(target = "questionType", source = "formQuestion.questionType")
    @Mapping(target = "questionNumber", source = "formQuestion.number")
    @Mapping(target = "questionText", source = "formQuestion.questionText")
    @Mapping(target = "isMultiple", source = "formQuestion.isMultiple")
    @Mapping(target = "optionResponseDtoList", source = "optionResponseDtoList")
    QuestionResponseDto toQuestionResponseDto(FormQuestion formQuestion, List<OptionResponseDto> optionResponseDtoList);

    @Mapping(target = "optionId", source = "formQuestionOption.id")
    @Mapping(target = "optionNumber", source = "formQuestionOption.number")
    @Mapping(target = "optionText", source = "formQuestionOption.optionText")
    OptionResponseDto toOptionResponseDto(FormQuestionOption formQuestionOption);

    @Mapping(target = "questionId", source = "replyQuestion.formQuestion.id")
    @Mapping(target = "questionAnswer", source = "replyQuestion.questionAnswer")
    @Mapping(target = "selectedOptionList", source = "replyQuestion", qualifiedByName = "getSelectedOptionList")
    ReplyQuestionResponseDto toReplyQuestionResponseDto(ReplyQuestion replyQuestion);

    @Mapping(target = "replyUserResponseDto", source = "replyUserResponseDto")
    @Mapping(target = "replyQuestionResponseDtoList", source = "replyQuestionResponseDtoList")
    @Mapping(target = "createdAt", source = "createdAt")
    ReplyResponseDto toReplyResponseDto(ReplyUserResponseDto replyUserResponseDto, List<ReplyQuestionResponseDto> replyQuestionResponseDtoList, LocalDateTime createdAt);

    @Mapping(target = "optionId", source = "formQuestionOption.id")
    @Mapping(target = "optionNumber", source = "formQuestionOption.number")
    @Mapping(target = "optionText", source = "formQuestionOption.optionText")
    @Mapping(target = "selectedCount", source = "selectedCount")
    OptionSummaryResponseDto toOptionSummaryResponseDto(FormQuestionOption formQuestionOption, Long selectedCount);

    @Mapping(target = "questionId", source = "question.id")
    @Mapping(target = "questionText", source = "question.questionText")
    @Mapping(target = "questionAnswers", source = "questionAnswerList")
    @Mapping(target = "optionSummaries", source = "optionSummaryResponseDtoList")
    QuestionSummaryResponseDto toQuestionSummaryResponseDto(FormQuestion formQuestion, List<String> questionAnswerList, List<OptionSummaryResponseDto> optionSummaryResponseDtoList) ;

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "nickName", source = "user.nickName")
    @Mapping(target = "admissionYear", source = "user.admissionYear")
    @Mapping(target = "studentId", source = "user.studentId")
    @Mapping(target = "major", source = "user.major")
    @Mapping(target = "phoneNumber", source = "user.phoneNumber")
    @Mapping(target = "academicStatus", source = "user.academicStatus")
    @Mapping(target = "currentCompletedSemester", source = "user.currentCompletedSemester")
    @Mapping(target = "graduationYear", source = "user.graduationYear")
    @Mapping(target = "graduationType", source = "user.graduationType")
    @Mapping(target = "createdAt", source = "user.createdAt")
    @Mapping(target = "isAppliedThisSemester", source = "isAppliedThisSemester")
    @Mapping(target = "paidAt", source = "userCouncilFee.paidAt")
    @Mapping(target = "numOfPaidSemester", source = "userCouncilFee.numOfPaidSemester")
    @Mapping(target = "restOfSemester", source = "restOfSemester")
    @Mapping(target = "isRefunded", source = "userCouncilFee.isRefunded")
    ReplyUserResponseDto toReplyUserResponseDto(User user, UserCouncilFee userCouncilFee, Boolean isAppliedThisSemester, Integer restOfSemester);

    @Mapping(target = "questionResponseDtoList", source = "questionResponseDtoList")
    @Mapping(target = "replyResponseDtoPage", source = "replyResponseDtoPage")
    ReplyPageResponseDto toReplyPageResponseDto(List<QuestionResponseDto> questionResponseDtoList, Page<ReplyResponseDto> replyResponseDtoPage);

    @Mapping(target = "questionResponseDtoList", source = "questionResponseDtoList")
    @Mapping(target = "replyResponseDtoList", source = "replyResponseDtoList")
    ReplyListResponseDto toReplyListResponseDto(List<QuestionResponseDto> questionResponseDtoList, List<ReplyResponseDto> replyResponseDtoList);

}
