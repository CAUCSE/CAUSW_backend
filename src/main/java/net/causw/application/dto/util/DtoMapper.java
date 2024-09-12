package net.causw.application.dto.util;

import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.board.BoardApply;
import net.causw.adapter.persistence.calendar.Calendar;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.comment.ChildComment;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.event.Event;
import net.causw.adapter.persistence.form.Form;
import net.causw.adapter.persistence.form.Option;
import net.causw.adapter.persistence.form.Question;
import net.causw.adapter.persistence.form.Reply;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.board.*;
import net.causw.application.dto.calendar.CalendarResponseDto;
import net.causw.application.dto.calendar.CalendarsResponseDto;
import net.causw.application.dto.circle.CircleResponseDto;
import net.causw.application.dto.comment.ChildCommentResponseDto;
import net.causw.application.dto.comment.CommentResponseDto;
import net.causw.application.dto.event.EventResponseDto;
import net.causw.application.dto.event.EventsResponseDto;
import net.causw.application.dto.file.FileResponseDto;
import net.causw.application.dto.form.*;
import net.causw.application.dto.post.PostContentDto;
import net.causw.application.dto.user.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DtoMapper extends UuidFileToUrlDtoMapper {

    DtoMapper INSTANCE = Mappers.getMapper(DtoMapper.class);

    // 자료형 변환 등이 필요하다면 아래 형식으로 메서드를 작성합니다.
    // 이 메서드는 post.attachment를 attachmentsToStringList 메서드로 List<FileResponseDto>로 변환합니다.
    // 메서드 수가 많아지면 별도의 Converter 클래스를 만들어 상속받는 식으로 처리해도 좋습니다.
    @Named("attachmentsToStringList")
    default List<FileResponseDto> attachmentsToStringList(String attachments) {
        if(attachments == null || attachments.isEmpty()) return List.of();
        return Arrays.stream(attachments.split(":::"))
                .map(FileResponseDto::from)
                .collect(Collectors.toList());
    }

    @Mapping(target = "writerName", source = "comment.writer.name")
    @Mapping(target = "writerAdmissionYear", source = "comment.writer.admissionYear")
    @Mapping(target = "writerProfileImage", source = "comment.writer.profileImageUuidFile", qualifiedByName = "mapUuidFileToFileUrl")
    @Mapping(target = "postId", source = "comment.post.id")
    @Mapping(target = "isAnonymous", source = "comment.isAnonymous")
    @Mapping(target ="numLike", source = "numCommentLike")
    CommentResponseDto toCommentResponseDto(Comment comment, Long numChildComment, Long numCommentLike, List<ChildCommentResponseDto> childCommentList, Boolean updatable, Boolean deletable);

    @Mapping(target = "writerName", source = "childComment.writer.name")
    @Mapping(target = "writerAdmissionYear", source = "childComment.writer.admissionYear")
    @Mapping(target = "writerProfileImage", source = "childComment.writer.profileImageUuidFile", qualifiedByName = "mapUuidFileToFileUrl")
    @Mapping(target = "isAnonymous", source = "childComment.isAnonymous")
    @Mapping(target ="numLike", source = "numChildCommentLike")
    ChildCommentResponseDto toChildCommentResponseDto(ChildComment childComment, Long numChildCommentLike, Boolean updatable, Boolean deletable);


    /** TODO: 각자 역할분담한 부분의 Dto를 위를 참고하여 아래 작성하시면 됩니다.
     *  기존에 Dto에 존재하던 of 메서드를 DtoMapper.INSTANCE.toDtoName(entity)로 대체하시면 됩니다.
     *  컴파일 후 DtoMapperImpl 파일을 확인하여 필드별로 제대로 매핑이 되었는지 확인해야 합니다.
     */





    // Board
    BoardResponseDto toBoardResponseDto(Board entity, List<String> createRoleList, Boolean writable, String circleId, String circleName);

    @Mapping(target = "isPresent", source = "isPresent")
    BoardNameCheckResponseDto toBoardNameCheckResponseDto(Boolean isPresent);

    @Mapping(target = "id", source = "board.id")
    @Mapping(target = "name", source = "board.name")
    @Mapping(target = "writable", source = "writable")
    @Mapping(target = "isDeleted", source = "board.isDeleted")
    @Mapping(target = "postId", source = "post.id")
    @Mapping(target = "postTitle", source = "post.title")
    @Mapping(target = "postWriterName", source = "post.writer.name")
    @Mapping(target = "postWriterStudentId", source = "post.writer.studentId")
    @Mapping(target = "postCreatedAt", source = "post.createdAt")
    @Mapping(target = "postNumComment", source = "numComment")
    BoardOfCircleResponseDto toBoardOfCircleResponseDto(Board board, Post post, Long numComment, boolean writable);

    @Mapping(target = "boardId", source = "board.id")
    @Mapping(target = "boardName", source = "board.name")
    @Mapping(target = "isDefault", source = "board.isDefault")
    @Mapping(target = "contents", source = "postContentDtos")
    BoardMainResponseDto toBoardMainResponseDto(Board board, List<PostContentDto> postContentDtos);

    @Mapping(target = "writable", source = "writable")
    @Mapping(target = "postNumComment", source = "numComment")
    BoardOfCircleResponseDto toBoardOfCircleResponseDto(Board entity, Long numComment, boolean writable);

    @Mapping(target = "id", source = "boardApply.id")
    @Mapping(target = "boardName", source = "boardApply.boardName")
    @Mapping(target = "description", source = "boardApply.description")
    @Mapping(target = "createRoles", source = "boardApply.createRoles")
    @Mapping(target = "isAnonymousAllowed", source = "boardApply.isAnonymousAllowed")
    @Mapping(target = "user", source = "user")
    NormalBoardApplyResponseDto toNormalBoardApplyResponseDto(BoardApply boardApply, UserResponseDto user);

    @Mapping(target = "id", source = "boardApply.id")
    @Mapping(target = "boardName", source = "boardApply.boardName")
    NormalBoardAppliesResponseDto toNormalBoardAppliesResponseDto(BoardApply boardApply);

    // Circle
    @Mapping(target = "id", source = "circle.id")
    @Mapping(target = "name", source = "circle.name")
    @Mapping(target = "description", source = "circle.description")
    @Mapping(target = "isDeleted", source = "circle.isDeleted")
    @Mapping(target = "leaderId", source = "leader.id")
    @Mapping(target = "leaderName", source = "leader.name")
    @Mapping(target = "createdAt", source = "circle.createdAt")
    CircleResponseDto toCircleResponseDto(Circle circle, User leader);

    // Locker

    // Form
    FormResponseDto toFormResponseDto(Form form);

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
    QuestionSummaryResponseDto toQuestionSummaryResponseDto(Question question, List<String> questionAnswers,List<OptionSummaryResponseDto> optionSummaries) ;

    // Calendar
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "formatDateTime")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "formatDateTime")
    @Mapping(target = "image", source = "calendar.attachImageUuidFile", qualifiedByName = "mapUuidFileToFileUrl")
    CalendarResponseDto toCalendarResponseDto(Calendar calendar);

    @Mapping(target = "calendars", source = "calendars")
    CalendarsResponseDto toCalendarsResponseDto(Integer count, List<CalendarResponseDto> calendars);

    // Event
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "formatDateTime")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "formatDateTime")
    @Mapping(target = "image", source = "event.uuidFile", qualifiedByName = "mapUuidFileToFileUrl")
    EventResponseDto toEventResponseDto(Event event);

    @Named("formatDateTime")
    default String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd."));
    }

    @Mapping(target = "events", source = "events")
    EventsResponseDto toEventsResponseDto(Integer count, List<EventResponseDto> events);
}
