package net.causw.application

import net.causw.application.dto.CommentCreateRequestDto
import net.causw.application.dto.CommentResponseDto
import net.causw.application.dto.CommentUpdateRequestDto
import net.causw.application.spi.CircleMemberPort
import net.causw.application.spi.CommentPort
import net.causw.application.spi.PostPort
import net.causw.application.spi.UserPort
import net.causw.domain.exceptions.BadRequestException
import net.causw.domain.exceptions.UnauthorizedException
import net.causw.domain.model.*
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
    /* for find all test case */
    def mockCommentWriterUserDomainModel2
    def mockCommentWriterUserDomainModel3
    def mockCommentDomainModel2
    def mockCommentDomainModel3

    // Circle
    def mockCircleLeaderUserDomainModel
    def mockCircleDomainModel

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

        this.mockCommentWriterUserDomainModel2 = UserDomainModel.of(
                "test comment writer 2 user id",
                "test-comment-writer2@cau.ac.kr",
                "test comment writer 2 user name",
                "test1234!",
                "20210002",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        this.mockCommentWriterUserDomainModel3 = UserDomainModel.of(
                "test comment writer 3 user id",
                "test-comment-writer3@cau.ac.kr",
                "test comment writer 3 user name",
                "test1234!",
                "20210003",
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

        this.mockCommentDomainModel2 = CommentDomainModel.of(
                "test comment 2 id",
                "test comment 2 content",
                false,
                null,
                null,
                (UserDomainModel) this.mockCommentWriterUserDomainModel2,
                ((PostDomainModel) this.mockPostDomainModel).getId(),
        )

        this.mockCommentDomainModel3 = CommentDomainModel.of(
                "test comment 3 id",
                "test comment 3 content",
                false,
                null,
                null,
                (UserDomainModel) this.mockCommentWriterUserDomainModel3,
                ((PostDomainModel) this.mockPostDomainModel).getId(),
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

        /* TODO when: "Parent comment is deleted" */
    }

    def "Comment find all normal case"() {
        given: "Create multiple comments"
        def mockCommentCreateRequestDto = new CommentCreateRequestDto(
                ((CommentDomainModel) this.mockCommentDomainModel).getContent(),
                ((CommentDomainModel) this.mockCommentDomainModel).getPostId(),
                null
        )

        def mockCommentCreateRequestDto2 = new CommentCreateRequestDto(
                ((CommentDomainModel) this.mockCommentDomainModel2).getContent(),
                ((CommentDomainModel) this.mockCommentDomainModel2).getPostId(),
                ((CommentDomainModel) this.mockParentCommentDomainModel).getId()
        )

        def mockCommentCreateRequestDto3 = new CommentCreateRequestDto(
                ((CommentDomainModel) this.mockCommentDomainModel3).getContent(),
                ((CommentDomainModel) this.mockCommentDomainModel3).getPostId(),
                ((CommentDomainModel) this.mockParentCommentDomainModel).getId()
        )

        this.userPort.findById(((UserDomainModel) this.mockCommentWriterUserDomainModel).getId()) >> Optional.of((UserDomainModel) this.mockCommentWriterUserDomainModel)
        this.userPort.findById(((UserDomainModel) this.mockCommentWriterUserDomainModel2).getId()) >> Optional.of((UserDomainModel) this.mockCommentWriterUserDomainModel2)
        this.userPort.findById(((UserDomainModel) this.mockCommentWriterUserDomainModel3).getId()) >> Optional.of((UserDomainModel) this.mockCommentWriterUserDomainModel3)
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of((PostDomainModel) this.mockPostDomainModel)
        this.commentPort.findById(((CommentDomainModel) this.mockParentCommentDomainModel).getId()) >> Optional.of(((CommentDomainModel) this.mockParentCommentDomainModel))
        this.commentPort.findByPostId(((PostDomainModel) this.mockPostDomainModel).getId(), 0) >> new PageImpl<CommentDomainModel>(List.of((CommentDomainModel) this.mockCommentDomainModel, (CommentDomainModel) this.mockCommentDomainModel2, (CommentDomainModel) this.mockCommentDomainModel3))
        this.commentPort.create((CommentDomainModel) this.mockCommentDomainModel, (PostDomainModel) this.mockPostDomainModel) >> (CommentDomainModel) this.mockCommentDomainModel
        this.commentPort.create((CommentDomainModel) this.mockCommentDomainModel2, (PostDomainModel) this.mockPostDomainModel) >> (CommentDomainModel) this.mockCommentDomainModel2
        this.commentPort.create((CommentDomainModel) this.mockCommentDomainModel3, (PostDomainModel) this.mockPostDomainModel) >> (CommentDomainModel) this.mockCommentDomainModel3

        ((CommentDomainModel) this.mockCommentDomainModel2).setParentComment(((CommentDomainModel) this.mockParentCommentDomainModel))
        ((CommentDomainModel) this.mockCommentDomainModel3).setParentComment(((CommentDomainModel) this.mockParentCommentDomainModel))
        mockCommentCreateRequestDto2.setParentCommentId(((CommentDomainModel) this.mockParentCommentDomainModel).getId())
        mockCommentCreateRequestDto3.setParentCommentId(((CommentDomainModel) this.mockParentCommentDomainModel).getId())

        PowerMockito.mockStatic(CommentDomainModel.class)
        PowerMockito.when(CommentDomainModel.of(
                mockCommentCreateRequestDto.getContent(),
                (UserDomainModel) this.mockCommentWriterUserDomainModel,
                ((PostDomainModel) this.mockPostDomainModel).getId(),
                null
        )).thenReturn((CommentDomainModel) this.mockCommentDomainModel)

        PowerMockito.when(CommentDomainModel.of(
                mockCommentCreateRequestDto2.getContent(),
                (UserDomainModel) this.mockCommentWriterUserDomainModel2,
                ((PostDomainModel) this.mockPostDomainModel).getId(),
                (CommentDomainModel) this.mockParentCommentDomainModel
        )).thenReturn((CommentDomainModel) this.mockCommentDomainModel2)

        PowerMockito.when(CommentDomainModel.of(
                mockCommentCreateRequestDto3.getContent(),
                (UserDomainModel) this.mockCommentWriterUserDomainModel3,
                ((PostDomainModel) this.mockPostDomainModel).getId(),
                (CommentDomainModel) this.mockParentCommentDomainModel
        )).thenReturn((CommentDomainModel) this.mockCommentDomainModel3)

        this.commentService.create("test comment writer user id", mockCommentCreateRequestDto)
        this.commentService.create("test comment writer 2 user id", mockCommentCreateRequestDto2)
        this.commentService.create("test comment writer 3 user id", mockCommentCreateRequestDto3)

        when:
        def commentList = this.commentService.findAll(((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(), ((PostDomainModel) this.mockPostDomainModel).getId(), 0)

        then:
        commentList instanceof Page<CommentResponseDto>
        commentList.size() == 3
        commentList.getContent().get(0).getContent() == "test comment content"
        commentList.getContent().get(1).getContent() == "test comment 2 content"
        commentList.getContent().get(2).getContent() == "test comment 3 content"
        commentList.getContent().get(0).getParentCommentId() == null
        commentList.getContent().get(1).getParentCommentId() == "test parent comment id"
        commentList.getContent().get(2).getParentCommentId() == "test parent comment id"
    }

    /**
     * Test case for comment update
     */
    def "Comment update normal without circle case"() {
        given:
        def targetContent = "Update comment content"
        def mockUpdatedCommentDomainModel = CommentDomainModel.of(
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                targetContent,
                ((CommentDomainModel) this.mockCommentDomainModel).getIsDeleted(),
                ((CommentDomainModel) this.mockCommentDomainModel).getCreatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getUpdatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getWriter(),
                ((CommentDomainModel) this.mockCommentDomainModel).getPostId()
        )

        def commentUpdateRequestDto = new CommentUpdateRequestDto(targetContent)

        this.userPort.findById(((UserDomainModel) this.mockCommentWriterUserDomainModel).getId()) >> Optional.of((UserDomainModel) this.mockCommentWriterUserDomainModel)
        this.commentPort.findById(((CommentDomainModel) this.mockCommentDomainModel).getId()) >> Optional.of(((CommentDomainModel) this.mockCommentDomainModel))
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(((PostDomainModel) this.mockPostDomainModel))

        this.commentPort.update(((CommentDomainModel) this.mockCommentDomainModel).getId(), mockUpdatedCommentDomainModel) >> Optional.of(mockUpdatedCommentDomainModel)

        when:
        PowerMockito.mockStatic(CommentDomainModel.class)
        PowerMockito.when(CommentDomainModel.of(
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                targetContent,
                ((CommentDomainModel) this.mockCommentDomainModel).getIsDeleted(),
                ((CommentDomainModel) this.mockCommentDomainModel).getCreatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getUpdatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getWriter(),
                ((CommentDomainModel) this.mockCommentDomainModel).getPostId()
        )).thenReturn(mockUpdatedCommentDomainModel)
        def commentUpdateResponse = this.commentService.update(
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                commentUpdateRequestDto
        )

        then:
        commentUpdateResponse instanceof CommentResponseDto
        commentUpdateResponse.getContent() == targetContent
    }

    def "Comment update normal with circle case"() {
        given:
        def targetContent = "Update comment content"
        def mockUpdatedCommentDomainModel = CommentDomainModel.of(
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                targetContent,
                ((CommentDomainModel) this.mockCommentDomainModel).getIsDeleted(),
                ((CommentDomainModel) this.mockCommentDomainModel).getCreatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getUpdatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getWriter(),
                ((CommentDomainModel) this.mockCommentDomainModel).getPostId()
        )

        def mockWriterUserCircleMemberDomainModel = CircleMemberDomainModel.of(
                "test circle member id",
                CircleMemberStatus.MEMBER,
                (CircleDomainModel) this.mockCircleDomainModel,
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(),
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getName(),
                null,
                null
        )

        def commentUpdateRequestDto = new CommentUpdateRequestDto(targetContent)

        this.userPort.findById(((UserDomainModel) this.mockCommentWriterUserDomainModel).getId()) >> Optional.of((UserDomainModel) this.mockCommentWriterUserDomainModel)
        this.commentPort.findById(((CommentDomainModel) this.mockCommentDomainModel).getId()) >> Optional.of(((CommentDomainModel) this.mockCommentDomainModel))

        ((BoardDomainModel) this.mockBoardDomainModel).setCircle((CircleDomainModel) this.mockCircleDomainModel)
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(((PostDomainModel) this.mockPostDomainModel))
        this.circleMemberPort.findByUserIdAndCircleId(((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(), ((CircleDomainModel) this.mockCircleDomainModel).getId()) >> Optional.of(mockWriterUserCircleMemberDomainModel)

        this.commentPort.update(((CommentDomainModel) this.mockCommentDomainModel).getId(), mockUpdatedCommentDomainModel) >> Optional.of(mockUpdatedCommentDomainModel)

        when:
        PowerMockito.mockStatic(CommentDomainModel.class)
        PowerMockito.when(CommentDomainModel.of(
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                targetContent,
                ((CommentDomainModel) this.mockCommentDomainModel).getIsDeleted(),
                ((CommentDomainModel) this.mockCommentDomainModel).getCreatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getUpdatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getWriter(),
                ((CommentDomainModel) this.mockCommentDomainModel).getPostId()
        )).thenReturn(mockUpdatedCommentDomainModel)
        def commentUpdateResponse = this.commentService.update(
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                commentUpdateRequestDto
        )

        then:
        commentUpdateResponse instanceof CommentResponseDto
        commentUpdateResponse.getContent() == targetContent
    }

    def "Comment update target deleted case"() {
        given:
        def targetContent = "Update comment content"
        def mockUpdatedCommentDomainModel = CommentDomainModel.of(
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                targetContent,
                ((CommentDomainModel) this.mockCommentDomainModel).getIsDeleted(),
                ((CommentDomainModel) this.mockCommentDomainModel).getCreatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getUpdatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getWriter(),
                ((CommentDomainModel) this.mockCommentDomainModel).getPostId()
        )

        def commentUpdateRequestDto = new CommentUpdateRequestDto(targetContent)

        this.userPort.findById(((UserDomainModel) this.mockCommentWriterUserDomainModel).getId()) >> Optional.of((UserDomainModel) this.mockCommentWriterUserDomainModel)
        this.commentPort.findById(((CommentDomainModel) this.mockCommentDomainModel).getId()) >> Optional.of(((CommentDomainModel) this.mockCommentDomainModel))
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(((PostDomainModel) this.mockPostDomainModel))

        this.commentPort.update(((CommentDomainModel) this.mockCommentDomainModel).getId(), mockUpdatedCommentDomainModel) >> Optional.of(mockUpdatedCommentDomainModel)

        when: "Target comment deleted"
        PowerMockito.mockStatic(CommentDomainModel.class)
        PowerMockito.when(CommentDomainModel.of(
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                targetContent,
                ((CommentDomainModel) this.mockCommentDomainModel).getIsDeleted(),
                ((CommentDomainModel) this.mockCommentDomainModel).getCreatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getUpdatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getWriter(),
                ((CommentDomainModel) this.mockCommentDomainModel).getPostId()
        )).thenReturn(mockUpdatedCommentDomainModel)
        ((CommentDomainModel) this.mockCommentDomainModel).setIsDeleted(true)
        ((PostDomainModel) this.mockPostDomainModel).setIsDeleted(false)
        this.commentService.update(
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                commentUpdateRequestDto
        )

        then: "Target post deleted"
        thrown(BadRequestException)

        when:
        ((CommentDomainModel) this.mockCommentDomainModel).setIsDeleted(false)
        ((PostDomainModel) this.mockPostDomainModel).setIsDeleted(true)
        this.commentService.update(
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                commentUpdateRequestDto
        )

        then:
        thrown(BadRequestException)
    }

    def "Comment update request user not writer case"() {
        given:
        def targetContent = "Update comment content"
        def mockUpdatedCommentDomainModel = CommentDomainModel.of(
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                targetContent,
                ((CommentDomainModel) this.mockCommentDomainModel).getIsDeleted(),
                ((CommentDomainModel) this.mockCommentDomainModel).getCreatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getUpdatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getWriter(),
                ((CommentDomainModel) this.mockCommentDomainModel).getPostId()
        )

        def requestUser = UserDomainModel.of(
                "test request user id",
                "test-request-user@cau.ac.kr",
                "test request user name",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        def commentUpdateRequestDto = new CommentUpdateRequestDto(targetContent)

        this.userPort.findById(requestUser.getId()) >> Optional.of(requestUser)
        this.commentPort.findById(((CommentDomainModel) this.mockCommentDomainModel).getId()) >> Optional.of(((CommentDomainModel) this.mockCommentDomainModel))
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(((PostDomainModel) this.mockPostDomainModel))

        this.commentPort.update(((CommentDomainModel) this.mockCommentDomainModel).getId(), mockUpdatedCommentDomainModel) >> Optional.of(mockUpdatedCommentDomainModel)

        when:
        PowerMockito.mockStatic(CommentDomainModel.class)
        PowerMockito.when(CommentDomainModel.of(
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                targetContent,
                ((CommentDomainModel) this.mockCommentDomainModel).getIsDeleted(),
                ((CommentDomainModel) this.mockCommentDomainModel).getCreatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getUpdatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getWriter(),
                ((CommentDomainModel) this.mockCommentDomainModel).getPostId()
        )).thenReturn(mockUpdatedCommentDomainModel)
        this.commentService.update(
                requestUser.getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                commentUpdateRequestDto
        )

        then:
        thrown(UnauthorizedException)
    }

    def "Comment update not circle member case"() {
        given:
        def targetContent = "Update comment content"
        def mockUpdatedCommentDomainModel = CommentDomainModel.of(
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                targetContent,
                ((CommentDomainModel) this.mockCommentDomainModel).getIsDeleted(),
                ((CommentDomainModel) this.mockCommentDomainModel).getCreatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getUpdatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getWriter(),
                ((CommentDomainModel) this.mockCommentDomainModel).getPostId()
        )

        def commentUpdateRequestDto = new CommentUpdateRequestDto(targetContent)

        this.userPort.findById(((UserDomainModel) this.mockCommentWriterUserDomainModel).getId()) >> Optional.of((UserDomainModel) this.mockCommentWriterUserDomainModel)
        this.commentPort.findById(((CommentDomainModel) this.mockCommentDomainModel).getId()) >> Optional.of(((CommentDomainModel) this.mockCommentDomainModel))

        ((BoardDomainModel) this.mockBoardDomainModel).setCircle((CircleDomainModel) this.mockCircleDomainModel)
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(((PostDomainModel) this.mockPostDomainModel))
        this.circleMemberPort.findByUserIdAndCircleId(((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(), ((CircleDomainModel) this.mockCircleDomainModel).getId()) >> Optional.empty()

        this.commentPort.update(((CommentDomainModel) this.mockCommentDomainModel).getId(), mockUpdatedCommentDomainModel) >> Optional.of(mockUpdatedCommentDomainModel)

        when:
        PowerMockito.mockStatic(CommentDomainModel.class)
        PowerMockito.when(CommentDomainModel.of(
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                targetContent,
                ((CommentDomainModel) this.mockCommentDomainModel).getIsDeleted(),
                ((CommentDomainModel) this.mockCommentDomainModel).getCreatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getUpdatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getWriter(),
                ((CommentDomainModel) this.mockCommentDomainModel).getPostId()
        )).thenReturn(mockUpdatedCommentDomainModel)
        this.commentService.update(
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                commentUpdateRequestDto
        )

        then:
        thrown(UnauthorizedException)
    }

    def "Comment update not circle member status MEMBER case"() {
        given:
        def targetContent = "Update comment content"
        def mockUpdatedCommentDomainModel = CommentDomainModel.of(
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                targetContent,
                ((CommentDomainModel) this.mockCommentDomainModel).getIsDeleted(),
                ((CommentDomainModel) this.mockCommentDomainModel).getCreatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getUpdatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getWriter(),
                ((CommentDomainModel) this.mockCommentDomainModel).getPostId()
        )

        def mockWriterUserCircleMemberDomainModel = CircleMemberDomainModel.of(
                "test circle member id",
                CircleMemberStatus.MEMBER,
                (CircleDomainModel) this.mockCircleDomainModel,
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(),
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getName(),
                null,
                null
        )

        def commentUpdateRequestDto = new CommentUpdateRequestDto(targetContent)

        this.userPort.findById(((UserDomainModel) this.mockCommentWriterUserDomainModel).getId()) >> Optional.of((UserDomainModel) this.mockCommentWriterUserDomainModel)
        this.commentPort.findById(((CommentDomainModel) this.mockCommentDomainModel).getId()) >> Optional.of(((CommentDomainModel) this.mockCommentDomainModel))

        ((BoardDomainModel) this.mockBoardDomainModel).setCircle((CircleDomainModel) this.mockCircleDomainModel)
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(((PostDomainModel) this.mockPostDomainModel))
        this.circleMemberPort.findByUserIdAndCircleId(((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(), ((CircleDomainModel) this.mockCircleDomainModel).getId()) >> Optional.of(mockWriterUserCircleMemberDomainModel)

        this.commentPort.update(((CommentDomainModel) this.mockCommentDomainModel).getId(), mockUpdatedCommentDomainModel) >> Optional.of(mockUpdatedCommentDomainModel)

        when: "Exception case with AWAIT status"
        PowerMockito.mockStatic(CommentDomainModel.class)
        PowerMockito.when(CommentDomainModel.of(
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                targetContent,
                ((CommentDomainModel) this.mockCommentDomainModel).getIsDeleted(),
                ((CommentDomainModel) this.mockCommentDomainModel).getCreatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getUpdatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getWriter(),
                ((CommentDomainModel) this.mockCommentDomainModel).getPostId()
        )).thenReturn(mockUpdatedCommentDomainModel)
        mockWriterUserCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.commentService.update(
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                commentUpdateRequestDto
        )

        then:
        thrown(BadRequestException)

        when: "Exception case with REJECT status"
        mockWriterUserCircleMemberDomainModel.setStatus(CircleMemberStatus.REJECT)
        this.commentService.update(
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                commentUpdateRequestDto
        )

        then:
        thrown(UnauthorizedException)

        when: "Exception case with LEAVE status"
        mockWriterUserCircleMemberDomainModel.setStatus(CircleMemberStatus.LEAVE)
        this.commentService.update(
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                commentUpdateRequestDto
        )

        then:
        thrown(BadRequestException)

        when: "Exception case with DROP status"
        mockWriterUserCircleMemberDomainModel.setStatus(CircleMemberStatus.DROP)
        this.commentService.update(
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                commentUpdateRequestDto
        )

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test case for comment delete
     */
    def "Comment delete normal without circle case"() {
        given:
        def requestPresidentUser = UserDomainModel.of(
                "test president user id",
                "test-president-user@cau.ac.kr",
                "test president user name",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        def mockDeletedCommentDomainModel = CommentDomainModel.of(
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getContent(),
                true,
                ((CommentDomainModel) this.mockCommentDomainModel).getCreatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getUpdatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getWriter(),
                ((CommentDomainModel) this.mockCommentDomainModel).getPostId()
        )

        this.userPort.findById(requestPresidentUser.getId()) >> Optional.of(requestPresidentUser)
        this.userPort.findById(((UserDomainModel) this.mockCommentWriterUserDomainModel).getId()) >> Optional.of((UserDomainModel) this.mockCommentWriterUserDomainModel)
        this.commentPort.findById(((CommentDomainModel) this.mockCommentDomainModel).getId()) >> Optional.of(((CommentDomainModel) this.mockCommentDomainModel))
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(((PostDomainModel) this.mockPostDomainModel))

        this.commentPort.delete(((CommentDomainModel) this.mockCommentDomainModel).getId()) >> Optional.of(mockDeletedCommentDomainModel)

        when: "delete comment with president user"
        def commentDeleteResponse = this.commentService.delete(
                requestPresidentUser.getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getId()
        )

        then:
        commentDeleteResponse instanceof CommentResponseDto
        with(commentDeleteResponse) {
            commentDeleteResponse.getIsDeleted()
        }

        when: "delete comment with writer user"
        commentDeleteResponse = this.commentService.delete(
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getId()
        )

        then:
        commentDeleteResponse instanceof CommentResponseDto
        with(commentDeleteResponse) {
            commentDeleteResponse.getIsDeleted()
        }
    }

    def "Comment delete normal with circle case"() {
        given:
        ((BoardDomainModel) this.mockBoardDomainModel).setCircle((CircleDomainModel) this.mockCircleDomainModel)

        def mockDeletedCommentDomainModel = CommentDomainModel.of(
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getContent(),
                true,
                ((CommentDomainModel) this.mockCommentDomainModel).getCreatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getUpdatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getWriter(),
                ((CommentDomainModel) this.mockCommentDomainModel).getPostId()
        )

        def mockWriterUserCircleMemberDomainModel = CircleMemberDomainModel.of(
                "test circle member id",
                CircleMemberStatus.MEMBER,
                (CircleDomainModel) this.mockCircleDomainModel,
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(),
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getName(),
                null,
                null
        )

        def mockCircleLeaderCircleMemberDomainModel = CircleMemberDomainModel.of(
                "test circle member id",
                CircleMemberStatus.MEMBER,
                (CircleDomainModel) this.mockCircleDomainModel,
                ((UserDomainModel) this.mockCircleLeaderUserDomainModel).getId(),
                ((UserDomainModel) this.mockCircleLeaderUserDomainModel).getName(),
                null,
                null
        )

        this.userPort.findById(((UserDomainModel) this.mockCircleLeaderUserDomainModel).getId()) >> Optional.of((UserDomainModel) this.mockCircleLeaderUserDomainModel)
        this.userPort.findById(((UserDomainModel) this.mockCommentWriterUserDomainModel).getId()) >> Optional.of((UserDomainModel) this.mockCommentWriterUserDomainModel)
        this.commentPort.findById(((CommentDomainModel) this.mockCommentDomainModel).getId()) >> Optional.of(((CommentDomainModel) this.mockCommentDomainModel))
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(((PostDomainModel) this.mockPostDomainModel))

        this.circleMemberPort.findByUserIdAndCircleId(((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(), ((CircleDomainModel) this.mockCircleDomainModel).getId()) >> Optional.of(mockWriterUserCircleMemberDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId(((UserDomainModel) this.mockCircleLeaderUserDomainModel).getId(), ((CircleDomainModel) this.mockCircleDomainModel).getId()) >> Optional.of(mockCircleLeaderCircleMemberDomainModel)

        this.commentPort.delete(((CommentDomainModel) this.mockCommentDomainModel).getId()) >> Optional.of(mockDeletedCommentDomainModel)

        when: "delete comment with circle leader user"
        def commentDeleteResponse = this.commentService.delete(
                ((UserDomainModel) this.mockCircleLeaderUserDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getId()
        )

        then:
        commentDeleteResponse instanceof CommentResponseDto
        with(commentDeleteResponse) {
            commentDeleteResponse.getIsDeleted()
        }

        when: "delete comment with writer user"
        commentDeleteResponse = this.commentService.delete(
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getId()
        )

        then:
        commentDeleteResponse instanceof CommentResponseDto
        with(commentDeleteResponse) {
            commentDeleteResponse.getIsDeleted()
        }
    }

    def "Comment delete target deleted case"() {
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

        this.userPort.findById(((UserDomainModel) this.mockCommentWriterUserDomainModel).getId()) >> Optional.of((UserDomainModel) this.mockCommentWriterUserDomainModel)
        this.commentPort.findById(((CommentDomainModel) this.mockCommentDomainModel).getId()) >> Optional.of(((CommentDomainModel) this.mockCommentDomainModel))
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(((PostDomainModel) this.mockPostDomainModel))

        this.commentPort.delete(((CommentDomainModel) this.mockCommentDomainModel).getId()) >> Optional.of(mockDeletedCommentDomainModel)

        when: "Target comment deleted"
        ((CommentDomainModel) this.mockCommentDomainModel).setIsDeleted(true)
        ((PostDomainModel) this.mockPostDomainModel).setIsDeleted(false)
        this.commentService.delete(
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getId()
        )

        then:
        thrown(BadRequestException)

        when: "Target post deleted"
        ((CommentDomainModel) this.mockCommentDomainModel).setIsDeleted(false)
        ((PostDomainModel) this.mockPostDomainModel).setIsDeleted(true)
        this.commentService.delete(
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getId()
        )

        then:
        thrown(BadRequestException)
    }

    def "Comment delete not circle member case"() {
        given:
        ((BoardDomainModel) this.mockBoardDomainModel).setCircle((CircleDomainModel) this.mockCircleDomainModel)

        def mockDeletedCommentDomainModel = CommentDomainModel.of(
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getContent(),
                true,
                ((CommentDomainModel) this.mockCommentDomainModel).getCreatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getUpdatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getWriter(),
                ((CommentDomainModel) this.mockCommentDomainModel).getPostId()
        )

        this.userPort.findById(((UserDomainModel) this.mockCommentWriterUserDomainModel).getId()) >> Optional.of((UserDomainModel) this.mockCommentWriterUserDomainModel)
        this.commentPort.findById(((CommentDomainModel) this.mockCommentDomainModel).getId()) >> Optional.of(((CommentDomainModel) this.mockCommentDomainModel))
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(((PostDomainModel) this.mockPostDomainModel))

        this.circleMemberPort.findByUserIdAndCircleId(((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(), ((CircleDomainModel) this.mockCircleDomainModel).getId()) >> Optional.empty()

        this.commentPort.delete(((CommentDomainModel) this.mockCommentDomainModel).getId()) >> Optional.of(mockDeletedCommentDomainModel)

        when:
        this.commentService.delete(
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getId()
        )

        then:
        thrown(UnauthorizedException)
    }

    def "Comment delete not circle member status MEMBER case"() {
        given:
        ((BoardDomainModel) this.mockBoardDomainModel).setCircle((CircleDomainModel) this.mockCircleDomainModel)

        def mockDeletedCommentDomainModel = CommentDomainModel.of(
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getContent(),
                true,
                ((CommentDomainModel) this.mockCommentDomainModel).getCreatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getUpdatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getWriter(),
                ((CommentDomainModel) this.mockCommentDomainModel).getPostId()
        )

        def mockWriterUserCircleMemberDomainModel = CircleMemberDomainModel.of(
                "test circle member id",
                CircleMemberStatus.MEMBER,
                (CircleDomainModel) this.mockCircleDomainModel,
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(),
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getName(),
                null,
                null
        )

        this.userPort.findById(((UserDomainModel) this.mockCommentWriterUserDomainModel).getId()) >> Optional.of((UserDomainModel) this.mockCommentWriterUserDomainModel)
        this.commentPort.findById(((CommentDomainModel) this.mockCommentDomainModel).getId()) >> Optional.of(((CommentDomainModel) this.mockCommentDomainModel))
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(((PostDomainModel) this.mockPostDomainModel))

        this.circleMemberPort.findByUserIdAndCircleId(((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(), ((CircleDomainModel) this.mockCircleDomainModel).getId()) >> Optional.of(mockWriterUserCircleMemberDomainModel)

        this.commentPort.delete(((CommentDomainModel) this.mockCommentDomainModel).getId()) >> Optional.of(mockDeletedCommentDomainModel)

        when: "Exception case with AWAIT status"
        mockWriterUserCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.commentService.delete(
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getId()
        )

        then:
        thrown(BadRequestException)

        when: "Exception case with REJECT status"
        mockWriterUserCircleMemberDomainModel.setStatus(CircleMemberStatus.REJECT)
        this.commentService.delete(
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getId()
        )

        then:
        thrown(UnauthorizedException)

        when: "Exception case with LEAVE status"
        mockWriterUserCircleMemberDomainModel.setStatus(CircleMemberStatus.LEAVE)
        this.commentService.delete(
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getId()
        )

        then:
        thrown(BadRequestException)

        when: "Exception case with DROP status"
        mockWriterUserCircleMemberDomainModel.setStatus(CircleMemberStatus.DROP)
        this.commentService.delete(
                ((UserDomainModel) this.mockCommentWriterUserDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getId()
        )

        then:
        thrown(UnauthorizedException)
    }

    def "Comment delete not circle leader of this circle case"() {
        given:
        ((BoardDomainModel) this.mockBoardDomainModel).setCircle((CircleDomainModel) this.mockCircleDomainModel)

        def mockDeletedCommentDomainModel = CommentDomainModel.of(
                ((CommentDomainModel) this.mockCommentDomainModel).getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getContent(),
                true,
                ((CommentDomainModel) this.mockCommentDomainModel).getCreatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getUpdatedAt(),
                ((CommentDomainModel) this.mockCommentDomainModel).getWriter(),
                ((CommentDomainModel) this.mockCommentDomainModel).getPostId()
        )

        def requestCircleLeaderUser = UserDomainModel.of(
                "test request circle leader user id",
                "test-circle-leader-user@cau.ac.kr",
                "test circle leader user name",
                "test1234!",
                "20210000",
                2021,
                Role.LEADER_CIRCLE,
                null,
                UserState.ACTIVE
        )

        def mockRequestCircleLeaderCircleMemberDomainModel = CircleMemberDomainModel.of(
                "test circle member id",
                CircleMemberStatus.MEMBER,
                (CircleDomainModel) this.mockCircleDomainModel,
                ((UserDomainModel) this.mockCircleLeaderUserDomainModel).getId(),
                ((UserDomainModel) this.mockCircleLeaderUserDomainModel).getName(),
                null,
                null
        )

        this.userPort.findById(requestCircleLeaderUser.getId()) >> Optional.of(requestCircleLeaderUser)
        this.commentPort.findById(((CommentDomainModel) this.mockCommentDomainModel).getId()) >> Optional.of(((CommentDomainModel) this.mockCommentDomainModel))
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(((PostDomainModel) this.mockPostDomainModel))

        this.circleMemberPort.findByUserIdAndCircleId(requestCircleLeaderUser.getId(), ((CircleDomainModel) this.mockCircleDomainModel).getId()) >> Optional.of(mockRequestCircleLeaderCircleMemberDomainModel)

        this.commentPort.delete(((CommentDomainModel) this.mockCommentDomainModel).getId()) >> Optional.of(mockDeletedCommentDomainModel)

        when:
        this.commentService.delete(
                requestCircleLeaderUser.getId(),
                ((CommentDomainModel) this.mockCommentDomainModel).getId()
        )

        then:
        thrown(UnauthorizedException)
    }
}
