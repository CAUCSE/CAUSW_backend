package net.causw.application.dto.util.dtoMapper;

import net.causw.adapter.persistence.form.Form;
import net.causw.adapter.persistence.form.Option;
import net.causw.adapter.persistence.form.Question;
import net.causw.adapter.persistence.form.Reply;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.form.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FormDtoMapper {

    FormDtoMapper INSTANCE = Mappers.getMapper(FormDtoMapper.class);

    @Mapping(target = "formId", source = "form.id")
    @Mapping(target = "questions", source = "form.questions")
    FormResponseDto toFormResponseDto(Form form);

    @Mapping(target = "questionNumber", source = "question.number")
    @Mapping(target = "isMultiple", source = "question.isMultiple")
    @Mapping(target = "options", source = "question.options")
    QuestionResponseDto toQuestionResponseDto(Question question);

    @Mapping(target = "optionNumber", source = "option.number")
    OptionResponseDto toOptionResponseDto(Option option);

    @Mapping(target = "questionId", source = "reply.question.id")
    QuestionReplyResponseDto toQuestionReplyResponseDto(Reply reply);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "replies", source = "replies")
    ReplyUserResponseDto toReplyUserResponseDto(User user, List<QuestionReplyResponseDto> replies);

    OptionSummaryResponseDto toOptionSummaryResponseDto(Option option, Long selectedCount);

    @Mapping(target = "questionId", source = "question.id")
    @Mapping(target = "questionText", source = "question.questionText")
    @Mapping(target = "questionAnswers", source = "questionAnswers")
    @Mapping(target = "optionSummaries", source = "optionSummaries")
    QuestionSummaryResponseDto toQuestionSummaryResponseDto(Question question, List<String> questionAnswers, List<OptionSummaryResponseDto> optionSummaries) ;


}
