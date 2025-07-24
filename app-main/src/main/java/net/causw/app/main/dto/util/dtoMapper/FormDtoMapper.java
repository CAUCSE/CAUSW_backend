package net.causw.app.main.dto.util.dtoMapper;

import java.time.LocalDateTime;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import net.causw.app.main.domain.model.entity.form.Form;
import net.causw.app.main.domain.model.entity.form.FormQuestion;
import net.causw.app.main.domain.model.entity.form.FormQuestionOption;
import net.causw.app.main.domain.model.entity.form.Reply;
import net.causw.app.main.domain.model.entity.form.ReplyQuestion;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.userCouncilFee.UserCouncilFee;
import net.causw.app.main.domain.model.enums.form.RegisteredSemester;
import net.causw.app.main.dto.form.response.FormResponseDto;
import net.causw.app.main.dto.form.response.OptionResponseDto;
import net.causw.app.main.dto.form.response.OptionSummaryResponseDto;
import net.causw.app.main.dto.form.response.QuestionResponseDto;
import net.causw.app.main.dto.form.response.QuestionSummaryResponseDto;
import net.causw.app.main.dto.form.response.reply.ReplyPageResponseDto;
import net.causw.app.main.dto.form.response.reply.ReplyQuestionResponseDto;
import net.causw.app.main.dto.form.response.reply.ReplyResponseDto;
import net.causw.app.main.dto.form.response.reply.ReplyUserResponseDto;
import net.causw.app.main.dto.form.response.reply.UserReplyResponseDto;
import net.causw.app.main.dto.form.response.reply.excel.ExcelReplyListResponseDto;
import net.causw.app.main.dto.form.response.reply.excel.ExcelReplyQuestionResponseDto;
import net.causw.app.main.dto.form.response.reply.excel.ExcelReplyResponseDto;

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

	@Named("getSelectedOptionTextList")
	default List<String> getSelectedOptionTextList(ReplyQuestion replyQuestion) {
		List<Integer> selectedOptionList = replyQuestion.getSelectedOptionList();

		List<FormQuestionOption> formQuestionOptionList = replyQuestion.getFormQuestion().getFormQuestionOptionList();

		return formQuestionOptionList.stream()
			.filter(formQuestionOption -> selectedOptionList.contains(formQuestionOption.getNumber()))
			.map(formQuestionOption -> {
				return formQuestionOption.getNumber() + ". " + formQuestionOption.getOptionText();
			})
			.toList();
	}

	@Mapping(target = "formId", source = "form.id")
	@Mapping(target = "title", source = "form.title")
	@Mapping(target = "isClosed", source = "form.isClosed")
	@Mapping(target = "isAllowedEnrolled", source = "form.isAllowedEnrolled")
	@Mapping(target = "enrolledRegisteredSemesterList", source = "form", qualifiedByName = "getEnrolledRegisteredSemesterList")
	@Mapping(target = "isNeedCouncilFeePaid", source = "form.isNeedCouncilFeePaid")
	@Mapping(target = "isAllowedLeaveOfAbsence", source = "form.isAllowedLeaveOfAbsence")
	@Mapping(target = "leaveOfAbsenceRegisteredSemesterList", source = "form", qualifiedByName = "getLeaveOfAbsenceRegisteredSemesterList")
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
	ReplyResponseDto toReplyResponseDto(ReplyUserResponseDto replyUserResponseDto,
		List<ReplyQuestionResponseDto> replyQuestionResponseDtoList, LocalDateTime createdAt);

	@Mapping(target = "questionResponseDtoList", source = "questionResponseDtoList")
	@Mapping(target = "replyQuestionResponseDtoList", source = "reply.replyQuestionList")
	UserReplyResponseDto toUserReplyResponseDto(Reply reply, List<QuestionResponseDto> questionResponseDtoList);

	@Mapping(target = "optionId", source = "formQuestionOption.id")
	@Mapping(target = "optionNumber", source = "formQuestionOption.number")
	@Mapping(target = "optionText", source = "formQuestionOption.optionText")
	@Mapping(target = "selectedCount", source = "selectedCount")
	OptionSummaryResponseDto toOptionSummaryResponseDto(FormQuestionOption formQuestionOption, Long selectedCount);

	@Mapping(target = "questionId", source = "formQuestion.id")
	@Mapping(target = "questionText", source = "formQuestion.questionText")
	@Mapping(target = "questionAnswerList", source = "questionAnswerList")
	@Mapping(target = "optionSummarieList", source = "optionSummaryResponseDtoList")
	@Mapping(target = "numOfReply", source = "numOfReply")
	@Mapping(target = "isMultiple", source = "isMultiple")
	QuestionSummaryResponseDto toQuestionSummaryResponseDto(FormQuestion formQuestion, List<String> questionAnswerList,
		List<OptionSummaryResponseDto> optionSummaryResponseDtoList, Long numOfReply, Boolean isMultiple);

	@Mapping(target = "userId", source = "user.id")
	@Mapping(target = "email", source = "user.email")
	@Mapping(target = "name", source = "user.name")
	@Mapping(target = "nickName", source = "user.nickname")
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
	ReplyUserResponseDto toReplyUserResponseDto(User user, UserCouncilFee userCouncilFee, Boolean isAppliedThisSemester,
		Integer restOfSemester);

	default ReplyPageResponseDto toReplyPageResponseDto(List<QuestionResponseDto> questionResponseDtoList,
		Page<ReplyResponseDto> replyResponseDtoPage) {
		return ReplyPageResponseDto.builder()
			.questionResponseDtoList(questionResponseDtoList)
			.replyResponseDtoPage(replyResponseDtoPage)
			.build();
	}

	default ExcelReplyListResponseDto toExcelReplyListResponseDto(List<QuestionResponseDto> questionResponseDtoList,
		List<ExcelReplyResponseDto> excelReplyResponseDtoList) {
		return ExcelReplyListResponseDto.builder()
			.questionResponseDtoList(questionResponseDtoList)
			.excelReplyResponseDtoList(excelReplyResponseDtoList)
			.build();
	}

	@Mapping(target = "questionId", source = "replyQuestion.formQuestion.id")
	@Mapping(target = "questionAnswer", source = "replyQuestion.questionAnswer")
	@Mapping(target = "selectedOptionTextList", source = "replyQuestion", qualifiedByName = "getSelectedOptionTextList")
	ExcelReplyQuestionResponseDto toExcelReplyQuestionResponseDto(ReplyQuestion replyQuestion);

	@Mapping(target = "replyUserResponseDto", source = "replyUserResponseDto")
	@Mapping(target = "excelReplyQuestionResponseDtoList", source = "excelReplyQuestionResponseDtoList")
	@Mapping(target = "createdAt", source = "createdAt")
	ExcelReplyResponseDto toExcelReplyResponseDto(ReplyUserResponseDto replyUserResponseDto,
		List<ExcelReplyQuestionResponseDto> excelReplyQuestionResponseDtoList, LocalDateTime createdAt);
}
