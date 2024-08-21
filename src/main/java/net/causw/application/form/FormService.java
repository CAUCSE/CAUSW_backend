package net.causw.application.form;

import jakarta.validation.Validator;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.repository.CircleMemberRepository;
import net.causw.adapter.persistence.repository.CircleRepository;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.CircleMemberStatus;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.validation.CircleMemberStatusValidator;
import net.causw.domain.validation.UserEqualValidator;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.ValidatorBucket;
import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.form.Form;
import net.causw.adapter.persistence.form.Question;
import net.causw.adapter.persistence.repository.FormRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.form.FormCreateRequestDto;
import net.causw.application.dto.form.FormResponseDto;
import net.causw.application.dto.form.QuestionResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormService {
    private final FormRepository formRepository;
    private final CircleRepository circleRepository;
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

    @Transactional(readOnly = true)
    public FormResponseDto findForm(String formId) {
        Form form = getForm(formId);
        return toFormResponseDto(form);
    }



    private FormResponseDto toFormResponseDto(Form form) {
        List<QuestionResponseDto> questionResponseDtos = form.getQuestions().stream()
                .map(this::toQuestionResponseDto)
                .collect(Collectors.toList());

        UserResponseDto writerDto = UserResponseDto.from(form.getWriter());

        return new FormResponseDto(form.getId(), form.getTitle(), writerDto, form.getAllowedGrades(), questionResponseDtos);
    }

    private QuestionResponseDto toQuestionResponseDto(Question question) {
        List<OptionResponseDto> optionResponseDtos = question.getOptions().stream()
                .map(this::toOptionResponseDto)
                .collect(Collectors.toList());

        return new QuestionResponseDto(question.getId(), question.getNumber(), question.getQuestionText(), question.getIsMultiple(), optionResponseDtos);
    }

    private OptionResponseDto toOptionResponseDto(Option option){
        return new OptionResponseDto(option.getNumber(), option.getOptionText(), option.getIsSelected());
    }

    private Form getForm(String formId){
        return formRepository.findById(formId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.FORM_NOT_FOUND
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
