package net.causw.application

import net.causw.application.dto.comment.ChildCommentsResponseDto
import net.causw.application.dto.comment.ChildCommentCreateRequestDto
import net.causw.application.dto.comment.ChildCommentResponseDto
import net.causw.application.dto.comment.ChildCommentUpdateRequestDto
import net.causw.application.spi.ChildCommentPort
import net.causw.application.spi.CircleMemberPort
import net.causw.application.spi.CommentPort
import net.causw.application.spi.PostPort
import net.causw.application.spi.UserPort
import net.causw.domain.exceptions.BadRequestException
import net.causw.domain.exceptions.UnauthorizedException
import net.causw.domain.model.BoardDomainModel
import net.causw.domain.model.ChildCommentDomainModel
import net.causw.domain.model.CommentDomainModel
import net.causw.domain.model.PostDomainModel
import net.causw.domain.model.Role
import net.causw.domain.model.UserDomainModel
import net.causw.domain.model.UserState
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate
import org.spockframework.runtime.Sputnik
import org.springframework.data.domain.PageImpl
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import javax.validation.ConstraintViolationException
import javax.validation.Validation
import javax.validation.Validator

@ActiveProfiles(value = "test")
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Sputnik.class)
@PrepareForTest([ChildCommentDomainModel.class])
class ChildCommentServiceTest extends Specification {
    private ChildCommentPort childCommentPort = Mock(ChildCommentPort.class)
    private CommentPort commentPort = Mock(CommentPort.class)
    private UserPort userPort = Mock(UserPort.class)
    private CircleMemberPort circleMemberPort = Mock(CircleMemberPort.class)
    private PostPort postPort = Mock(PostPort.class)
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator()
    private ChildCommentService childCommentService = new ChildCommentService(
            this.childCommentPort,
            this.commentPort,
            this.userPort,
            this.circleMemberPort,
            this.postPort,
            this.validator
    )

    def mockBoardDomainModel
    def mockPostWriter
    def mockPostDomainModel
    def mockCommentWriter
    def mockCommentDomainModel
    def mockChildCommentWriter
    def mockChildCommentDomainModel
    def mockRefChildCommentDomainModel

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

        this.mockPostWriter = UserDomainModel.of(
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
                (UserDomainModel) this.mockPostWriter,
                false,
                (BoardDomainModel) this.mockBoardDomainModel,
                null,
                null
        )

        this.mockCommentWriter = UserDomainModel.of(
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

        this.mockChildCommentWriter = UserDomainModel.of(
                "test child comment writer user id",
                "test-child-comment-writer@cau.ac.kr",
                "test child comment writer user name",
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
                (UserDomainModel) this.mockCommentWriter,
                ((PostDomainModel) this.mockPostDomainModel).getId()
        )

        this.mockChildCommentDomainModel = ChildCommentDomainModel.of(
                "test child comment id",
                "test child comment content",
                false,
                "test child comment writer user name",
                "test ref child comment id",
                (UserDomainModel) this.mockChildCommentWriter,
                (CommentDomainModel) this.mockCommentDomainModel,
                null,
                null
        )

        this.mockRefChildCommentDomainModel = ChildCommentDomainModel.of(
                "test ref child comment id",
                "test child comment content",
                false,
                null,
                null,
                (UserDomainModel) this.mockChildCommentWriter,
                (CommentDomainModel) this.mockCommentDomainModel,
                null,
                null
        )
    }

