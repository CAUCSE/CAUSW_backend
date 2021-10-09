package net.causw.application

import net.causw.application.dto.CommentCreateRequestDto
import net.causw.application.dto.CommentResponseDto
import net.causw.application.spi.CircleMemberPort
import net.causw.application.spi.CommentPort
import net.causw.application.spi.PostPort
import net.causw.application.spi.UserPort
import net.causw.domain.exceptions.BadRequestException
import net.causw.domain.model.*
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate
import org.spockframework.runtime.Sputnik
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

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
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator()
    private CommentService commentService = new CommentService(
            this.commentPort,
            this.userPort,
            this.postPort,
            this.circleMemberPort,
            this.validator
    )

    def mockBoardDomainModel
    def mockPostWriterUserDomainModel
    def mockPostDomainModel
    def mockCommentWriterUserDomainModel
    def mockCommentDomainModel
    def mockParentCommentWriterUserDomainModel
    def mockParentCommentDomainModel

    def setup() {
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
                null
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

        this.mockParentCommentWriterUserDomainModel = UserDomainModel.of(
                "test parent comment writer user id",
                "test-parent-comment-writer@cau.ac.kr",
                "test parent comment writer user name",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        this.mockParentCommentDomainModel = CommentDomainModel.of(
                "test parent comment id",
                "test parent comment content",
                false,
                null,
                null,
                (UserDomainModel) this.mockParentCommentWriterUserDomainModel,
                ((PostDomainModel) this.mockPostDomainModel).getId()
        )
    }

    /**
     * Test case for comment create
     */
    def "Comment create normal case"() {
        def mockCommentCreateRequestDto = new CommentCreateRequestDto(
                ((CommentDomainModel) this.mockCommentDomainModel).getContent(),
                ((CommentDomainModel) this.mockCommentDomainModel).getPostId(),
                null
        )

        this.userPort.findById(((UserDomainModel) this.mockCommentWriterUserDomainModel).getId()) >> Optional.of((UserDomainModel) this.mockCommentWriterUserDomainModel)
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of((PostDomainModel) this.mockPostDomainModel)
        this.commentPort.findById(((CommentDomainModel) this.mockParentCommentDomainModel).getId()) >> Optional.of(((CommentDomainModel) this.mockParentCommentDomainModel))

        this.commentPort.create((CommentDomainModel) this.mockCommentDomainModel, (PostDomainModel) this.mockPostDomainModel) >> (CommentDomainModel) this.mockCommentDomainModel

        when: "Comment create without parent comment"
        PowerMockito.mockStatic(CommentDomainModel.class)
        PowerMockito.when(CommentDomainModel.of(
                mockCommentCreateRequestDto.getContent(),
                (UserDomainModel) this.mockCommentWriterUserDomainModel,
                ((PostDomainModel) this.mockPostDomainModel).getId(),
                null
        )).thenReturn((CommentDomainModel) this.mockCommentDomainModel)
        def commentCreate = this.commentService.create("test comment writer user id", mockCommentCreateRequestDto)

        then:
        commentCreate instanceof CommentResponseDto
        with(commentCreate) {
            getContent() == "test comment content"
            (!getIsDeleted())
        }

        when: "Comment create with parent comment"
        ((CommentDomainModel) this.mockCommentDomainModel).setParentComment(((CommentDomainModel) this.mockParentCommentDomainModel))
        mockCommentCreateRequestDto.setParentCommentId(((CommentDomainModel) this.mockParentCommentDomainModel).getId())
        PowerMockito.mockStatic(CommentDomainModel.class)
        PowerMockito.when(CommentDomainModel.of(
                mockCommentCreateRequestDto.getContent(),
                (UserDomainModel) this.mockCommentWriterUserDomainModel,
                ((PostDomainModel) this.mockPostDomainModel).getId(),
                ((CommentDomainModel) this.mockParentCommentDomainModel)
        )).thenReturn((CommentDomainModel) this.mockCommentDomainModel)
        commentCreate = this.commentService.create("test comment writer user id", mockCommentCreateRequestDto)

        then:
        commentCreate instanceof CommentResponseDto
        with(commentCreate) {
            getContent() == "test comment content"
            (!getIsDeleted())
            getParentCommentId() == "test parent comment id"
        }
    }

    def "Comment create target deleted case"() {
        def mockCommentCreateRequestDto = new CommentCreateRequestDto(
                ((CommentDomainModel) this.mockCommentDomainModel).getContent(),
                ((CommentDomainModel) this.mockCommentDomainModel).getPostId(),
                null
        )

        this.userPort.findById(((UserDomainModel) this.mockCommentWriterUserDomainModel).getId()) >> Optional.of((UserDomainModel) this.mockCommentWriterUserDomainModel)
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of((PostDomainModel) this.mockPostDomainModel)
        this.commentPort.findById(((CommentDomainModel) this.mockParentCommentDomainModel).getId()) >> Optional.of(((CommentDomainModel) this.mockParentCommentDomainModel))

        this.commentPort.create((CommentDomainModel) this.mockCommentDomainModel, (PostDomainModel) this.mockPostDomainModel) >> (CommentDomainModel) this.mockCommentDomainModel

        when: "Target post is deleted"
        ((PostDomainModel) this.mockPostDomainModel).setIsDeleted(true)
        PowerMockito.mockStatic(CommentDomainModel.class)
        PowerMockito.when(CommentDomainModel.of(
                mockCommentCreateRequestDto.getContent(),
                (UserDomainModel) this.mockCommentWriterUserDomainModel,
                ((PostDomainModel) this.mockPostDomainModel).getId(),
                null
        )).thenReturn((CommentDomainModel) this.mockCommentDomainModel)
        this.commentService.create("test comment writer user id", mockCommentCreateRequestDto)

        then:
        thrown(BadRequestException)
    }
}
