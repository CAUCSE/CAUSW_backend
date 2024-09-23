package net.causw.application.form;

import jakarta.servlet.http.HttpServletResponse;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.form.Option;
import net.causw.adapter.persistence.form.Reply;
import net.causw.adapter.persistence.repository.board.BoardRepository;
import net.causw.adapter.persistence.repository.circle.CircleMemberRepository;
import net.causw.adapter.persistence.repository.circle.CircleRepository;
import net.causw.adapter.persistence.repository.form.FormRepository;
import net.causw.adapter.persistence.repository.form.OptionRepository;
import net.causw.adapter.persistence.repository.form.QuestionRepository;
import net.causw.adapter.persistence.repository.form.ReplyRepository;
import net.causw.adapter.persistence.repository.user.UserRepository;
import net.causw.application.dto.form.*;
import net.causw.application.dto.util.dtoMapper.FormDtoMapper;
import net.causw.domain.aop.annotation.MeasureTime;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.circle.CircleMemberStatus;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.RedisUtils;
import net.causw.domain.validation.CircleMemberStatusValidator;
import net.causw.domain.validation.UserEqualValidator;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.ValidatorBucket;
import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.form.Form;
import net.causw.adapter.persistence.form.Question;
import net.causw.adapter.persistence.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ReplyRepository replyRepository;
    private final UserRepository userRepository;
    private final CircleMemberRepository circleMemberRepository;

    @Transactional
    public void replyForm(String formId, FormReplyRequestDto formReplyRequestDto, User writer){
        Board board = getBoard(formReplyRequestDto.getBoardId());
        List<String> createRoles = new ArrayList<>(Arrays.asList(board.getCreateRoles().split(",")));

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket.consistOf(UserRoleValidator.of(
                writer.getRoles(),
                createRoles.stream()
                        .map(Role::of)
                        .collect(Collectors.toSet())
        ));

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


    //1. 각 유저별 결과를 반환(개별) -> form
    //내가 유저 별로 나눠주고 알아서 분리해서 쓰냐 or 매번 누구의 결과를 원하는지 저쪽에서 보내냐
    //근데 보통은 formId로 조회하니까 그 form의 모든 것을 조회하는게 좋을 거 같은데
    //그냥 내가 유저별로 응답을 묶어서 보내면 프론트에서 페이지별로 한명씩 보여주기
    public List<ReplyUserResponseDto> findUserReply(String formId, User user){
        List<Reply> replies = replyRepository.findAllByFormId(formId);

        Map<String, List<Reply>> repliesByUser = replies.stream()
                .collect(Collectors.groupingBy(reply -> reply.getUser().getId()));

        // ReplyUserResponseDto 리스트를 생성합니다.
        return repliesByUser.entrySet().stream()
                .map(reply -> {
                    User replyUser = reply.getValue().get(0).getUser();
                    List<QuestionReplyResponseDto> questionReplies = reply.getValue().stream()
                            .map(FormDtoMapper.INSTANCE::toQuestionReplyResponseDto)
                            .collect(Collectors.toList());

                    return FormDtoMapper.INSTANCE.toReplyUserResponseDto(replyUser, questionReplies);
                })
                .collect(Collectors.toList());
    }


    //2. 각 질문별 결과를 반환(요약)
    //이거는 그냥 form의 질문별로 결과를 보여주면 될듯
    public List<QuestionSummaryResponseDto> findSummaryReply(String formId, User user){
        List<Reply> replies = replyRepository.findAllByFormId(formId);

        Map<Question, List<Reply>> repliesByQuestion = replies.stream()
                .collect(Collectors.groupingBy(Reply::getQuestion));

        return repliesByQuestion.entrySet().stream()
                .map(entry -> {
                    Question question = entry.getKey();
                    List<Reply> questionReplies = entry.getValue();


                    Map<Integer, Long> optionCount = new HashMap<>();
                    List<String> questionAnswers = new ArrayList<>();


                    for (Reply reply : questionReplies) {
                        //주관식 정답 저장
                        if(reply.getQuestionAnswer() != null){
                            questionAnswers.add(reply.getQuestionAnswer());
                        }
                        //객관식 정답 저장
                        for (Integer optionNumber : reply.getSelectedOptions()) {
                            optionCount.put(optionNumber, optionCount.getOrDefault(optionNumber, 0L) + 1);
                        }
                    }

                    List<OptionSummaryResponseDto> optionSummaries = question.getOptions().stream()
                            .map(option -> FormDtoMapper.INSTANCE.toOptionSummaryResponseDto(option, optionCount.getOrDefault(option.getNumber(), 0L)))
                            .collect(Collectors.toList());


                    return FormDtoMapper.INSTANCE.toQuestionSummaryResponseDto(question, questionAnswers, optionSummaries);
                })
                .collect(Collectors.toList());
    }

    public void exportFormResult(String formId, User user, HttpServletResponse response) {
        Form form = getForm(formId);

    }

    // private methods
    private User getUser(String userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
        );
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
}
