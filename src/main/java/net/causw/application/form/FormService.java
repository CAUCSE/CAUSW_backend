package net.causw.application.form;

import jakarta.validation.Validator;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.form.Option;
import net.causw.adapter.persistence.form.Reply;
import net.causw.adapter.persistence.repository.*;
import net.causw.application.dto.form.*;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.CircleMemberStatus;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.validation.CircleMemberStatusValidator;
import net.causw.domain.validation.UserEqualValidator;
import net.causw.domain.validation.ValidatorBucket;
import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.form.Form;
import net.causw.adapter.persistence.form.Question;
import net.causw.adapter.persistence.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormService {
    private final FormRepository formRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final CircleRepository circleRepository;
    private final ReplyRepository replyRepository;
    private final CircleMemberRepository circleMemberRepository;
    private final Validator validator;

    @Transactional
    public FormResponseDto createForm(User writer, FormCreateRequestDto formCreateRequestDto) {

        ValidatorBucket validatorBucket = ValidatorBucket.of();

        Circle circle = null;
        if(formCreateRequestDto.getCircleId() != null){
            circle = getCircle(formCreateRequestDto.getCircleId());

            CircleMember circleMember = getCircleMember(writer.getId(), circle.getId());
            validatorBucket
                    .consistOf(UserEqualValidator.of(
                            getCircleLeader(circle).getId(),
                            writer.getId()))
                    .consistOf(CircleMemberStatusValidator.of(
                            circleMember.getStatus(),
                            List.of(CircleMemberStatus.MEMBER)
                    ));
        }

        List<Question> questions = Optional.ofNullable(formCreateRequestDto.getQuestions())
                .orElse(new ArrayList<>())
                .stream().map(questionDto -> {
            List<Option> options = Optional.ofNullable(questionDto.getOptions())
                    .orElse(new ArrayList<>())
                    .stream()
                    .map(optionDto -> Option.of(
                            optionDto.getOptionNumber(),
                            optionDto.getOptionText(),
                            null
                    )).collect(Collectors.toList());

            Question question = Question.of(
                    questionDto.getQuestionNumber(),
                    questionDto.getQuestionText(),
                    questionDto.getIsMultiple(),
                    options,
                    null
            );

            options.forEach(option -> option.setQuestion(question));

            return question;
        }).collect(Collectors.toList());


        Form form = Form.of(
                formCreateRequestDto.getTitle(),
                formCreateRequestDto.getAllowedGrades(),
                questions,
                writer,
                circle
        );

        questions.forEach(question -> question.setForm(form));

        formRepository.save(form);

        return toFormResponseDto(form);
    }

    @Transactional
    public void deleteForm(String formId, User deleter){
        Form form = getForm(formId);

        //삭제 권한은... 작성자랑... 관리자? 정도
        //어차피 폼 생성은 관리자, 동아리장만 가리

        form.setIsDeleted(true);
        formRepository.save(form);
    }

    @Transactional(readOnly = true)
    public FormResponseDto findForm(String formId) {
        Form form = getForm(formId);
        return toFormResponseDto(form);
    }

    @Transactional
    public void replyForm(String formId, FormReplyRequestDto formReplyRequestDto, User writer){
        Form form = getForm(formId);

        for(QuestionReplyRequestDto questionReplyRequestDto : formReplyRequestDto.getReplyDtos()){
            Question question = getQuestion(questionReplyRequestDto.getQuestionId());
            Reply reply = Reply.of(
                    form,
                    writer,
                    question,
                    questionReplyRequestDto.getQuestionReply(),
                    questionReplyRequestDto.getSelectedOptions()
            );
            replyRepository.save(reply);
        }
    }



    private FormResponseDto toFormResponseDto(Form form) {
        List<QuestionResponseDto> questionResponseDtos = form.getQuestions().stream()
                .map(this::toQuestionResponseDto)
                .collect(Collectors.toList());

        UserResponseDto writerDto = UserResponseDto.from(form.getWriter());

        return new FormResponseDto(form.getId(), form.getTitle(), writerDto, form.getAllowedGrades(), questionResponseDtos);
    }


        return new QuestionResponseDto(question.getId(), question.getNumber(), question.getQuestionText(), question.getIsMultiple(), optionResponseDtos);
    }

    private OptionResponseDto toOptionResponseDto(Option option){
        return new OptionResponseDto(option.getId(), option.getNumber(), option.getOptionText(), option.getIsSelected());
    }

    private Form getForm(String formId){
        return formRepository.findById(formId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.FORM_NOT_FOUND
                )
        );
    }

    private Question getQuestion(String questionId){
        return questionRepository.findById(questionId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.QUESTION_NOT_FOUND
                )
        );
    }

    private Option getOption(String optionId){
        return optionRepository.findById(optionId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.OPTION_NOT_FOUND
                )
        );

    }

    private Circle getCircle(String circleId) {
        return circleRepository.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.SMALL_CLUB_NOT_FOUND
                )
        );
    }

    private CircleMember getCircleMember(String userId, String circleId) {
        return circleMemberRepository.findByUser_IdAndCircle_Id(userId, circleId).orElseThrow(
                () -> new UnauthorizedException(
                        ErrorCode.NOT_MEMBER,
                        MessageUtil.CIRCLE_APPLY_INVALID
                )
        );
    }

    private User getCircleLeader(Circle circle) {
        User leader = circle.getLeader().orElse(null);
        if (leader == null) {
            throw new InternalServerException(
                    ErrorCode.INTERNAL_SERVER,
                    MessageUtil.CIRCLE_WITHOUT_LEADER
            );
        }
        return leader;
    }
}
