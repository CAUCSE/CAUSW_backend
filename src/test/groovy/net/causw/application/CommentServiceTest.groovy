package net.causw.application

import net.causw.application.dto.comment.CommentCreateRequestDto
import net.causw.application.dto.comment.CommentResponseDto
import net.causw.application.dto.comment.CommentUpdateRequestDto
import net.causw.application.spi.ChildCommentPort
import net.causw.application.spi.CircleMemberPort
import net.causw.application.spi.CommentPort
import net.causw.application.spi.PostPort
import net.causw.application.spi.UserPort
import net.causw.domain.exceptions.BadRequestException
import net.causw.domain.exceptions.UnauthorizedException
import net.causw.domain.model.*
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate
import org.spockframework.runtime.Sputnik
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import javax.validation.ConstraintViolationException
import javax.validation.Validation
import javax.validation.Validator

@ActiveProfiles(value = "test")
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Sputnik.class)
@PrepareForTest([CommentDomainModel.class])
class CommentServiceTest extends Specification {
    private CommentPort commentPort = Mock(CommentPort.class)
    private PostPort postPort = Mock(PostPort.class)
    private UserPort userPort = Mock(UserPort.class)
    private CircleMemberPort circleMemberPort = Mock(CircleMemberPort.class)
    private ChildCommentPort childCommentPort = Mock(ChildCommentPort.class)
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator()
    private CommentService commentService = new CommentService(
            this.commentPort,
            this.userPort,
            this.postPort,
            this.circleMemberPort,
            this.childCommentPort,
            this.validator
    )

    def mockUserDomainModel
    def mockBoardDomainModel
    def mockPostWriterUserDomainModel
    def mockPostDomainModel
    def mockCommentWriterUserDomainModel
    def mockCommentDomainModel

    // Circle
    def mockCircleLeaderUserDomainModel
    def mockCircleDomainModel
    def mockCircleMemberDomainModel

    def setup() {
        this.mockUserDomainModel = UserDomainModel.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.ADMIN,
                null,
                UserState.ACTIVE
        )

        this.mockBoardDomainModel = BoardDomainModel.of(
                "test board id",
                "test board id",
                "test board description",
                Arrays.asList("PRESIDENT"),
                "test category",
                false,
                null
        )