    /**
     * Test case for child comment create
     */
    def "Child Comment create normal case"() {
        def childCommentCreateRequestDto = new ChildCommentCreateRequestDto(
                "test child comment content",
                ((CommentDomainModel)this.mockCommentDomainModel).getId(),
                "test ref child comment id"
        )

        this.userPort.findById(((UserDomainModel)this.mockChildCommentWriter).getId()) >> Optional.of(this.mockChildCommentWriter)
        this.commentPort.findById(((CommentDomainModel)this.mockCommentDomainModel).getId()) >> Optional.of(this.mockCommentDomainModel)
        this.childCommentPort.findById(((ChildCommentDomainModel)this.mockRefChildCommentDomainModel).getId()) >> Optional.of(this.mockRefChildCommentDomainModel)
        this.postPort.findById(((PostDomainModel)this.mockPostDomainModel).getId()) >> Optional.of(this.mockPostDomainModel)

        PowerMockito.mockStatic(ChildCommentDomainModel.class)
        PowerMockito.when(ChildCommentDomainModel.of(
                childCommentCreateRequestDto.getContent(),
                ((ChildCommentDomainModel) this.mockRefChildCommentDomainModel).getWriter().getName(),
                childCommentCreateRequestDto.getRefChildComment().orElse(null),
                (UserDomainModel) this.mockChildCommentWriter,
                (CommentDomainModel) this.mockCommentDomainModel
        )).thenReturn((ChildCommentDomainModel) this.mockChildCommentDomainModel)

        this.childCommentPort.create((ChildCommentDomainModel)this.mockChildCommentDomainModel, (PostDomainModel)this.mockPostDomainModel) >> this.mockChildCommentDomainModel

        when:
        def childCommentResponse = this.childCommentService.create(((UserDomainModel)this.mockChildCommentWriter).getId(), childCommentCreateRequestDto)

        then:
        childCommentResponse instanceof ChildCommentResponseDto
        with(childCommentResponse) {
            getContent() == "test child comment content"
        }
    }

