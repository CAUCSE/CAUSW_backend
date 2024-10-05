package net.causw.application.form;

import jakarta.servlet.http.HttpServletResponse;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.form.*;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.board.BoardRepository;
import net.causw.adapter.persistence.repository.circle.CircleMemberRepository;
import net.causw.adapter.persistence.repository.circle.CircleRepository;
import net.causw.adapter.persistence.repository.form.*;
import net.causw.adapter.persistence.repository.post.PostRepository;
import net.causw.adapter.persistence.repository.user.UserRepository;
import net.causw.adapter.persistence.repository.userCouncilFee.UserCouncilFeeRepository;
import net.causw.adapter.persistence.userCouncilFee.UserCouncilFee;
import net.causw.application.dto.form.response.*;
import net.causw.application.dto.form.response.reply.*;
import net.causw.application.dto.form.request.FormReplyRequestDto;
import net.causw.application.dto.form.request.QuestionReplyRequestDto;
import net.causw.application.dto.form.response.reply.excel.ExcelReplyListResponseDto;
import net.causw.application.dto.form.response.reply.excel.ExcelReplyQuestionResponseDto;
import net.causw.application.dto.form.response.reply.excel.ExcelReplyResponseDto;
import net.causw.application.dto.util.dtoMapper.FormDtoMapper;
import net.causw.application.excel.FormExcelService;
import net.causw.domain.aop.annotation.MeasureTime;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.circle.CircleMemberStatus;
import net.causw.domain.model.enums.form.FormType;
import net.causw.domain.model.enums.form.QuestionType;
import net.causw.domain.model.enums.form.RegisteredSemester;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.domain.model.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@MeasureTime
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FormService {
    
    private final FormRepository formRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final BoardRepository boardRepository;
    private final CircleRepository circleRepository;
    private final ReplyQuestionRepository replyQuestionRepository;
    private final UserRepository userRepository;
    private final CircleMemberRepository circleMemberRepository;
    private final UserCouncilFeeRepository userCouncilFeeRepository;
    private final ReplyRepository replyRepository;
    private final PostRepository postRepository;
    private final FormExcelService formExcelService;

    @Transactional
    public void setFormIsClosed(
            String formId,
            User user,
            Boolean targetIsClosed
    ) {
        Form form = getForm(formId);

        validateCanAccessFormResult(user, form);

        if (form.getFormType().equals(FormType.CIRCLE_APPLICATION_FORM)) {
            Circle circle = form.getCircle();
            circle.setIsRecruit(false);
            circleRepository.save(circle);
        }

        form.setIsClosed(targetIsClosed);

        formRepository.save(form);
    }
    
    public Boolean getCanReplyToPostForm(User user, String formId) {
        Form form = getForm(formId);

        if (form.getIsClosed()) {
            return false;
        }

        try {
            // writer가 해당 form이 있는 post에 접근 가능한지 검사
            validCanAccessPost(user, form);

            // writer가 해당 form에 대한 권한이 있는지 확인
            validateToReply(user, form);

            // 중복 답변 검사
            validDuplicateReplyExist(user, form);
        } catch (BadRequestException e) {
            return false;
        }

        return true;
    }

    public FormResponseDto getFormById(User user, String formId) {
        Form form = getForm(formId);

        validCanAccessPost(user, form);

        return this.toFormResponseDto(form);
    }

    @Transactional
    public void replyForm(
            String formId,
            FormReplyRequestDto formReplyRequestDto,
            User writer
    ) {
        Form form = getForm(formId);
        
        // 동아리 신청서의 경우 본 API 통해서 답변 불가. Circle Controller 사용 필수
        if (form.getFormType().equals(FormType.CIRCLE_APPLICATION_FORM)) {
            throw new BadRequestException(
                    ErrorCode.NOT_ALLOWED_TO_REPLY_FORM,
                    MessageUtil.NOT_ALLOWED_TO_REPLY_FORM
            );
        }

        // 제출 가능한지 검사
        if (form.getIsClosed()) {
            throw new BadRequestException(
                    ErrorCode.NOT_ALLOWED_TO_REPLY_FORM,
                    MessageUtil.FORM_CLOSED
            );
        }

        // writer가 해당 form이 있는 post에 접근 가능한지 검사
        validCanAccessPost(writer, form);

        // writer가 해당 form에 대한 권한이 있는지 확인
        validateToReply(writer, form);

        // 중복 답변 검사
        validDuplicateReplyExist(writer, form);

        // 주관식, 객관식 질문에 따라 유효한 답변인지 검증 및 저장
        List<ReplyQuestion> replyQuestionList = new ArrayList<>();

        for (QuestionReplyRequestDto questionReplyRequestDto : formReplyRequestDto.getQuestionReplyRequestDtoList()) {
            FormQuestion formQuestion = getQuestion(questionReplyRequestDto.getQuestionId());

            // 객관식일 시
            if (formQuestion.getQuestionType().equals(QuestionType.OBJECTIVE)) {
                if (questionReplyRequestDto.getQuestionReply() != null) {
                    throw new BadRequestException(
                            ErrorCode.INVALID_PARAMETER,
                            MessageUtil.INVALID_REPLY_INFO
                    );
                }

                if (!formQuestion.getIsMultiple() && questionReplyRequestDto.getSelectedOptionList().size() > 1) {
                    throw new BadRequestException(
                            ErrorCode.INVALID_PARAMETER,
                            MessageUtil.INVALID_REPLY_INFO
                    );
                }

                // 객관식일 시: 유효한 옵션 번호 선택했는지 검사
                List<Integer> formQuestionOptionNumberList = formQuestion.getFormQuestionOptionList()
                        .stream()
                        .map(FormQuestionOption::getNumber)
                        .toList();

                questionReplyRequestDto.getSelectedOptionList().forEach(optionNumber -> {
                    if (!formQuestionOptionNumberList.contains(optionNumber)) {
                        throw new BadRequestException(
                                ErrorCode.INVALID_PARAMETER,
                                MessageUtil.INVALID_REPLY_INFO
                        );
                    }
                });
            }
            // 주관식일 시
            else {
                if (questionReplyRequestDto.getSelectedOptionList() != null) {
                    throw new BadRequestException(
                            ErrorCode.INVALID_PARAMETER,
                            MessageUtil.INVALID_REPLY_INFO
                    );
                }
            }

            ReplyQuestion replyQuestion = ReplyQuestion.of(
                    formQuestion,
                    formQuestion.getQuestionType().equals(QuestionType.SUBJECTIVE) ?
                            questionReplyRequestDto.getQuestionReply()
                            : null,
                    formQuestion.getQuestionType().equals(QuestionType.OBJECTIVE) ?
                            questionReplyRequestDto.getSelectedOptionList()
                            : null
            );

            replyQuestionList.add(replyQuestion);
        }

        // 답변 개수 맞는지 확인
        if (replyQuestionList.size() != form.getFormQuestionList().size()) {
            throw new BadRequestException(
                    ErrorCode.INVALID_PARAMETER,
                    MessageUtil.REPLY_SIZE_INVALID
            );
        }

        // 모든 문항에 대해 답변 정확히 하나 있는지 검사(답변 유효성 검사)
        form.getFormQuestionList().forEach(formQuestion -> {
            int questionReplyCount = 0;
            for (ReplyQuestion replyQuestion : replyQuestionList) {
                if (formQuestion.equals(replyQuestion.getFormQuestion())) {
                    questionReplyCount++;
                }
            }
            if (questionReplyCount != 1) {
                throw new BadRequestException(
                        ErrorCode.INVALID_PARAMETER,
                        MessageUtil.INVALID_REPLY_INFO
                );
            }
        });

        Reply reply = Reply.of(form, writer, replyQuestionList);

        replyQuestionList.forEach(replyQuestion -> replyQuestion.setReply(reply));

        replyRepository.save(reply);
    }

    public ReplyPageResponseDto findAllReplyPageByForm(String formId, Pageable pageable, User user){
        Form form = getForm(formId);

        validateCanAccessFormResult(user, form);

        Page<Reply> replyPage = replyRepository.findAllByForm(form, pageable);

        return toReplyPageResponseDto(form, replyPage);
    }

    // 각 질문별 결과를 반환(요약)
    public List<QuestionSummaryResponseDto> findSummaryReply(String formId, User user){
        Form form = getForm(formId);

        validateCanAccessFormResult(user, form);

        List<FormQuestion> formQuestionList = form.getFormQuestionList();

        List<ReplyQuestion> replyQuestionList = replyQuestionRepository.findAllByReplyForm(form);

        Map<FormQuestion, List<ReplyQuestion>> replyQuestionMap = formQuestionList
                .stream()
                .collect(Collectors.toMap(
                        formQuestion -> formQuestion,
                        formQuestion -> replyQuestionList.stream()
                                .filter(replyQuestion -> replyQuestion.getFormQuestion().equals(formQuestion))
                                .collect(Collectors.toList())
                ));

        List<QuestionSummaryResponseDto> questionSummaryResponseDtoList = new ArrayList<>();
        for (FormQuestion formQuestion : formQuestionList) {
            questionSummaryResponseDtoList.add(
                    toQuestionSummaryResponseDto(
                            formQuestion,
                            replyQuestionMap.get(formQuestion)
                    )
            );
        }

        return questionSummaryResponseDtoList;
    }

    public void exportFormResult(String formId, User user, HttpServletResponse response) {
        Form form = getForm(formId);

        validateCanAccessFormResult(user, form);

        String fileName = form.getTitle() + "_결과";

        List<Reply> replyList = replyRepository.findAllByForm(form);

        ExcelReplyListResponseDto excelReplyListResponseDto = toExcelReplyListResponseDto(form, replyList);

        List<String> headerStringList = new ArrayList<>(List.of(
                "제출 시각",
                "이메일(아이디)",
                "이름",
                "닉네임",
                "입학년도",
                "학번",
                "학부/학과",
                "전화번호",
                "학적상태",
                "현재 학기",
                "졸업 년도",
                "졸업 유형",
                "동문네트워크 가입일",
                "본 학기 학생회비 적용 여부",
                "납부 시점 학기",
                "납부한 학기 수",
                "잔여 학생회비 적용 학기",
                "환불 여부"
        ));
        List<String> questionStringList = excelReplyListResponseDto.getQuestionResponseDtoList()
                .stream()
                .map(questionResponseDto -> (
                        questionResponseDto.getQuestionNumber().toString()
                                + "."
                                + questionResponseDto.getQuestionText()
                )).toList();
        headerStringList.addAll(questionStringList);

        LinkedHashMap<String, List<ExcelReplyResponseDto>> sheetNameDataMap = new LinkedHashMap<>();
        sheetNameDataMap.put("결과", excelReplyListResponseDto.getExcelReplyResponseDtoList());

        formExcelService.generateExcel(
                response,
                fileName,
                headerStringList,
                sheetNameDataMap
        );
    }

    // private methods
    // 중복 답변 검사
    private void validDuplicateReplyExist(User writer, Form form) {
        if (replyRepository.existsByFormAndUser(form, writer)) {
            throw new BadRequestException(
                    ErrorCode.ROW_ALREADY_EXIST,
                    MessageUtil.ALREADY_REPLIED
            );
        }
    }

    // writer가 해당 form에 대한 권한이 있는지 확인
    private void validateToReply(User writer, Form form) {
        Set<AcademicStatus> allowedAcademicStatus = new HashSet<>();

        if (form.getIsAllowedEnrolled())
            allowedAcademicStatus.add(AcademicStatus.ENROLLED);
        if (form.getIsAllowedLeaveOfAbsence())
            allowedAcademicStatus.add(AcademicStatus.LEAVE_OF_ABSENCE);
        if (form.getIsAllowedGraduation())
            allowedAcademicStatus.add(AcademicStatus.GRADUATED);

        if (!allowedAcademicStatus.contains(writer.getAcademicStatus())) {
            throw new BadRequestException(
                    ErrorCode.NOT_ALLOWED_TO_REPLY_FORM,
                    MessageUtil.NOT_ALLOWED_TO_REPLY_FORM
            );
        } else {
            if (allowedAcademicStatus.contains(AcademicStatus.ENROLLED)
                    && writer.getAcademicStatus().equals(AcademicStatus.ENROLLED)
            ) {
                EnumSet<RegisteredSemester> allowedRegisteredSemester = form.getEnrolledRegisteredSemester();
                if (!allowedRegisteredSemester
                        .stream()
                        .map(RegisteredSemester::getSemester)
                        .collect(Collectors.toSet())
                        .contains(writer.getCurrentCompletedSemester())
                ) {
                    throw new BadRequestException(
                            ErrorCode.NOT_ALLOWED_TO_REPLY_FORM,
                            MessageUtil.NOT_ALLOWED_TO_REPLY_FORM
                    );
                }

                if (form.getIsNeedCouncilFeePaid()) {
                    // 학생회비 납부 필요
                    UserCouncilFee userCouncilFee = userCouncilFeeRepository.findByUser(writer).orElseThrow(
                            () -> new BadRequestException(
                                    ErrorCode.NOT_ALLOWED_TO_REPLY_FORM,
                                    MessageUtil.NOT_ALLOWED_TO_REPLY_FORM
                            )
                    );

                    if (!getIsAppliedCurrentSemester(userCouncilFee)) {
                        throw new BadRequestException(
                                ErrorCode.NOT_ALLOWED_TO_REPLY_FORM,
                                MessageUtil.NOT_ALLOWED_TO_REPLY_FORM
                        );
                    }
                }
            }

            if (allowedAcademicStatus.contains(AcademicStatus.LEAVE_OF_ABSENCE)
                    && writer.getAcademicStatus().equals(AcademicStatus.LEAVE_OF_ABSENCE)
            ) {
                EnumSet<RegisteredSemester> allowedRegisteredSemester = form.getLeaveOfAbsenceRegisteredSemester();
                if (!allowedRegisteredSemester
                        .stream()
                        .map(RegisteredSemester::getSemester)
                        .collect(Collectors.toSet())
                        .contains(writer.getCurrentCompletedSemester())
                ) {
                    throw new BadRequestException(
                            ErrorCode.NOT_ALLOWED_TO_REPLY_FORM,
                            MessageUtil.NOT_ALLOWED_TO_REPLY_FORM
                    );
                }
            }
        }
    }

    private void validateCanAccessFormResult(User user, Form form) {
        if (form.getFormType().equals(FormType.POST_FORM)) {
            Post post = getPost(form);
            if (!post.getWriter().equals(user)) {
                throw new UnauthorizedException(
                        ErrorCode.API_NOT_ALLOWED,
                        MessageUtil.NOT_ALLOWED_TO_ACCESS_REPLY
                );
            }
        } else {
            Circle circle = form.getCircle();
            if (circle == null) {
                throw new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                );
            }
            CircleMember circleMember = circleMemberRepository.findByUser_IdAndCircle_Id(user.getId(), circle.getId()).orElseThrow(
                    () -> new UnauthorizedException(
                            ErrorCode.NOT_MEMBER,
                            MessageUtil.CIRCLE_APPLY_INVALID
                    )
            );
            if (circleMember.getStatus().equals(CircleMemberStatus.MEMBER) ||
                    !user.getRoles().contains(Role.LEADER_CIRCLE)
            ) {
                throw new UnauthorizedException(
                        ErrorCode.API_NOT_ALLOWED,
                        MessageUtil.NOT_ALLOWED_TO_ACCESS_REPLY
                );
            }
        }
    }

    private User getUser(String userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
        );
    }

    private Form getForm(String formId){
        return formRepository.findByIdAndIsDeleted(formId, false).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.FORM_NOT_FOUND
                )
        );
    }

    private FormQuestion getQuestion(String questionId){
        return questionRepository.findById(questionId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.QUESTION_NOT_FOUND
                )
        );
    }

    private FormQuestionOption getOption(String optionId){
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

    private Board getBoard(String boardId) {
        return boardRepository.findById(boardId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.BOARD_NOT_FOUND
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

    private Boolean getIsAppliedCurrentSemester(UserCouncilFee userCouncilFee) {
        Integer startOfAppliedSemester = userCouncilFee.getPaidAt();
        Integer endOfAppliedSemester = ( userCouncilFee.getIsRefunded() ) ?
                ( startOfAppliedSemester - 1 ) + userCouncilFee.getNumOfPaidSemester() :
                userCouncilFee.getRefundedAt();
        Boolean isAppliedThisSemester;

        if (userCouncilFee.getIsJoinedService()) {
            isAppliedThisSemester = (startOfAppliedSemester <= userCouncilFee.getUser().getCurrentCompletedSemester()) &&
                    (userCouncilFee.getUser().getCurrentCompletedSemester() <= endOfAppliedSemester);
        } else {
            isAppliedThisSemester = (startOfAppliedSemester <= userCouncilFee.getCouncilFeeFakeUser().getCurrentCompletedSemester()) &&
                    (userCouncilFee.getCouncilFeeFakeUser().getCurrentCompletedSemester() <= endOfAppliedSemester);
        }
        return isAppliedThisSemester;
    }

    private Integer getRestOfSemester(UserCouncilFee userCouncilFee) {
        Integer startOfAppliedSemester = userCouncilFee.getPaidAt();
        Integer endOfAppliedSemester = ( userCouncilFee.getIsRefunded() ) ?
                ( startOfAppliedSemester - 1 ) + userCouncilFee.getNumOfPaidSemester() :
                userCouncilFee.getRefundedAt();
        Integer restOfSemester;

        if (userCouncilFee.getIsJoinedService()) {
            restOfSemester = Math.max(endOfAppliedSemester - userCouncilFee.getUser().getCurrentCompletedSemester(), 0);
        } else {
            restOfSemester = Math.max(endOfAppliedSemester - userCouncilFee.getCouncilFeeFakeUser().getCurrentCompletedSemester(), 0);
        }
        return restOfSemester;
    }

    private void validCanAccessPost(User writer, Form form) {
        if (form.getCircle() != null) {
            return;
        }

        Post post = getPost(form);

        Board board = post.getBoard();

        if (board.getCircle() != null) {
            Circle circle = board.getCircle();
            CircleMember circleMember = getCircleMember(writer.getId(), circle.getId());
            if (circleMember.getStatus().equals(CircleMemberStatus.MEMBER)) {
                throw new UnauthorizedException(
                        ErrorCode.API_NOT_ALLOWED,
                        MessageUtil.NOT_ALLOWED_TO_REPLY_FORM
                );
            }
        }
    }

    private Post getPost(Form form) {
        return postRepository.findByForm(form).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.FORM_NOT_FOUND
                )
        );
    }

    // Dto Mapper

    private ReplyPageResponseDto toReplyPageResponseDto(Form form, Page<Reply> replyPage) {
        return FormDtoMapper.INSTANCE.toReplyPageResponseDto(
                form.getFormQuestionList().stream()
                        .map(this::toQuestionResponseDto)
                        .toList(),

                replyPage.map(reply -> {
                    User replyUser = reply.getUser();

                    List<ReplyQuestionResponseDto> questionReplyList = reply.getReplyQuestionList()
                            .stream()
                            .map(this::toReplyQuestionResponseDto)
                            .toList();

                    return this.toReplyResponseDto(
                            replyUser,
                            questionReplyList,
                            reply.getCreatedAt()
                    );
                })
        );
    }

    private FormResponseDto toFormResponseDto(Form form) {
        return FormDtoMapper.INSTANCE.toFormResponseDto(
                form,
                form.getFormQuestionList().stream()
                        .map(this::toQuestionResponseDto)
                        .collect(Collectors.toList())
        );
    }

    private QuestionResponseDto toQuestionResponseDto(FormQuestion formQuestion) {
        return FormDtoMapper.INSTANCE.toQuestionResponseDto(
                formQuestion,
                formQuestion.getFormQuestionOptionList().stream()
                        .map(this::toOptionResponseDto)
                        .toList());
    }

    private OptionResponseDto toOptionResponseDto(FormQuestionOption formQuestionOption) {
        return FormDtoMapper.INSTANCE.toOptionResponseDto(formQuestionOption);
    }

    private ReplyQuestionResponseDto toReplyQuestionResponseDto(ReplyQuestion replyQuestion) {
        return FormDtoMapper.INSTANCE.toReplyQuestionResponseDto(replyQuestion);
    }

    private ReplyResponseDto toReplyResponseDto(User user, List<ReplyQuestionResponseDto> replyQuestionResponseDtoList, LocalDateTime createdAt) {
        return FormDtoMapper.INSTANCE.toReplyResponseDto(
                this.toReplyUserResponseDto(user),
                replyQuestionResponseDtoList,
                createdAt
        );
    }

    private ReplyUserResponseDto toReplyUserResponseDto(User user) {
        UserCouncilFee userCouncilFee = userCouncilFeeRepository.findByUser(user).orElse(null);

        if (userCouncilFee == null) {
            return FormDtoMapper.INSTANCE.toReplyUserResponseDto(user, null, null, null);
        }

        Boolean isAppliedThisSemester = getIsAppliedCurrentSemester(userCouncilFee);
        Integer restOfSemester = getRestOfSemester(userCouncilFee);

        return FormDtoMapper.INSTANCE.toReplyUserResponseDto(user, userCouncilFee, isAppliedThisSemester, restOfSemester);
    }




    private QuestionSummaryResponseDto toQuestionSummaryResponseDto(
            FormQuestion formQuestion,
            List<ReplyQuestion> replyQuestionList
    ) {
        if (formQuestion.getQuestionType().equals(QuestionType.SUBJECTIVE)) {
            List<String> answerList = replyQuestionList.stream()
                    .map(ReplyQuestion::getQuestionAnswer)
                    .toList();
            return FormDtoMapper.INSTANCE.toQuestionSummaryResponseDto(
                    formQuestion,
                    answerList,
                    null,
                    (long) answerList.size(),
                    findIsMultiple(formQuestion)
            );
        } else {
            List<OptionSummaryResponseDto> answerList = formQuestion.getFormQuestionOptionList().stream()
                    .map(formQuestionOption -> {
                        Long selectedCount = replyQuestionList
                                .stream()
                                .filter(replyQuestion -> replyQuestion.getSelectedOptionList().contains(formQuestionOption.getNumber()))
                                .count();
                        return toOptionSummaryResponseDto(formQuestionOption, selectedCount);
                    })
                    .toList();
            return FormDtoMapper.INSTANCE.toQuestionSummaryResponseDto(
                    formQuestion,
                    null,
                    answerList,
                    findNumOfReply(answerList),
                    findIsMultiple(formQuestion)
            );
        }

    }

    private Long findNumOfReply(List<OptionSummaryResponseDto> answerList) {
        Long numOfReply = 0L;
        for (OptionSummaryResponseDto optionSummaryResponseDto : answerList) {
            numOfReply += optionSummaryResponseDto.getSelectedCount();
        }
        return numOfReply;
    }

    private Boolean findIsMultiple(FormQuestion formQuestion) {
        return formQuestion.getIsMultiple();
    }

    private OptionSummaryResponseDto toOptionSummaryResponseDto(
            FormQuestionOption formQuestionOption,
            Long selectedCount
    ) {
        return FormDtoMapper.INSTANCE.toOptionSummaryResponseDto(formQuestionOption, selectedCount);
    }

    private ExcelReplyListResponseDto toExcelReplyListResponseDto(Form form, List<Reply> replyList) {
        return FormDtoMapper.INSTANCE.toExcelReplyListResponseDto(
                form.getFormQuestionList().stream()
                        .map(this::toQuestionResponseDto)
                        .toList(),

                replyList.stream()
                        .map(reply -> {
                            User replyUser = reply.getUser();

                            List<ExcelReplyQuestionResponseDto> excelReplyQuestionResponseDtoList = reply.getReplyQuestionList()
                                    .stream()
                                    .map(this::toExcelReplyQuestionResponseDto)
                                    .toList();

                            return this.toExcelReplyResponseDto(
                                    replyUser,
                                    excelReplyQuestionResponseDtoList,
                                    reply.getCreatedAt()
                            );
                        })
                        .toList()
        );
    }

    private ExcelReplyResponseDto toExcelReplyResponseDto(User replyUser, List<ExcelReplyQuestionResponseDto> excelReplyQuestionResponseDtoList, LocalDateTime createdAt) {
        return FormDtoMapper.INSTANCE.toExcelReplyResponseDto(
                this.toReplyUserResponseDto(replyUser),
                excelReplyQuestionResponseDtoList,
                createdAt
        );
    }

    private ExcelReplyQuestionResponseDto toExcelReplyQuestionResponseDto(ReplyQuestion replyQuestion) {
        return FormDtoMapper.INSTANCE.toExcelReplyQuestionResponseDto(replyQuestion);
    }

    public List<UserReplyResponseDto> getReplyByUserAndCircle(String userId, String circleId) {
        List<Form> circleFormList = this.formRepository.findAllByCircleAndIsDeletedAndIsClosed(this.getCircle(circleId), false, false);

        List<Reply> circleReplyList = circleFormList.stream()
                .flatMap(form -> replyRepository.findAllByForm(form).stream())
                .collect(Collectors.toList());

        List<Reply> userReplyList = circleReplyList.stream()
                .filter(reply -> reply.getUser().getId().equals(userId))
                .collect(Collectors.toList());

        if (userReplyList.isEmpty()) {
            throw new BadRequestException(
                    ErrorCode.ROW_DOES_NOT_EXIST,
                    MessageUtil.USER_APPLY_NOT_FOUND
            );
        }

        return userReplyList.stream()
                .map(FormDtoMapper.INSTANCE::toUserReplyResponseDto)
                .collect(Collectors.toList());
    }
}