        this.mockPostWriterUserDomainModel = UserDomainModel.of(
                "test post writer user id",
                "test-post-writer@cau.ac.kr",
                "test post writer user name",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        this.mockPostDomainModel = PostDomainModel.of(
                "test post id",
                "test post title",
                "test post content",
                (UserDomainModel) this.mockPostWriterUserDomainModel,
                false,
                (BoardDomainModel) this.mockBoardDomainModel,
                null,
                null,
                List.of()
        )

        this.mockCommentWriterUserDomainModel = UserDomainModel.of(
                "test comment writer user id",
                "test-comment-writer@cau.ac.kr",
                "test comment writer user name",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        this.mockCommentDomainModel = CommentDomainModel.of(
                "test comment id",
                "test comment content",
                false,
                null,
                null,
                (UserDomainModel) this.mockCommentWriterUserDomainModel,
                ((PostDomainModel) this.mockPostDomainModel).getId()
        )

        this.mockCircleLeaderUserDomainModel = UserDomainModel.of(
                "test leader user id",
                "test-leader@cau.ac.kr",
                "test leader user name",
                "test1234!",
                "20210000",
                2021,
                Role.LEADER_CIRCLE,
                null,
                UserState.ACTIVE
        )

        this.mockCircleDomainModel = CircleDomainModel.of(
                "test circle id",
                "test circle name",
                null,
                "test circle description",
                false,
                (UserDomainModel) this.mockCircleLeaderUserDomainModel
        )

        this.mockCircleMemberDomainModel = CircleMemberDomainModel.of(
                "test",
                CircleMemberStatus.MEMBER,
                (CircleDomainModel) this.mockCircleDomainModel,
                "test comment writer user id",
                "test",
                null,
                null
        )
    }

    /**
     * Test case for comment create
     */
    @Test
    def "Comment create normal case"() {
        given:
        def mockCommentCreateRequestDto = new CommentCreateRequestDto(
                "test comment content",
                "test post id"
        )

        this.userPort.findById("test comment writer user id") >> Optional.of(this.mockCommentWriterUserDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        this.commentPort.create((CommentDomainModel) this.mockCommentDomainModel, (PostDomainModel) this.mockPostDomainModel) >> this.mockCommentDomainModel

        PowerMockito.mockStatic(CommentDomainModel.class)

        when: "without circle"
        PowerMockito.when(CommentDomainModel.of(
                mockCommentCreateRequestDto.getContent(),
                (UserDomainModel) this.mockCommentWriterUserDomainModel,
                "test post id"
        )).thenReturn((CommentDomainModel) this.mockCommentDomainModel)
        def commentResponseDto = this.commentService.create("test comment writer user id", mockCommentCreateRequestDto)

        then:
        commentResponseDto instanceof CommentResponseDto
        with(commentResponseDto) {
            getContent() == "test comment content"
            (!getIsDeleted())
        }

        when: "with circle"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        commentResponseDto = this.commentService.create("test comment writer user id", mockCommentCreateRequestDto)

        then:
        commentResponseDto instanceof CommentResponseDto
        with(commentResponseDto) {
            getContent() == "test comment content"
            (!getIsDeleted())
        }

        when: "with circle for admin"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockCommentWriterUserDomainModel.setRole(Role.ADMIN)
        PowerMockito.when(CommentDomainModel.of(
                mockCommentCreateRequestDto.getContent(),
                (UserDomainModel) this.mockCommentWriterUserDomainModel,
                "test post id"
        )).thenReturn((CommentDomainModel) this.mockCommentDomainModel)
        commentResponseDto = this.commentService.create("test comment writer user id", mockCommentCreateRequestDto)

        then:
        commentResponseDto instanceof CommentResponseDto
        with(commentResponseDto) {
            getContent() == "test comment content"
            (!getIsDeleted())
        }
    }

    @Test
    def "Comment create deleted case"() {
        given:
        def mockCommentCreateRequestDto = new CommentCreateRequestDto(
                "test comment content",
                "test post id"
        )

        this.userPort.findById("test comment writer user id") >> Optional.of(this.mockCommentWriterUserDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        when: "deleted post"
        this.mockPostDomainModel.setIsDeleted(true)
        this.commentService.create("test comment writer user id", mockCommentCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "deleted board"
        this.mockPostDomainModel.setIsDeleted(false)
        this.mockBoardDomainModel.setIsDeleted(true)
        this.commentService.create("test comment writer user id", mockCommentCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "deleted circle"
        this.mockBoardDomainModel.setIsDeleted(false)
        this.mockCircleDomainModel.setIsDeleted(true)
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.commentService.create("test comment writer user id", mockCommentCreateRequestDto)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Comment create unauthorized case"() {
        given:
        def mockCommentCreateRequestDto = new CommentCreateRequestDto(
                "test comment content",
                "test post id"
        )

        this.userPort.findById("test comment writer user id") >> Optional.of(this.mockCommentWriterUserDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        when: "circle member is await"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.circleMemberPort.findByUserIdAndCircleId("test comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.commentService.create("test comment writer user id", mockCommentCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "circle member is blocked"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.REJECT)
        this.commentService.create("test comment writer user id", mockCommentCreateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "Comment create invalid parameter"() {
        given:
        def mockCommentCreateRequestDto = new CommentCreateRequestDto(
                "",
                "test post id"
        )

        this.userPort.findById("test comment writer user id") >> Optional.of(this.mockCommentWriterUserDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        PowerMockito.mockStatic(CommentDomainModel.class)

        when:
        this.mockCommentDomainModel.setContent("")
        PowerMockito.when(CommentDomainModel.of(
                mockCommentCreateRequestDto.getContent(),
                (UserDomainModel) this.mockCommentWriterUserDomainModel,
                "test post id"
        )).thenReturn((CommentDomainModel) this.mockCommentDomainModel)
        this.commentService.create("test comment writer user id", mockCommentCreateRequestDto)

        then:
        thrown(ConstraintViolationException)
    }

    /**
     * Test case for comment find all
     */
    @Test
    def "Comment find all normal case"() {
        given: "Create multiple comments"

        this.userPort.findById("test comment writer user id") >> Optional.of(this.mockCommentWriterUserDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)
        this.commentPort.findByPostId("test post id", 0) >> new PageImpl<CommentDomainModel>(List.of((CommentDomainModel)this.mockCommentDomainModel))

        when: "without circle"
        def commentResponseDtoPage = this.commentService.findAll("test comment writer user id", "test post id", 0)

        then:
        commentResponseDtoPage instanceof Page<CommentResponseDto>
        with(commentResponseDtoPage) {
            getContent().get(0).getContent() == "test comment content"
        }

        when: "with circle"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        commentResponseDtoPage = this.commentService.findAll("test comment writer user id", "test post id", 0)

        then:
        commentResponseDtoPage instanceof Page<CommentResponseDto>
        with(commentResponseDtoPage) {
            getContent().get(0).getContent() == "test comment content"
        }

        when: "with circle for admin"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockCommentWriterUserDomainModel.setRole(Role.ADMIN)
        commentResponseDtoPage = this.commentService.findAll("test comment writer user id", "test post id", 0)

        then:
        commentResponseDtoPage instanceof Page<CommentResponseDto>
        with(commentResponseDtoPage) {
            getContent().get(0).getContent() == "test comment content"
        }
    }

    @Test
    def "Comment find all deleted case"() {
        given:
        this.userPort.findById("test comment writer user id") >> Optional.of(this.mockCommentWriterUserDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        when: "deleted post"
        this.mockPostDomainModel.setIsDeleted(true)
        this.commentService.findAll("test comment writer user id", "test post id", 0)

        then:
        thrown(BadRequestException)

        when: "deleted board"
        this.mockPostDomainModel.setIsDeleted(false)
        this.mockBoardDomainModel.setIsDeleted(true)
        this.commentService.findAll("test comment writer user id", "test post id", 0)

        then:
        thrown(BadRequestException)

        when: "deleted circle"
        this.mockBoardDomainModel.setIsDeleted(false)
        this.mockCircleDomainModel.setIsDeleted(true)
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.commentService.findAll("test comment writer user id", "test post id", 0)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Comment find all unauthorized case"() {
        given:
        this.userPort.findById("test comment writer user id") >> Optional.of(this.mockCommentWriterUserDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        when: "circle member is await"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.circleMemberPort.findByUserIdAndCircleId("test comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.commentService.findAll("test comment writer user id", "test post id", 0)

        then:
        thrown(BadRequestException)

        when: "circle member is blocked"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.REJECT)
        this.commentService.findAll("test comment writer user id", "test post id", 0)

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test case for comment update
     */
    @Test
    def "Comment update normal case"() {
        given:
        def mockUpdatedCommentDomainModel = CommentDomainModel.of(
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                "Update comment content",
                ((CommentDomainModel) this.mockCommentDomainModel).getIsDeleted(),
                ((CommentDomainModel) this.mockCommentDomainModel).getCreatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getUpdatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getWriter(),
                ((CommentDomainModel) this.mockCommentDomainModel).getPostId()
        )

        def commentUpdateRequestDto = new CommentUpdateRequestDto("Update comment content")

        this.userPort.findById("test comment writer user id") >> Optional.of((UserDomainModel) this.mockCommentWriterUserDomainModel)
        this.commentPort.findById("test comment id") >> Optional.of(this.mockCommentDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        this.commentPort.update("test comment id", (CommentDomainModel)this.mockCommentDomainModel) >> Optional.of(mockUpdatedCommentDomainModel)

        when: "without circle"
        def commentResponseDto = this.commentService.update("test comment writer user id", "test comment id", commentUpdateRequestDto)

        then:
        commentResponseDto instanceof CommentResponseDto
        with(commentResponseDto) {
            getContent() == "Update comment content"
        }

        when: "without circle for admin"
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        commentResponseDto = this.commentService.update("test", "test comment id", commentUpdateRequestDto)

        then:
        commentResponseDto instanceof CommentResponseDto
        with(commentResponseDto) {
            getContent() == "Update comment content"
        }

        when: "with circle"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        commentResponseDto = this.commentService.update("test comment writer user id", "test comment id", commentUpdateRequestDto)

        then:
        commentResponseDto instanceof CommentResponseDto
        with(commentResponseDto) {
            getContent() == "Update comment content"
        }

        when: "with circle for admin"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        commentResponseDto = this.commentService.update("test", "test comment id", commentUpdateRequestDto)

        then:
        commentResponseDto instanceof CommentResponseDto
        with(commentResponseDto) {
            getContent() == "Update comment content"
        }
    }

    @Test
    def "Comment update deleted case"() {
        given:
        def commentUpdateRequestDto = new CommentUpdateRequestDto("Update comment content")

        this.userPort.findById("test comment writer user id") >> Optional.of(this.mockCommentWriterUserDomainModel)
        this.commentPort.findById("test comment id") >> Optional.of(this.mockCommentDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        when: "deleted comment"
        this.mockCommentDomainModel.setIsDeleted(true)
        this.commentService.update("test comment writer user id", "test comment id", commentUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "deleted post"
        this.mockCommentDomainModel.setIsDeleted(false)
        this.mockPostDomainModel.setIsDeleted(true)
        this.commentService.update("test comment writer user id", "test comment id", commentUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "deleted board"
        this.mockPostDomainModel.setIsDeleted(false)
        this.mockBoardDomainModel.setIsDeleted(true)
        this.commentService.update("test comment writer user id", "test comment id", commentUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "deleted circle"
        this.mockBoardDomainModel.setIsDeleted(false)
        this.mockCircleDomainModel.setIsDeleted(true)
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.commentService.update("test comment writer user id", "test comment id", commentUpdateRequestDto)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Comment update unauthorized case"() {
        given:
        def commentUpdateRequestDto = new CommentUpdateRequestDto("Update comment content")

        this.userPort.findById("test comment writer user id") >> Optional.of(this.mockCommentWriterUserDomainModel)
        this.commentPort.findById("test comment id") >> Optional.of(this.mockCommentDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        when: "not writer"
        this.mockUserDomainModel.setRole(Role.PRESIDENT)
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.commentService.update("test", "test comment id", commentUpdateRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "circle member is await"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.circleMemberPort.findByUserIdAndCircleId("test comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.commentService.update("test comment writer user id", "test comment id", commentUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "circle member is blocked"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.REJECT)
        this.commentService.update("test comment writer user id", "test comment id", commentUpdateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "Comment update invalid parameter"() {
        given:
        def commentUpdateRequestDto = new CommentUpdateRequestDto("")

        this.userPort.findById("test comment writer user id") >> Optional.of(this.mockCommentWriterUserDomainModel)
        this.commentPort.findById("test comment id") >> Optional.of(this.mockCommentDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        PowerMockito.mockStatic(CommentDomainModel.class)

        when:
        this.commentService.update("test comment writer user id", "test comment id", commentUpdateRequestDto)

        then:
        thrown(ConstraintViolationException)
    }

    /**
     * Test case for comment delete
     */
    @Test
    def "Comment delete normal case"() {
        given:
        def mockDeletedCommentDomainModel = CommentDomainModel.of(
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getContent(),
                true,
                ((CommentDomainModel) this.mockCommentDomainModel).getCreatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getUpdatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getWriter(),
                ((CommentDomainModel) this.mockCommentDomainModel).getPostId()
        )

        this.userPort.findById("test comment writer user id") >> Optional.of(this.mockCommentWriterUserDomainModel)
        this.commentPort.findById("test comment id") >> Optional.of(this.mockCommentDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        this.commentPort.delete("test comment id") >> Optional.of(mockDeletedCommentDomainModel)

        when: "without circle"
        def commentResponseDto = this.commentService.delete("test comment writer user id", "test comment id")

        then:
        commentResponseDto instanceof CommentResponseDto
        with(commentResponseDto) {
            getIsDeleted()
        }

        when: "without circle for president"
        this.mockUserDomainModel.setRole(Role.PRESIDENT)
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        commentResponseDto = this.commentService.delete("test", "test comment id")

        then:
        commentResponseDto instanceof CommentResponseDto
        with(commentResponseDto) {
            getIsDeleted()
        }

        when: "with circle"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        commentResponseDto = this.commentService.delete("test comment writer user id", "test comment id")

        then:
        commentResponseDto instanceof CommentResponseDto
        with(commentResponseDto) {
            getIsDeleted()
        }

        when: "with circle for leader circle"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.userPort.findById("test leader user id") >> Optional.of(this.mockCircleLeaderUserDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test leader user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        commentResponseDto = this.commentService.delete("test leader user id", "test comment id")

        then:
        commentResponseDto instanceof CommentResponseDto
        with(commentResponseDto) {
            getIsDeleted()
        }

        when: "with circle for admin"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockUserDomainModel.setRole(Role.ADMIN)
        commentResponseDto = this.commentService.delete("test", "test comment id")

        then:
        commentResponseDto instanceof CommentResponseDto
        with(commentResponseDto) {
            getIsDeleted()
        }
    }

    @Test
    def "Comment delete deleted case"() {
        given:
        this.userPort.findById("test comment writer user id") >> Optional.of(this.mockCommentWriterUserDomainModel)
        this.commentPort.findById("test comment id") >> Optional.of(this.mockCommentDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        when: "deleted comment"
        this.mockCommentDomainModel.setIsDeleted(true)
        this.commentService.delete("test comment writer user id", "test comment id")

        then:
        thrown(BadRequestException)

        when: "deleted circle"
        this.mockCommentDomainModel.setIsDeleted(false)
        this.mockCircleDomainModel.setIsDeleted(true)
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.commentService.delete("test comment writer user id", "test comment id")

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Comment delete unauthorized case"() {
        given:
        this.userPort.findById("test comment writer user id") >> Optional.of(this.mockCommentWriterUserDomainModel)
        this.commentPort.findById("test comment id") >> Optional.of(this.mockCommentDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        when: "not writer"
        this.mockUserDomainModel.setRole(Role.COMMON)
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.commentService.delete("test", "test comment id")

        then:
        thrown(UnauthorizedException)

        when: "not writer with circle"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.commentService.delete("test", "test comment id")

        then:
        thrown(UnauthorizedException)

        when: "not leader with circle"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockUserDomainModel.setRole(Role.LEADER_CIRCLE)
        this.commentService.delete("test", "test comment id")

        then:
        thrown(UnauthorizedException)

        when: "circle member is await"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.circleMemberPort.findByUserIdAndCircleId("test comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.commentService.delete("test comment writer user id", "test comment id")

        then:
        thrown(BadRequestException)

        when: "circle member is blocked"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.REJECT)
        this.commentService.delete("test comment writer user id", "test comment id")

        then:
        thrown(UnauthorizedException)
    }
}
