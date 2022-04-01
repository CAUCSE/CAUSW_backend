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
import net.causw.domain.model.CircleDomainModel
import net.causw.domain.model.CircleMemberDomainModel
import net.causw.domain.model.CircleMemberStatus
import net.causw.domain.model.CommentDomainModel
import net.causw.domain.model.PostDomainModel
import net.causw.domain.model.Role
import net.causw.domain.model.UserDomainModel
import net.causw.domain.model.UserState
import org.junit.Test
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

    def mockUserDomainModel
    def mockBoardDomainModel
    def mockPostWriter
    def mockPostDomainModel
    def mockCommentWriter
    def mockCommentDomainModel
    def mockChildCommentWriter
    def mockChildCommentDomainModel
    def mockRefChildCommentDomainModel

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
                null,
                List.of()
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
     * Test case for child comment create
     */
    @Test
    def "Child Comment create normal case"() {
        given:
        def childCommentCreateRequestDto = new ChildCommentCreateRequestDto(
                "test child comment content",
                "test comment id",
                "test ref child comment id"
        )

        this.userPort.findById("test child comment writer user id") >> Optional.of(this.mockChildCommentWriter)
        this.commentPort.findById("test comment id") >> Optional.of(this.mockCommentDomainModel)
        this.childCommentPort.findById("test ref child comment id") >> Optional.of(this.mockRefChildCommentDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        PowerMockito.mockStatic(ChildCommentDomainModel.class)

        this.childCommentPort.create((ChildCommentDomainModel)this.mockChildCommentDomainModel, (PostDomainModel)this.mockPostDomainModel) >> this.mockChildCommentDomainModel

        when: "without circle"
        PowerMockito.when(ChildCommentDomainModel.of(
                childCommentCreateRequestDto.getContent(),
                ((ChildCommentDomainModel) this.mockRefChildCommentDomainModel).getWriter().getName(),
                childCommentCreateRequestDto.getRefChildComment().orElse(null),
                (UserDomainModel) this.mockChildCommentWriter,
                (CommentDomainModel) this.mockCommentDomainModel
        )).thenReturn((ChildCommentDomainModel) this.mockChildCommentDomainModel)
        def childCommentResponseDto = this.childCommentService.create("test child comment writer user id", childCommentCreateRequestDto)

        then:
        childCommentResponseDto instanceof ChildCommentResponseDto
        with(childCommentResponseDto) {
            getContent() == "test child comment content"
        }

        when: "with circle"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test child comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        childCommentResponseDto = this.childCommentService.create("test child comment writer user id", childCommentCreateRequestDto)

        then:
        childCommentResponseDto instanceof ChildCommentResponseDto
        with(childCommentResponseDto) {
            getContent() == "test child comment content"
        }

        when: "with circle for admin"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockChildCommentWriter.setRole(Role.ADMIN)
        PowerMockito.when(ChildCommentDomainModel.of(
                childCommentCreateRequestDto.getContent(),
                ((ChildCommentDomainModel) this.mockRefChildCommentDomainModel).getWriter().getName(),
                childCommentCreateRequestDto.getRefChildComment().orElse(null),
                (UserDomainModel) this.mockChildCommentWriter,
                (CommentDomainModel) this.mockCommentDomainModel
        )).thenReturn((ChildCommentDomainModel) this.mockChildCommentDomainModel)
        childCommentResponseDto = this.childCommentService.create("test child comment writer user id", childCommentCreateRequestDto)

        then:
        childCommentResponseDto instanceof ChildCommentResponseDto
        with(childCommentResponseDto) {
            getContent() == "test child comment content"
        }
    }

    @Test
    def "Child Comment create deleted case"() {
        given:
        def childCommentCreateRequestDto = new ChildCommentCreateRequestDto(
                "test child comment content",
                "test comment id",
                "test ref child comment id"
        )

        this.userPort.findById("test child comment writer user id") >> Optional.of(this.mockChildCommentWriter)
        this.commentPort.findById("test comment id") >> Optional.of(this.mockCommentDomainModel)
        this.childCommentPort.findById("test ref child comment id") >> Optional.of(this.mockRefChildCommentDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        when: "deleted board"
        this.mockBoardDomainModel.setIsDeleted(true)
        this.childCommentService.create("test child comment writer user id", childCommentCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "deleted post"
        this.mockBoardDomainModel.setIsDeleted(false)
        this.mockPostDomainModel.setIsDeleted(true)
        this.childCommentService.create("test child comment writer user id", childCommentCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "deleted circle"
        this.mockPostDomainModel.setIsDeleted(false)
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockCircleDomainModel.setIsDeleted(true)
        this.circleMemberPort.findByUserIdAndCircleId("test child comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.childCommentService.create("test child comment writer user id", childCommentCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "deleted ref child comment"
        childCommentCreateRequestDto.setContent("test child comment content")
        this.mockChildCommentDomainModel.setContent("test child comment content")
        this.mockBoardDomainModel.setCircle(null)
        this.mockCircleDomainModel.setIsDeleted(false)
        this.mockRefChildCommentDomainModel.setIsDeleted(true)
        this.childCommentService.create(((UserDomainModel)this.mockChildCommentWriter).getId(), childCommentCreateRequestDto)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Child Comment create invalid parameter"() {
        given:
        def childCommentCreateRequestDto = new ChildCommentCreateRequestDto(
                "test child comment content",
                "test comment id",
                "test ref child comment id"
        )

        this.userPort.findById("test child comment writer user id") >> Optional.of(this.mockChildCommentWriter)
        this.commentPort.findById("test comment id") >> Optional.of(this.mockCommentDomainModel)
        this.childCommentPort.findById("test ref child comment id") >> Optional.of(this.mockRefChildCommentDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        PowerMockito.mockStatic(ChildCommentDomainModel.class)

        when: "Child comment's content is blank"
        childCommentCreateRequestDto.setContent("")
        this.mockChildCommentDomainModel.setContent("")
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
    }

    @Test
    def "Child Comment create unauthorized case"() {
        given:
        def childCommentCreateRequestDto = new ChildCommentCreateRequestDto(
                "test child comment content",
                "test comment id",
                "test ref child comment id"
        )

        this.userPort.findById("test child comment writer user id") >> Optional.of(this.mockChildCommentWriter)
        this.commentPort.findById("test comment id") >> Optional.of(this.mockCommentDomainModel)
        this.childCommentPort.findById("test ref child comment id") >> Optional.of(this.mockRefChildCommentDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        when: "circle member is await"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.circleMemberPort.findByUserIdAndCircleId("test child comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.childCommentService.create("test child comment writer user id", childCommentCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "circle member is blocked"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.REJECT)
        this.childCommentService.create("test child comment writer user id", childCommentCreateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test case for child comment find all
     */
    @Test
    def "Child Comment find all normal case"() {
        given:
        this.userPort.findById("test child comment writer user id") >> Optional.of(this.mockChildCommentWriter)
        this.commentPort.findById("test comment id") >> Optional.of(this.mockCommentDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        this.childCommentPort.findByParentComment("test comment id", 0) >> new PageImpl<ChildCommentDomainModel>(List.of((ChildCommentDomainModel)this.mockChildCommentDomainModel))

        when: "without circle"
        def childCommentsResponseDto = this.childCommentService.findAll("test child comment writer user id", "test comment id", 0)

        then:
        childCommentsResponseDto instanceof ChildCommentsResponseDto
        with (childCommentsResponseDto) {
            getChildComments().getContent().get(0).getContent() == "test child comment content"
        }

        when: "with circle"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test child comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        childCommentsResponseDto = this.childCommentService.findAll("test child comment writer user id", "test comment id", 0)

        then:
        childCommentsResponseDto instanceof ChildCommentsResponseDto
        with (childCommentsResponseDto) {
            getChildComments().getContent().get(0).getContent() == "test child comment content"
        }

        when: "with circle for admin"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockChildCommentWriter.setRole(Role.ADMIN)
        childCommentsResponseDto = this.childCommentService.findAll("test child comment writer user id", "test comment id", 0)

        then:
        childCommentsResponseDto instanceof ChildCommentsResponseDto
        with (childCommentsResponseDto) {
            getChildComments().getContent().get(0).getContent() == "test child comment content"
        }
    }

    @Test
    def "Child Comment find all deleted case"() {
        given:
        this.userPort.findById("test child comment writer user id") >> Optional.of(this.mockChildCommentWriter)
        this.commentPort.findById("test comment id") >> Optional.of(this.mockCommentDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        when: "deleted board"
        this.mockBoardDomainModel.setIsDeleted(true)
        this.childCommentService.findAll("test child comment writer user id", "test comment id", 0)

        then:
        thrown(BadRequestException)

        when: "deleted post"
        this.mockBoardDomainModel.setIsDeleted(false)
        this.mockPostDomainModel.setIsDeleted(true)
        this.childCommentService.findAll("test child comment writer user id", "test comment id", 0)

        then:
        thrown(BadRequestException)

        when: "deleted circle"
        this.mockPostDomainModel.setIsDeleted(false)
        this.mockCircleDomainModel.setIsDeleted(true)
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test child comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.childCommentService.findAll("test child comment writer user id", "test comment id", 0)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Child Comment find all unauthorized case"() {
        given:
        this.userPort.findById("test child comment writer user id") >> Optional.of(this.mockChildCommentWriter)
        this.commentPort.findById("test comment id") >> Optional.of(this.mockCommentDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        when: "circle member is await"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.circleMemberPort.findByUserIdAndCircleId("test child comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.childCommentService.findAll("test child comment writer user id", "test comment id", 0)

        then:
        thrown(BadRequestException)

        when: "circle member is blocked"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.REJECT)
        this.childCommentService.findAll("test child comment writer user id", "test comment id", 0)

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test case for child update create
     */
    @Test
    def "Child Comment update normal case"() {
        given:
        def childCommentUpdateRequestDto = new ChildCommentUpdateRequestDto(
                "test child comment content"
        )

        def updatedChildCommentDomainModel = ChildCommentDomainModel.of(
                "test child comment id",
                "updated content",
                false,
                "test child comment writer user name",
                "test ref child comment id",
                (UserDomainModel) this.mockChildCommentWriter,
                (CommentDomainModel) this.mockCommentDomainModel,
                null,
                null
        )

        this.userPort.findById("test child comment writer user id") >> Optional.of(this.mockChildCommentWriter)
        this.commentPort.findById("test comment id") >> Optional.of(this.mockCommentDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)
        this.childCommentPort.findById("test child comment id") >> Optional.of(this.mockChildCommentDomainModel)

        this.childCommentPort.update("test child comment id", (ChildCommentDomainModel)this.mockChildCommentDomainModel) >> Optional.of(updatedChildCommentDomainModel)

        when: "without circle"
        def childCommentResponse = this.childCommentService.update("test child comment writer user id", "test child comment id", childCommentUpdateRequestDto)

        then:
        childCommentResponse instanceof ChildCommentResponseDto
        with(childCommentResponse) {
            getContent() == "updated content"
        }

        when: "without circle for admin"
        this.mockChildCommentWriter.setRole(Role.ADMIN)
        childCommentResponse = this.childCommentService.update("test child comment writer user id", "test child comment id", childCommentUpdateRequestDto)

        then:
        childCommentResponse instanceof ChildCommentResponseDto
        with(childCommentResponse) {
            getContent() == "updated content"
        }

        when: "with circle"
        this.mockChildCommentWriter.setRole(Role.COMMON)
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test child comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        childCommentResponse = this.childCommentService.update("test child comment writer user id", "test child comment id", childCommentUpdateRequestDto)

        then:
        childCommentResponse instanceof ChildCommentResponseDto
        with(childCommentResponse) {
            getContent() == "updated content"
        }

        when: "with circle for admin"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockChildCommentWriter.setRole(Role.ADMIN)
        childCommentResponse = this.childCommentService.update("test child comment writer user id", "test child comment id", childCommentUpdateRequestDto)

        then:
        childCommentResponse instanceof ChildCommentResponseDto
        with(childCommentResponse) {
            getContent() == "updated content"
        }
    }

    @Test
    def "Child Comment update deleted case"() {
        given:
        def childCommentUpdateRequestDto = new ChildCommentUpdateRequestDto(
                "test child comment content"
        )

        this.userPort.findById("test child comment writer user id") >> Optional.of(this.mockChildCommentWriter)
        this.commentPort.findById("test comment id") >> Optional.of(this.mockCommentDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)
        this.childCommentPort.findById("test child comment id") >> Optional.of(this.mockChildCommentDomainModel)

        when: "deleted board"
        this.mockBoardDomainModel.setIsDeleted(true)
        this.childCommentService.update("test child comment writer user id", "test child comment id", childCommentUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "deleted post"
        this.mockBoardDomainModel.setIsDeleted(false)
        this.mockPostDomainModel.setIsDeleted(true)
        this.childCommentService.update("test child comment writer user id", "test child comment id", childCommentUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "deleted child comment"
        this.mockPostDomainModel.setIsDeleted(false)
        this.mockChildCommentDomainModel.setIsDeleted(true)
        this.childCommentService.update("test child comment writer user id", "test child comment id", childCommentUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "deleted circle"
        this.mockChildCommentDomainModel.setIsDeleted(false)
        this.mockCircleDomainModel.setIsDeleted(true)
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test child comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.childCommentService.update("test child comment writer user id", "test child comment id", childCommentUpdateRequestDto)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Child Comment update invalid parameter"() {
        given:
        def childCommentUpdateRequestDto = new ChildCommentUpdateRequestDto(
                ""
        )

        this.userPort.findById("test child comment writer user id") >> Optional.of(this.mockChildCommentWriter)
        this.commentPort.findById("test comment id") >> Optional.of(this.mockCommentDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)
        this.childCommentPort.findById("test child comment id") >> Optional.of(this.mockChildCommentDomainModel)

        when:
        this.childCommentService.update("test child comment writer user id", "test child comment id", childCommentUpdateRequestDto)

        then:
        thrown(ConstraintViolationException)
    }

    @Test
    def "Child Comment update unauthorized case"() {
        given:
        def childCommentUpdateRequestDto = new ChildCommentUpdateRequestDto(
                "test child comment content"
        )

        this.userPort.findById("test child comment writer user id") >> Optional.of(this.mockChildCommentWriter)
        this.commentPort.findById("test comment id") >> Optional.of(this.mockCommentDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)
        this.childCommentPort.findById("test child comment id") >> Optional.of(this.mockChildCommentDomainModel)

        when: "not writer"
        this.mockUserDomainModel.setRole(Role.COMMON)
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.childCommentService.update("test", "test child comment id", childCommentUpdateRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "circle member is await"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.circleMemberPort.findByUserIdAndCircleId("test child comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.childCommentService.update("test child comment writer user id", "test child comment id", childCommentUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "circle member is blocked"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.REJECT)
        this.childCommentService.update("test child comment writer user id", "test child comment id", childCommentUpdateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test case for child comment delete
     */
    @Test
    def "Child Comment delete normal case"() {
        given:
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

        this.userPort.findById("test child comment writer user id") >> Optional.of(this.mockChildCommentWriter)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)
        this.childCommentPort.findById("test child comment id") >> Optional.of(this.mockChildCommentDomainModel)

        this.childCommentPort.delete("test child comment id") >> Optional.of(deletedChildCommentDomainModel)

        when: "without circle"
        def childCommentResponseDto = this.childCommentService.delete("test child comment writer user id", "test child comment id")

        then:
        childCommentResponseDto instanceof ChildCommentResponseDto
        with(childCommentResponseDto) {
            getIsDeleted()
        }

        when: "without circle for president"
        this.mockUserDomainModel.setRole(Role.PRESIDENT)
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        childCommentResponseDto = this.childCommentService.delete("test", "test child comment id")

        then:
        childCommentResponseDto instanceof ChildCommentResponseDto
        with(childCommentResponseDto) {
            getIsDeleted()
        }

        when: "with circle"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test child comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        childCommentResponseDto = this.childCommentService.delete("test child comment writer user id", "test child comment id")

        then:
        childCommentResponseDto instanceof ChildCommentResponseDto
        with(childCommentResponseDto) {
            getIsDeleted()
        }

        when: "with circle for leader circle"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.userPort.findById("test leader user id") >> Optional.of(this.mockCircleLeaderUserDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test leader user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        childCommentResponseDto = this.childCommentService.delete("test leader user id", "test child comment id")

        then:
        childCommentResponseDto instanceof ChildCommentResponseDto
        with(childCommentResponseDto) {
            getIsDeleted()
        }

        when: "with circle for admin"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockUserDomainModel.setRole(Role.ADMIN)
        childCommentResponseDto = this.childCommentService.delete("test", "test child comment id")

        then:
        childCommentResponseDto instanceof ChildCommentResponseDto
        with(childCommentResponseDto) {
            getIsDeleted()
        }
    }

    @Test
    def "Child Comment delete deleted case"() {
        given:
        this.userPort.findById("test child comment writer user id") >> Optional.of(this.mockChildCommentWriter)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)
        this.childCommentPort.findById("test child comment id") >> Optional.of(this.mockChildCommentDomainModel)

        when: "deleted child comment"
        this.mockChildCommentDomainModel.setIsDeleted(true)
        this.childCommentService.delete("test child comment writer user id", "test child comment id")

        then:
        thrown(BadRequestException)

        when: "deleted circle"
        this.mockChildCommentDomainModel.setIsDeleted(false)
        this.mockCircleDomainModel.setIsDeleted(true)
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test child comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.childCommentService.delete("test child comment writer user id", "test child comment id")

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Child Comment delete unauthorized case"() {
        this.userPort.findById("test child comment writer user id") >> Optional.of(this.mockChildCommentWriter)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)
        this.childCommentPort.findById("test child comment id") >> Optional.of(this.mockChildCommentDomainModel)

        when: "not writer"
        this.mockUserDomainModel.setRole(Role.COMMON)
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.childCommentService.delete("test", "test child comment id")

        then:
        thrown(UnauthorizedException)

        when: "not writer with circle"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.childCommentService.delete("test", "test child comment id")

        then:
        thrown(UnauthorizedException)

        when: "not leader with circle"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockUserDomainModel.setRole(Role.LEADER_CIRCLE)
        this.childCommentService.delete("test", "test child comment id")

        then:
        thrown(UnauthorizedException)

        when: "circle member is await"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.circleMemberPort.findByUserIdAndCircleId("test child comment writer user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.childCommentService.delete("test child comment writer user id", "test child comment id")

        then:
        thrown(BadRequestException)

        when: "circle member is blocked"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.REJECT)
        this.childCommentService.delete("test child comment writer user id", "test child comment id")

        then:
        thrown(UnauthorizedException)
    }
}