    def "Child Comment create unauthorized case"() {
        def childCommentCreateRequestDto = new ChildCommentCreateRequestDto(
                "test child comment content",
                ((CommentDomainModel)this.mockCommentDomainModel).getId(),
                "test ref child comment id"
        )

        this.userPort.findById(((UserDomainModel)this.mockChildCommentWriter).getId()) >> Optional.of(this.mockChildCommentWriter)
        this.commentPort.findById(((CommentDomainModel)this.mockCommentDomainModel).getId()) >> Optional.of(this.mockCommentDomainModel)
        this.childCommentPort.findById(((ChildCommentDomainModel)this.mockRefChildCommentDomainModel).getId()) >> Optional.of(this.mockRefChildCommentDomainModel)
        this.postPort.findById(((PostDomainModel)this.mockPostDomainModel).getId()) >> Optional.of(this.mockPostDomainModel)

        PowerMockito.mockStatic(ChildCommentDomainModel.class)
        PowerMockito.when(ChildCommentDomainModel.of(
                childCommentCreateRequestDto.getContent(),
                ((ChildCommentDomainModel) this.mockRefChildCommentDomainModel).getWriter().getName(),
                childCommentCreateRequestDto.getRefChildComment().orElse(null),
                (UserDomainModel) this.mockChildCommentWriter,
                (CommentDomainModel) this.mockCommentDomainModel
        )).thenReturn((ChildCommentDomainModel) this.mockChildCommentDomainModel)

        when: "Creator' role is NONE"
        ((UserDomainModel)this.mockChildCommentWriter).setRole(Role.NONE)
        this.childCommentService.create(((UserDomainModel)this.mockChildCommentWriter).getId(), childCommentCreateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    def "Child Comment create bad request case"() {
        def childCommentCreateRequestDto = new ChildCommentCreateRequestDto(
                "test child comment content",
                ((CommentDomainModel)this.mockCommentDomainModel).getId(),
                "test ref child comment id"
        )

        this.userPort.findById(((UserDomainModel)this.mockChildCommentWriter).getId()) >> Optional.of(this.mockChildCommentWriter)
        this.commentPort.findById(((CommentDomainModel)this.mockCommentDomainModel).getId()) >> Optional.of(this.mockCommentDomainModel)
        this.childCommentPort.findById(((ChildCommentDomainModel)this.mockRefChildCommentDomainModel).getId()) >> Optional.of(this.mockRefChildCommentDomainModel)
        this.postPort.findById(((PostDomainModel)this.mockPostDomainModel).getId()) >> Optional.of(this.mockPostDomainModel)

        PowerMockito.mockStatic(ChildCommentDomainModel.class)
        PowerMockito.when(ChildCommentDomainModel.of(
                childCommentCreateRequestDto.getContent(),
                ((ChildCommentDomainModel) this.mockRefChildCommentDomainModel).getWriter().getName(),
                childCommentCreateRequestDto.getRefChildComment().orElse(null),
                (UserDomainModel) this.mockChildCommentWriter,
                (CommentDomainModel) this.mockCommentDomainModel
        )).thenReturn((ChildCommentDomainModel) this.mockChildCommentDomainModel)

        when: "Board is deleted"
        ((BoardDomainModel)this.mockBoardDomainModel).setIsDeleted(true)
        this.childCommentService.create(((UserDomainModel)this.mockChildCommentWriter).getId(), childCommentCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "Post is deleted"
        ((BoardDomainModel)this.mockBoardDomainModel).setIsDeleted(false)
        ((PostDomainModel)this.mockPostDomainModel).setIsDeleted(true)
        this.childCommentService.create(((UserDomainModel)this.mockChildCommentWriter).getId(), childCommentCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "Child comment's content is blank"
        ((PostDomainModel)this.mockPostDomainModel).setIsDeleted(false)
        childCommentCreateRequestDto.setContent("")
        ((ChildCommentDomainModel)this.mockChildCommentDomainModel).setContent("")
        PowerMockito.mockStatic(ChildCommentDomainModel.class)
        PowerMockito.when(ChildCommentDomainModel.of(
                childCommentCreateRequestDto.getContent(),
                ((ChildCommentDomainModel) this.mockRefChildCommentDomainModel).getWriter().getName(),
                childCommentCreateRequestDto.getRefChildComment().orElse(null),
                (UserDomainModel) this.mockChildCommentWriter,
                (CommentDomainModel) this.mockCommentDomainModel
        )).thenReturn((ChildCommentDomainModel) this.mockChildCommentDomainModel)
        this.childCommentService.create(((UserDomainModel)this.mockChildCommentWriter).getId(), childCommentCreateRequestDto)

        then:
        thrown(ConstraintViolationException)

        when: "Ref Child comment is deleted"
        childCommentCreateRequestDto.setContent("test child comment content")
        ((ChildCommentDomainModel)this.mockChildCommentDomainModel).setContent("test child comment content")
        ((ChildCommentDomainModel)this.mockRefChildCommentDomainModel).setIsDeleted(true)
        PowerMockito.mockStatic(ChildCommentDomainModel.class)
        PowerMockito.when(ChildCommentDomainModel.of(
                childCommentCreateRequestDto.getContent(),
                ((ChildCommentDomainModel) this.mockRefChildCommentDomainModel).getWriter().getName(),
                childCommentCreateRequestDto.getRefChildComment().orElse(null),
                (UserDomainModel) this.mockChildCommentWriter,
                (CommentDomainModel) this.mockCommentDomainModel
        )).thenReturn((ChildCommentDomainModel) this.mockChildCommentDomainModel)
        this.childCommentService.create(((UserDomainModel)this.mockChildCommentWriter).getId(), childCommentCreateRequestDto)

        then:
        thrown(BadRequestException)
    }

    /**
     * Test case for child update create
     */
    def "Child Comment update normal case"() {
        def childCommentUpdateRequestDto = new ChildCommentUpdateRequestDto(
                "test child comment content"
        )

        this.userPort.findById(((UserDomainModel)this.mockChildCommentWriter).getId()) >> Optional.of(this.mockChildCommentWriter)
        this.postPort.findById(((PostDomainModel)this.mockPostDomainModel).getId()) >> Optional.of(this.mockPostDomainModel)
        this.childCommentPort.findById(((ChildCommentDomainModel)this.mockChildCommentDomainModel).getId()) >> Optional.of(this.mockChildCommentDomainModel)

        this.childCommentPort.update(((ChildCommentDomainModel)this.mockChildCommentDomainModel).getId(), (ChildCommentDomainModel)this.mockChildCommentDomainModel) >> Optional.of(this.mockChildCommentDomainModel)

        when:
        def childCommentResponse = this.childCommentService.update(((UserDomainModel)this.mockChildCommentWriter).getId(), ((ChildCommentDomainModel)this.mockChildCommentDomainModel).getId(), childCommentUpdateRequestDto)

        then:
        childCommentResponse instanceof ChildCommentResponseDto
        with(childCommentResponse) {
            getContent() == "test child comment content"
        }
    }

    def "Child Comment update unauthorized case"() {
        def childCommentUpdateRequestDto = new ChildCommentUpdateRequestDto(
                "test child comment content"
        )

        this.userPort.findById(((UserDomainModel)this.mockChildCommentWriter).getId()) >> Optional.of(this.mockChildCommentWriter)
        this.postPort.findById(((PostDomainModel)this.mockPostDomainModel).getId()) >> Optional.of(this.mockPostDomainModel)
        this.childCommentPort.findById(((ChildCommentDomainModel)this.mockChildCommentDomainModel).getId()) >> Optional.of(this.mockChildCommentDomainModel)

        when: "Updater's Role is NONE"
        ((UserDomainModel)this.mockChildCommentWriter).setRole(Role.NONE)
        this.childCommentService.update(((UserDomainModel)this.mockChildCommentWriter).getId(), ((ChildCommentDomainModel)this.mockChildCommentDomainModel).getId(), childCommentUpdateRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "This user is not contents admin"
        ((UserDomainModel)this.mockChildCommentWriter).setRole(Role.COMMON)
        this.userPort.findById(((UserDomainModel)this.mockCommentWriter).getId()) >> Optional.of(this.mockCommentWriter)
        this.childCommentService.update(((UserDomainModel)this.mockCommentWriter).getId(), ((ChildCommentDomainModel)this.mockChildCommentDomainModel).getId(), childCommentUpdateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    def "Child Comment update bad request case"() {
        def childCommentUpdateRequestDto = new ChildCommentUpdateRequestDto(
                "test child comment content"
        )

        this.userPort.findById(((UserDomainModel)this.mockChildCommentWriter).getId()) >> Optional.of(this.mockChildCommentWriter)
        this.postPort.findById(((PostDomainModel)this.mockPostDomainModel).getId()) >> Optional.of(this.mockPostDomainModel)
        this.childCommentPort.findById(((ChildCommentDomainModel)this.mockChildCommentDomainModel).getId()) >> Optional.of(this.mockChildCommentDomainModel)

        when: "Board is deleted"
        ((BoardDomainModel)this.mockBoardDomainModel).setIsDeleted(true)
        this.childCommentService.update(((UserDomainModel)this.mockChildCommentWriter).getId(), ((ChildCommentDomainModel)this.mockChildCommentDomainModel).getId(), childCommentUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "Post is deleted"
        ((BoardDomainModel)this.mockBoardDomainModel).setIsDeleted(false)
        ((PostDomainModel)this.mockPostDomainModel).setIsDeleted(true)
        this.childCommentService.update(((UserDomainModel)this.mockChildCommentWriter).getId(), ((ChildCommentDomainModel)this.mockChildCommentDomainModel).getId(), childCommentUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "Child comment is deleted"
        ((ChildCommentDomainModel)this.mockRefChildCommentDomainModel).setIsDeleted(false)
        ((ChildCommentDomainModel)this.mockChildCommentDomainModel).setIsDeleted(true)
        this.childCommentService.update(((UserDomainModel)this.mockChildCommentWriter).getId(), ((ChildCommentDomainModel)this.mockChildCommentDomainModel).getId(), childCommentUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "Ref Child comment' writer is not same tag name"
        ((ChildCommentDomainModel)this.mockChildCommentDomainModel).setIsDeleted(false)
        ((ChildCommentDomainModel)this.mockRefChildCommentDomainModel).getWriter().setName("wrong")
        this.childCommentService.update(((UserDomainModel)this.mockChildCommentWriter).getId(), ((ChildCommentDomainModel)this.mockChildCommentDomainModel).getId(), childCommentUpdateRequestDto)

        then:
        thrown(BadRequestException)
    }

    /**
     * Test case for child comment delete
     */
    def "Child Comment delete normal case"() {
        def deletedChildCommentDomainModel = ChildCommentDomainModel.of(
                "test child comment id",
                "test child comment content",
                true,
                "test child comment writer user name",
                "test ref child comment id",
                (UserDomainModel) this.mockChildCommentWriter,
                (CommentDomainModel) this.mockCommentDomainModel,
                null,
                null
        )

        this.userPort.findById(((UserDomainModel)this.mockChildCommentWriter).getId()) >> Optional.of(this.mockChildCommentWriter)
        this.postPort.findById(((PostDomainModel)this.mockPostDomainModel).getId()) >> Optional.of(this.mockPostDomainModel)
        this.childCommentPort.findById(((ChildCommentDomainModel)this.mockChildCommentDomainModel).getId()) >> Optional.of(this.mockChildCommentDomainModel)

        this.childCommentPort.delete(((ChildCommentDomainModel)this.mockChildCommentDomainModel).getId()) >> Optional.of(deletedChildCommentDomainModel)

        when:
        def childCommentResponse = this.childCommentService.delete(((UserDomainModel)this.mockChildCommentWriter).getId(), ((ChildCommentDomainModel)this.mockChildCommentDomainModel).getId())

        then:
        childCommentResponse instanceof ChildCommentResponseDto
        with(childCommentResponse) {
            getIsDeleted()
        }
    }

    def "Child Comment delete unauthorized case"() {
        this.userPort.findById(((UserDomainModel)this.mockChildCommentWriter).getId()) >> Optional.of(this.mockChildCommentWriter)
        this.postPort.findById(((PostDomainModel)this.mockPostDomainModel).getId()) >> Optional.of(this.mockPostDomainModel)
        this.childCommentPort.findById(((ChildCommentDomainModel)this.mockChildCommentDomainModel).getId()) >> Optional.of(this.mockChildCommentDomainModel)

        when: "Deleter's Role is NONE"
        ((UserDomainModel)this.mockChildCommentWriter).setRole(Role.NONE)
        this.childCommentService.delete(((UserDomainModel)this.mockChildCommentWriter).getId(), ((ChildCommentDomainModel)this.mockChildCommentDomainModel).getId())

        then:
        thrown(UnauthorizedException)

        when: "This user is not contents admin"
        ((UserDomainModel)this.mockChildCommentWriter).setRole(Role.COMMON)
        this.userPort.findById(((UserDomainModel)this.mockCommentWriter).getId()) >> Optional.of(this.mockCommentWriter)
        this.childCommentService.delete(((UserDomainModel)this.mockCommentWriter).getId(), ((ChildCommentDomainModel)this.mockChildCommentDomainModel).getId())

        then:
        thrown(UnauthorizedException)
    }

    def "Child Comment delete bad request case"() {
        this.userPort.findById(((UserDomainModel)this.mockChildCommentWriter).getId()) >> Optional.of(this.mockChildCommentWriter)
        this.postPort.findById(((PostDomainModel)this.mockPostDomainModel).getId()) >> Optional.of(this.mockPostDomainModel)
        this.childCommentPort.findById(((ChildCommentDomainModel)this.mockChildCommentDomainModel).getId()) >> Optional.of(this.mockChildCommentDomainModel)

        when: "Child comment is deleted"
        ((ChildCommentDomainModel)this.mockChildCommentDomainModel).setIsDeleted(true)
        this.childCommentService.delete(((UserDomainModel)this.mockChildCommentWriter).getId(), ((ChildCommentDomainModel)this.mockChildCommentDomainModel).getId())

        then:
        thrown(BadRequestException)
    }

    /**
     * Test case for child comment find all
     */
    def "Child Comment find all normal case"() {
        this.userPort.findById(((UserDomainModel)this.mockChildCommentWriter).getId()) >> Optional.of(this.mockChildCommentWriter)
        this.commentPort.findById(((CommentDomainModel)this.mockCommentDomainModel).getId()) >> Optional.of(this.mockCommentDomainModel)
        this.postPort.findById(((PostDomainModel)this.mockPostDomainModel).getId()) >> Optional.of(this.mockPostDomainModel)
        this.childCommentPort.findById(((ChildCommentDomainModel)this.mockChildCommentDomainModel).getId()) >> Optional.of(this.mockChildCommentDomainModel)

        this.childCommentPort.findByParentComment(((CommentDomainModel)this.mockCommentDomainModel).getId(), 0) >> new PageImpl<ChildCommentDomainModel>(List.of((ChildCommentDomainModel)this.mockChildCommentDomainModel))

        when:
        def childCommentResponse = this.childCommentService.findAll(((UserDomainModel)this.mockChildCommentWriter).getId(), ((CommentDomainModel)this.mockCommentDomainModel).getId(), 0)

        then:
        childCommentResponse instanceof ChildCommentsResponseDto
        with (childCommentResponse) {
            getChildComments().getContent().get(0).getContent() == "test child comment content"
        }
    }

    def "Child Comment find all unauthorized case"() {
        this.userPort.findById(((UserDomainModel)this.mockChildCommentWriter).getId()) >> Optional.of(this.mockChildCommentWriter)
        this.commentPort.findById(((CommentDomainModel)this.mockCommentDomainModel).getId()) >> Optional.of(this.mockCommentDomainModel)
        this.postPort.findById(((PostDomainModel)this.mockPostDomainModel).getId()) >> Optional.of(this.mockPostDomainModel)
        this.childCommentPort.findById(((ChildCommentDomainModel)this.mockChildCommentDomainModel).getId()) >> Optional.of(this.mockChildCommentDomainModel)

        when: "Request user's Role is NONE"
        ((UserDomainModel)this.mockChildCommentWriter).setRole(Role.NONE)
        this.childCommentService.findAll(((UserDomainModel)this.mockChildCommentWriter).getId(), ((CommentDomainModel)this.mockCommentDomainModel).getId(), 0)

        then:
        thrown(UnauthorizedException)
    }

    def "Child Comment find all bad request case"() {
        this.userPort.findById(((UserDomainModel)this.mockChildCommentWriter).getId()) >> Optional.of(this.mockChildCommentWriter)
        this.commentPort.findById(((CommentDomainModel)this.mockCommentDomainModel).getId()) >> Optional.of(this.mockCommentDomainModel)
        this.postPort.findById(((PostDomainModel)this.mockPostDomainModel).getId()) >> Optional.of(this.mockPostDomainModel)
        this.childCommentPort.findById(((ChildCommentDomainModel)this.mockChildCommentDomainModel).getId()) >> Optional.of(this.mockChildCommentDomainModel)

        when: "Board is deleted"
        ((BoardDomainModel)this.mockBoardDomainModel).setIsDeleted(true)
        this.childCommentService.findAll(((UserDomainModel)this.mockChildCommentWriter).getId(), ((CommentDomainModel)this.mockCommentDomainModel).getId(), 0)

        then:
        thrown(BadRequestException)

        when: "Post is deleted"
        ((BoardDomainModel)this.mockBoardDomainModel).setIsDeleted(false)
        ((PostDomainModel)this.mockPostDomainModel).setIsDeleted(true)
        this.childCommentService.findAll(((UserDomainModel)this.mockChildCommentWriter).getId(), ((CommentDomainModel)this.mockCommentDomainModel).getId(), 0)

        then:
        thrown(BadRequestException)
    }
}
