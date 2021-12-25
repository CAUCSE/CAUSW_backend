package net.causw.application


import net.causw.application.dto.PostAllWithBoardResponseDto
import net.causw.application.dto.PostCreateRequestDto
import net.causw.application.dto.PostResponseDto
import net.causw.application.dto.PostUpdateRequestDto
import net.causw.application.spi.*
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
@PrepareForTest([PostDomainModel.class])
class PostServiceTest extends Specification {
    private PostPort postPort = Mock(PostPort.class)
    private UserPort userPort = Mock(UserPort.class)
    private BoardPort boardPort = Mock(BoardPort.class)
    private CircleMemberPort circleMemberPort = Mock(CircleMemberPort.class)
    private CommentPort commentPort = Mock(CommentPort.class)
    private ChildCommentPort childCommentPort = Mock(ChildCommentPort.class)
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator()
    private PostService postService = new PostService(
            this.postPort,
            this.userPort,
            this.boardPort,
            this.circleMemberPort,
            this.commentPort,
            this.childCommentPort,
            this.validator
    )

    def mockPostDomainModel
    def mockBoardDomainModel
    def mockCircleLeaderUserDomainModel
    def mockCircleDomainModel

    def setup() {
        this.mockBoardDomainModel = BoardDomainModel.of(
                "test board id",
                "test board name",
                "test board description",
                Arrays.asList("PRESIDENT"),
                "category",
                false,
                null
        )

        this.mockPostDomainModel = PostDomainModel.of(
                "test post id",
                "test post title",
                "test post content",
                null,
                false,
                (BoardDomainModel) this.mockBoardDomainModel,
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
    }

    /**
     * Test cases for post findById
     */
    @Test
    def "Post findById normal case"() {
        def requestUserDomainModel = UserDomainModel.of(
                "test user id",
                "test@cau.ac.kr",
                "test user name",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        def circleMemberDomainModel = CircleMemberDomainModel.of(
                "test circle member id",
                CircleMemberStatus.MEMBER,
                (CircleDomainModel) this.mockCircleDomainModel,
                requestUserDomainModel.getId(),
                requestUserDomainModel.getName(),
                null,
                null
        )

        ((PostDomainModel) this.mockPostDomainModel).setWriter(requestUserDomainModel)

        this.userPort.findById(requestUserDomainModel.getId()) >> Optional.of(requestUserDomainModel)
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(this.mockPostDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId(requestUserDomainModel.getId(), ((CircleDomainModel) this.mockCircleDomainModel).getId()) >> Optional.of(circleMemberDomainModel)
        this.commentPort.findByPostId(((PostDomainModel) this.mockPostDomainModel).getId(), 0) >> new PageImpl<CommentDomainModel>(List.of())

        when: "post findById without circle"
        def postFind = this.postService.findById("test user id", ((PostDomainModel) this.mockPostDomainModel).getId())

        then:
        postFind instanceof PostResponseDto
        with(postFind) {
            getTitle() == "test post title"
            getContent() == "test post content"
        }

        when: "post findById with circle"
        ((BoardDomainModel) this.mockBoardDomainModel).setCircle((CircleDomainModel) this.mockCircleDomainModel)
        postFind = this.postService.findById("test user id", ((PostDomainModel) this.mockPostDomainModel).getId())

        then:
        postFind instanceof PostResponseDto
        with(postFind) {
            getTitle() == "test post title"
            getContent() == "test post content"
        }
    }

    /**
     * Test cases for post find all by board id
     */
    @Test
    def "Post find all normal case"() {
        def requestUserDomainModel = UserDomainModel.of(
                "test user id",
                "test@cau.ac.kr",
                "test user name",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        def circleMemberDomainModel = CircleMemberDomainModel.of(
                "test circle member id",
                CircleMemberStatus.MEMBER,
                (CircleDomainModel) this.mockCircleDomainModel,
                requestUserDomainModel.getId(),
                requestUserDomainModel.getName(),
                null,
                null
        )

        ((PostDomainModel) this.mockPostDomainModel).setWriter(requestUserDomainModel)

        this.userPort.findById(requestUserDomainModel.getId()) >> Optional.of(requestUserDomainModel)
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(this.mockPostDomainModel)
        this.boardPort.findById(((BoardDomainModel) this.mockBoardDomainModel).getId()) >> Optional.of(this.mockBoardDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId(requestUserDomainModel.getId(), ((CircleDomainModel) this.mockCircleDomainModel).getId()) >> Optional.of(circleMemberDomainModel)
        this.postPort.findAll(((BoardDomainModel) this.mockBoardDomainModel).getId(), 0) >> new PageImpl<PostDomainModel>(List.of((PostDomainModel)this.mockPostDomainModel))

        when: "post findById without circle"
        def postFind = this.postService.findAll("test user id", "test board id", 0)

        then:
        postFind instanceof PostAllWithBoardResponseDto
        with(postFind) {
            getPost().getContent().get(0).getTitle() == "test post title"
        }

        when: "post findById with circle"
        ((BoardDomainModel) this.mockBoardDomainModel).setCircle((CircleDomainModel) this.mockCircleDomainModel)
        postFind = this.postService.findAll("test user id", "test board id", 0)

        then:
        postFind instanceof PostAllWithBoardResponseDto
        with(postFind) {
            getPost().getContent().get(0).getTitle() == "test post title"
        }
    }

    @Test
    def "Post find all in deleted board"() {
        def requestUserDomainModel = UserDomainModel.of(
                "test user id",
                "test@cau.ac.kr",
                "test user name",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        ((PostDomainModel) this.mockPostDomainModel).setWriter(requestUserDomainModel)

        this.userPort.findById(requestUserDomainModel.getId()) >> Optional.of(requestUserDomainModel)
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(this.mockPostDomainModel)
        this.boardPort.findById(((BoardDomainModel) this.mockBoardDomainModel).getId()) >> Optional.of(this.mockBoardDomainModel)
        this.postPort.findAll(((BoardDomainModel) this.mockBoardDomainModel).getId(), 0) >> new PageImpl<PostDomainModel>(List.of((PostDomainModel)this.mockPostDomainModel))

        when:
        this.mockBoardDomainModel.setIsDeleted(true)
        this.postService.findAll("test user id", "test board id", 0)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Post find all unauthorized case"() {
        def requestUserDomainModel = UserDomainModel.of(
                "test user id",
                "test@cau.ac.kr",
                "test user name",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        def circleMemberDomainModel = CircleMemberDomainModel.of(
                "test circle member id",
                CircleMemberStatus.LEAVE,
                (CircleDomainModel) this.mockCircleDomainModel,
                requestUserDomainModel.getId(),
                requestUserDomainModel.getName(),
                null,
                null
        )

        ((PostDomainModel) this.mockPostDomainModel).setWriter(requestUserDomainModel)

        this.userPort.findById(requestUserDomainModel.getId()) >> Optional.of(requestUserDomainModel)
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(this.mockPostDomainModel)
        this.boardPort.findById(((BoardDomainModel) this.mockBoardDomainModel).getId()) >> Optional.of(this.mockBoardDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId(requestUserDomainModel.getId(), ((CircleDomainModel) this.mockCircleDomainModel).getId()) >> Optional.of(circleMemberDomainModel)
        this.postPort.findAll(((BoardDomainModel) this.mockBoardDomainModel).getId(), 0) >> Page.of((PostDomainModel)this.mockPostDomainModel)

        when: "bad request case - leave"
        ((BoardDomainModel) this.mockBoardDomainModel).setCircle((CircleDomainModel) this.mockCircleDomainModel)
        this.postService.findAll("test user id", "test board id", 0)

        then:
        thrown(BadRequestException)

        when: "unauthorized case - drop"
        circleMemberDomainModel.setStatus(CircleMemberStatus.DROP)
        ((BoardDomainModel) this.mockBoardDomainModel).setCircle((CircleDomainModel) this.mockCircleDomainModel)
        this.postService.findAll("test user id", "test board id", 0)

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for post create
     */
    @Test
    def "Post create normal case"() {
        def mockPostCreateRequestDto = new PostCreateRequestDto(
                "test post title",
                "test post content",
                ((BoardDomainModel) this.mockBoardDomainModel).getId()
        )

        def creatorUserDomainModel = UserDomainModel.of(
                "test user id",
                "test@cau.ac.kr",
                "test user name",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        def circleMemberDomainModel = CircleMemberDomainModel.of(
                "test circle member id",
                CircleMemberStatus.MEMBER,
                (CircleDomainModel) this.mockCircleDomainModel,
                creatorUserDomainModel.getId(),
                creatorUserDomainModel.getName(),
                null,
                null
        )

        ((PostDomainModel) this.mockPostDomainModel).setWriter(creatorUserDomainModel)

        this.userPort.findById(creatorUserDomainModel.getId()) >> Optional.of(creatorUserDomainModel)
        this.boardPort.findById(((BoardDomainModel) this.mockBoardDomainModel).getId()) >> Optional.of((BoardDomainModel) this.mockBoardDomainModel)
        this.postPort.create((PostDomainModel) this.mockPostDomainModel) >> this.mockPostDomainModel
        this.circleMemberPort.findByUserIdAndCircleId(creatorUserDomainModel.getId(), ((CircleDomainModel) this.mockCircleDomainModel).getId()) >> Optional.of(circleMemberDomainModel)

        when: "create post without circle"
        PowerMockito.mockStatic(PostDomainModel.class)
        PowerMockito.when(PostDomainModel.of(
                mockPostCreateRequestDto.getTitle(),
                mockPostCreateRequestDto.getContent(),
                (UserDomainModel) creatorUserDomainModel,
                (BoardDomainModel) this.mockBoardDomainModel
        )).thenReturn((PostDomainModel) this.mockPostDomainModel)
        def postCreate = this.postService.create("test user id", mockPostCreateRequestDto)

        then:
        postCreate instanceof PostResponseDto
        with(postCreate) {
            getTitle() == "test post title"
            getContent() == "test post content"
        }

        when: "create post with circle"
        ((BoardDomainModel) this.mockBoardDomainModel).setCreateRoleList(Arrays.asList("LEADER_CIRCLE"))
        ((BoardDomainModel) this.mockBoardDomainModel).setCircle((CircleDomainModel) this.mockCircleDomainModel)
        creatorUserDomainModel.setRole(Role.LEADER_CIRCLE)
        PowerMockito.mockStatic(PostDomainModel.class)
        PowerMockito.when(PostDomainModel.of(
                mockPostCreateRequestDto.getTitle(),
                mockPostCreateRequestDto.getContent(),
                (UserDomainModel) creatorUserDomainModel,
                (BoardDomainModel) this.mockBoardDomainModel
        )).thenReturn((PostDomainModel) this.mockPostDomainModel)
        postCreate = this.postService.create("test user id", mockPostCreateRequestDto)

        then:
        postCreate instanceof PostResponseDto
        with(postCreate) {
            getTitle() == "test post title"
            getContent() == "test post content"
        }
    }

    @Test
    def "Post create unauthorized case"() {
        def mockPostCreateRequestDto = new PostCreateRequestDto(
                "test post title",
                "test post content",
                ((BoardDomainModel) this.mockBoardDomainModel).getId()
        )

        def creatorUserDomainModel = UserDomainModel.of(
                "test user id",
                "test@cau.ac.kr",
                "test user name",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        def circleMemberDomainModel = CircleMemberDomainModel.of(
                "test circle member id",
                CircleMemberStatus.AWAIT,
                (CircleDomainModel) this.mockCircleDomainModel,
                creatorUserDomainModel.getId(),
                creatorUserDomainModel.getName(),
                null,
                null
        )

        ((PostDomainModel) this.mockPostDomainModel).setWriter(creatorUserDomainModel)

        this.userPort.findById(creatorUserDomainModel.getId()) >> Optional.of(creatorUserDomainModel)
        this.boardPort.findById(((BoardDomainModel) this.mockBoardDomainModel).getId()) >> Optional.of((BoardDomainModel) this.mockBoardDomainModel)
        this.postPort.create((PostDomainModel) this.mockPostDomainModel) >> this.mockPostDomainModel
        this.circleMemberPort.findByUserIdAndCircleId(creatorUserDomainModel.getId(), ((CircleDomainModel) this.mockCircleDomainModel).getId()) >> Optional.of(circleMemberDomainModel)

        when: "creator user is COMMON when create role is PRESIDENT"
        PowerMockito.mockStatic(PostDomainModel.class)
        PowerMockito.when(PostDomainModel.of(
                mockPostCreateRequestDto.getTitle(),
                mockPostCreateRequestDto.getContent(),
                (UserDomainModel) creatorUserDomainModel,
                (BoardDomainModel) this.mockBoardDomainModel
        )).thenReturn((PostDomainModel) this.mockPostDomainModel)
        this.postService.create("test user id", mockPostCreateRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "creator user is not a member of circle"
        creatorUserDomainModel.setRole(Role.PRESIDENT)
        ((BoardDomainModel) this.mockBoardDomainModel).setCircle((CircleDomainModel) this.mockCircleDomainModel)
        creatorUserDomainModel.setRole(Role.LEADER_CIRCLE)
        PowerMockito.mockStatic(PostDomainModel.class)
        PowerMockito.when(PostDomainModel.of(
                mockPostCreateRequestDto.getTitle(),
                mockPostCreateRequestDto.getContent(),
                (UserDomainModel) creatorUserDomainModel,
                (BoardDomainModel) this.mockBoardDomainModel
        )).thenReturn((PostDomainModel) this.mockPostDomainModel)
        this.postService.create("test user id", mockPostCreateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "Post create target deleted case"() {
        def mockPostCreateRequestDto = new PostCreateRequestDto(
                "test post title",
                "test post content",
                ((BoardDomainModel) this.mockBoardDomainModel).getId()
        )

        def creatorUserDomainModel = UserDomainModel.of(
                "test user id",
                "test@cau.ac.kr",
                "test user name",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        def circleMemberDomainModel = CircleMemberDomainModel.of(
                "test circle member id",
                CircleMemberStatus.MEMBER,
                (CircleDomainModel) this.mockCircleDomainModel,
                creatorUserDomainModel.getId(),
                creatorUserDomainModel.getName(),
                null,
                null
        )

        ((PostDomainModel) this.mockPostDomainModel).setWriter(creatorUserDomainModel)

        this.userPort.findById(creatorUserDomainModel.getId()) >> Optional.of(creatorUserDomainModel)
        this.boardPort.findById(((BoardDomainModel) this.mockBoardDomainModel).getId()) >> Optional.of((BoardDomainModel) this.mockBoardDomainModel)
        this.postPort.create((PostDomainModel) this.mockPostDomainModel) >> this.mockPostDomainModel
        this.circleMemberPort.findByUserIdAndCircleId(creatorUserDomainModel.getId(), ((CircleDomainModel) this.mockCircleDomainModel).getId()) >> Optional.of(circleMemberDomainModel)

        when: "target board is deleted"
        ((BoardDomainModel) this.mockBoardDomainModel).setIsDeleted(true)
        PowerMockito.mockStatic(PostDomainModel.class)
        PowerMockito.when(PostDomainModel.of(
                mockPostCreateRequestDto.getTitle(),
                mockPostCreateRequestDto.getContent(),
                (UserDomainModel) creatorUserDomainModel,
                (BoardDomainModel) this.mockBoardDomainModel
        )).thenReturn((PostDomainModel) this.mockPostDomainModel)
        this.postService.create("test user id", mockPostCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "target circle is deleted"
        ((CircleDomainModel) this.mockCircleDomainModel).setIsDeleted(true)
        ((BoardDomainModel) this.mockBoardDomainModel).setIsDeleted(false)
        ((BoardDomainModel) this.mockBoardDomainModel).setCreateRoleList(Arrays.asList("LEADER_CIRCLE"))
        ((BoardDomainModel) this.mockBoardDomainModel).setCircle((CircleDomainModel) this.mockCircleDomainModel)
        creatorUserDomainModel.setRole(Role.LEADER_CIRCLE)
        PowerMockito.mockStatic(PostDomainModel.class)
        PowerMockito.when(PostDomainModel.of(
                mockPostCreateRequestDto.getTitle(),
                mockPostCreateRequestDto.getContent(),
                (UserDomainModel) creatorUserDomainModel,
                (BoardDomainModel) this.mockBoardDomainModel
        )).thenReturn((PostDomainModel) this.mockPostDomainModel)
        this.postService.create("test user id", mockPostCreateRequestDto)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Post create invalid data case"() {
        def mockPostCreateRequestDto = new PostCreateRequestDto(
                "test post title",
                "test post content",
                ((BoardDomainModel) this.mockBoardDomainModel).getId()
        )

        def creatorUserDomainModel = UserDomainModel.of(
                "test user id",
                "test@cau.ac.kr",
                "test user name",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        ((PostDomainModel) this.mockPostDomainModel).setWriter(creatorUserDomainModel)

        this.userPort.findById(creatorUserDomainModel.getId()) >> Optional.of(creatorUserDomainModel)
        this.boardPort.findById(((BoardDomainModel) this.mockBoardDomainModel).getId()) >> Optional.of((BoardDomainModel) this.mockBoardDomainModel)
        this.postPort.create((PostDomainModel) this.mockPostDomainModel) >> this.mockPostDomainModel

        when: "title is blank"
        mockPostCreateRequestDto.setTitle("")
        ((PostDomainModel) this.mockPostDomainModel).setTitle("")
        PowerMockito.mockStatic(PostDomainModel.class)
        PowerMockito.when(PostDomainModel.of(
                mockPostCreateRequestDto.getTitle(),
                mockPostCreateRequestDto.getContent(),
                (UserDomainModel) creatorUserDomainModel,
                (BoardDomainModel) this.mockBoardDomainModel
        )).thenReturn((PostDomainModel) this.mockPostDomainModel)
        this.postService.create("test user id", mockPostCreateRequestDto)

        then:
        thrown(ConstraintViolationException)
    }

    /**
     * Test cases for post update
     */
    @Test
    def "Post update normal case"() {
        given:
        def postId = "test-post-id"
        def currentTitle = "test-title-1"
        def targetTitle = "test-title-2"

        def writerUserDomainModel = UserDomainModel.of(
                "test user id",
                "test@cau.ac.kr",
                "test user name",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        def mockOriginPostDomainModel = PostDomainModel.of(
                postId,
                currentTitle,
                "test",
                writerUserDomainModel,
                false,
                (BoardDomainModel) mockBoardDomainModel,
                null,
                null
        )

        def mockUpdatedPostDomainModel = PostDomainModel.of(
                postId,
                targetTitle,
                "test",
                writerUserDomainModel,
                false,
                (BoardDomainModel) mockBoardDomainModel,
                null,
                null
        )

        def postUpdateRequestDto = new PostUpdateRequestDto(targetTitle, "test")

        this.userPort.findById(writerUserDomainModel.getId()) >> Optional.of(writerUserDomainModel)
        this.postPort.findById(postId) >> Optional.of(mockOriginPostDomainModel)

        this.postPort.update(postId, mockOriginPostDomainModel) >> Optional.of(mockUpdatedPostDomainModel)
        this.commentPort.findByPostId(postId, 0) >> new PageImpl<CommentDomainModel>(List.of())

        when:
        def postUpdateResponse = this.postService.update(writerUserDomainModel.getId(), postId, postUpdateRequestDto)

        then:
        postUpdateResponse instanceof PostResponseDto
        postUpdateResponse.getTitle() == targetTitle
    }

    @Test
    def "Post update invalid role case"() {
        given:
        def postId = "test-post-id"
        def currentTitle = "test-title-1"
        def targetTitle = "test-title-2"

        def presidentUserDomainModel = UserDomainModel.of(
                "test president user id",
                "test-president@cau.ac.kr",
                "test president user name",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        def writerUserDomainModel = UserDomainModel.of(
                "test user id",
                "test@cau.ac.kr",
                "test user name",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        def mockOriginPostDomainModel = PostDomainModel.of(
                postId,
                currentTitle,
                "test",
                writerUserDomainModel,
                false,
                (BoardDomainModel) mockBoardDomainModel,
                null,
                null
        )

        def mockUpdatedPostDomainModel = PostDomainModel.of(
                postId,
                targetTitle,
                "test",
                writerUserDomainModel,
                false,
                (BoardDomainModel) mockBoardDomainModel,
                null,
                null
        )

        def postUpdateRequestDto = new PostUpdateRequestDto(targetTitle, "test")

        this.userPort.findById(presidentUserDomainModel.getId()) >> Optional.of(presidentUserDomainModel)
        this.postPort.findById(postId) >> Optional.of(mockOriginPostDomainModel)

        this.postPort.update(postId, mockOriginPostDomainModel) >> Optional.of(mockUpdatedPostDomainModel)
        this.commentPort.findByPostId(postId, 0) >> new PageImpl<CommentDomainModel>(List.of())

        when:
        this.postService.update(presidentUserDomainModel.getId(), postId, postUpdateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "Post update target deleted case"() {
        given:
        def postId = "test-post-id"
        def currentTitle = "test-title-1"
        def targetTitle = "test-title-2"

        def writerUserDomainModel = UserDomainModel.of(
                "test user id",
                "test@cau.ac.kr",
                "test user name",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        def mockOriginPostDomainModel = PostDomainModel.of(
                postId,
                currentTitle,
                "test",
                writerUserDomainModel,
                true,
                (BoardDomainModel) mockBoardDomainModel,
                null,
                null
        )

        def mockUpdatedPostDomainModel = PostDomainModel.of(
                postId,
                targetTitle,
                "test",
                writerUserDomainModel,
                false,
                (BoardDomainModel) mockBoardDomainModel,
                null,
                null
        )

        def postUpdateRequestDto = new PostUpdateRequestDto(targetTitle, "test")

        this.userPort.findById(writerUserDomainModel.getId()) >> Optional.of(writerUserDomainModel)
        this.postPort.findById(postId) >> Optional.of(mockOriginPostDomainModel)

        this.postPort.update(postId, mockOriginPostDomainModel) >> Optional.of(mockUpdatedPostDomainModel)
        this.commentPort.findByPostId(postId, 0) >> new PageImpl<CommentDomainModel>(List.of())

        when:
        this.postService.update(writerUserDomainModel.getId(), postId, postUpdateRequestDto)

        then:
        thrown(BadRequestException)
    }

    /**
     * Test cases for post delete
     */
    @Test
    def "Post delete normal without circle case"() {
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
        def writerUser = UserDomainModel.of(
                "test writer user id",
                "test-writer-user@cau.ac.kr",
                "test writer user name",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        ((PostDomainModel) this.mockPostDomainModel).setWriter(writerUser)

        def deletedPostDomainModel = PostDomainModel.of(
                ((PostDomainModel) this.mockPostDomainModel).getId(),
                ((PostDomainModel) this.mockPostDomainModel).getTitle(),
                ((PostDomainModel) this.mockPostDomainModel).getContent(),
                ((PostDomainModel) this.mockPostDomainModel).getWriter(),
                true,
                ((PostDomainModel) this.mockPostDomainModel).getBoard(),
                ((PostDomainModel) this.mockPostDomainModel).getCreatedAt(),
                ((PostDomainModel) this.mockPostDomainModel).getUpdatedAt()
        )


        this.userPort.findById(requestPresidentUser.getId()) >> Optional.of(requestPresidentUser)
        this.userPort.findById(writerUser.getId()) >> Optional.of(writerUser)
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(((PostDomainModel) this.mockPostDomainModel))

        this.postPort.delete(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(deletedPostDomainModel)

        when: "delete post with president user"
        def postResponse = this.postService.delete(requestPresidentUser.getId(), ((PostDomainModel) this.mockPostDomainModel).getId())

        then:
        postResponse instanceof PostResponseDto
        with(postResponse) {
            postResponse.getIsDeleted()
        }

        when: "delete post with writer user"
        postResponse = this.postService.delete(writerUser.getId(), ((PostDomainModel) this.mockPostDomainModel).getId())

        then:
        postResponse instanceof PostResponseDto
        with(postResponse) {
            postResponse.getIsDeleted()
        }
    }

    @Test
    def "Post delete normal with circle case"() {
        given:
        def writerUser = UserDomainModel.of(
                "test writer user id",
                "test-writer-user@cau.ac.kr",
                "test writer user name",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        ((BoardDomainModel) this.mockBoardDomainModel).setCircle((CircleDomainModel) this.mockCircleDomainModel)
        ((PostDomainModel) this.mockPostDomainModel).setWriter(writerUser)

        def deletedPostDomainModel = PostDomainModel.of(
                ((PostDomainModel) this.mockPostDomainModel).getId(),
                ((PostDomainModel) this.mockPostDomainModel).getTitle(),
                ((PostDomainModel) this.mockPostDomainModel).getContent(),
                ((PostDomainModel) this.mockPostDomainModel).getWriter(),
                true,
                ((PostDomainModel) this.mockPostDomainModel).getBoard(),
                ((PostDomainModel) this.mockPostDomainModel).getCreatedAt(),
                ((PostDomainModel) this.mockPostDomainModel).getUpdatedAt()
        )

        def mockWriterUserCircleMemberDomainModel = CircleMemberDomainModel.of(
                "test circle member id",
                CircleMemberStatus.MEMBER,
                (CircleDomainModel) this.mockCircleDomainModel,
                writerUser.getId(),
                writerUser.getName(),
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

        this.userPort.findById(((UserDomainModel) this.mockCircleLeaderUserDomainModel).getId()) >> Optional.of(((UserDomainModel) this.mockCircleLeaderUserDomainModel))
        this.userPort.findById(writerUser.getId()) >> Optional.of(writerUser)
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(((PostDomainModel) this.mockPostDomainModel))

        this.circleMemberPort.findByUserIdAndCircleId(writerUser.getId(), ((CircleDomainModel) this.mockCircleDomainModel).getId()) >> Optional.of(mockWriterUserCircleMemberDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId(((UserDomainModel) this.mockCircleLeaderUserDomainModel).getId(), ((CircleDomainModel) this.mockCircleDomainModel).getId()) >> Optional.of(mockCircleLeaderCircleMemberDomainModel)

        this.postPort.delete(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(deletedPostDomainModel)

        when: "delete post with circle leader user"
        def postResponse = this.postService.delete(((UserDomainModel) this.mockCircleLeaderUserDomainModel).getId(), ((PostDomainModel) this.mockPostDomainModel).getId())

        then:
        postResponse instanceof PostResponseDto
        with(postResponse) {
            postResponse.getIsDeleted()
        }

        when: "delete post with writer user"
        postResponse = this.postService.delete(writerUser.getId(), ((PostDomainModel) this.mockPostDomainModel).getId())

        then:
        postResponse instanceof PostResponseDto
        with(postResponse) {
            postResponse.getIsDeleted()
        }
    }

    @Test
    def "Post delete target already deleted case"() {
        given:
        def writerUser = UserDomainModel.of(
                "test writer user id",
                "test-writer-user@cau.ac.kr",
                "test writer user name",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        ((PostDomainModel) this.mockPostDomainModel).setWriter(writerUser)
        ((PostDomainModel) this.mockPostDomainModel).setIsDeleted(true)

        def deletedPostDomainModel = PostDomainModel.of(
                ((PostDomainModel) this.mockPostDomainModel).getId(),
                ((PostDomainModel) this.mockPostDomainModel).getTitle(),
                ((PostDomainModel) this.mockPostDomainModel).getContent(),
                ((PostDomainModel) this.mockPostDomainModel).getWriter(),
                true,
                ((PostDomainModel) this.mockPostDomainModel).getBoard(),
                ((PostDomainModel) this.mockPostDomainModel).getCreatedAt(),
                ((PostDomainModel) this.mockPostDomainModel).getUpdatedAt()
        )


        this.userPort.findById(writerUser.getId()) >> Optional.of(writerUser)
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(((PostDomainModel) this.mockPostDomainModel))

        this.postPort.delete(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(deletedPostDomainModel)

        when:
        this.postService.delete(writerUser.getId(), ((PostDomainModel) this.mockPostDomainModel).getId())

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Post delete not circle member case"() {
        given:
        def writerUser = UserDomainModel.of(
                "test writer user id",
                "test-writer-user@cau.ac.kr",
                "test writer user name",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        ((BoardDomainModel) this.mockBoardDomainModel).setCircle((CircleDomainModel) this.mockCircleDomainModel)
        ((PostDomainModel) this.mockPostDomainModel).setWriter(writerUser)

        def deletedPostDomainModel = PostDomainModel.of(
                ((PostDomainModel) this.mockPostDomainModel).getId(),
                ((PostDomainModel) this.mockPostDomainModel).getTitle(),
                ((PostDomainModel) this.mockPostDomainModel).getContent(),
                ((PostDomainModel) this.mockPostDomainModel).getWriter(),
                true,
                ((PostDomainModel) this.mockPostDomainModel).getBoard(),
                ((PostDomainModel) this.mockPostDomainModel).getCreatedAt(),
                ((PostDomainModel) this.mockPostDomainModel).getUpdatedAt()
        )

        this.userPort.findById(writerUser.getId()) >> Optional.of(writerUser)
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(((PostDomainModel) this.mockPostDomainModel))

        this.circleMemberPort.findByUserIdAndCircleId(writerUser.getId(), ((CircleDomainModel) this.mockCircleDomainModel).getId()) >> Optional.empty()

        this.postPort.delete(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(deletedPostDomainModel)

        when:
        this.postService.delete(writerUser.getId(), ((PostDomainModel) this.mockPostDomainModel).getId())

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "Post delete not circle member status MEMBER case"() {
        given:
        def writerUser = UserDomainModel.of(
                "test writer user id",
                "test-writer-user@cau.ac.kr",
                "test writer user name",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        ((BoardDomainModel) this.mockBoardDomainModel).setCircle((CircleDomainModel) this.mockCircleDomainModel)
        ((PostDomainModel) this.mockPostDomainModel).setWriter(writerUser)

        def deletedPostDomainModel = PostDomainModel.of(
                ((PostDomainModel) this.mockPostDomainModel).getId(),
                ((PostDomainModel) this.mockPostDomainModel).getTitle(),
                ((PostDomainModel) this.mockPostDomainModel).getContent(),
                ((PostDomainModel) this.mockPostDomainModel).getWriter(),
                true,
                ((PostDomainModel) this.mockPostDomainModel).getBoard(),
                ((PostDomainModel) this.mockPostDomainModel).getCreatedAt(),
                ((PostDomainModel) this.mockPostDomainModel).getUpdatedAt()
        )

        def mockWriterUserCircleMemberDomainModel = CircleMemberDomainModel.of(
                "test circle member id",
                CircleMemberStatus.MEMBER,
                (CircleDomainModel) this.mockCircleDomainModel,
                writerUser.getId(),
                writerUser.getName(),
                null,
                null
        )

        this.userPort.findById(writerUser.getId()) >> Optional.of(writerUser)
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(((PostDomainModel) this.mockPostDomainModel))

        this.circleMemberPort.findByUserIdAndCircleId(writerUser.getId(), ((CircleDomainModel) this.mockCircleDomainModel).getId()) >> Optional.of(mockWriterUserCircleMemberDomainModel)

        this.postPort.delete(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(deletedPostDomainModel)

        when: "Exception case with AWAIT status"
        mockWriterUserCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.postService.delete(writerUser.getId(), ((PostDomainModel) this.mockPostDomainModel).getId())

        then:
        thrown(BadRequestException)

        when: "Exception case with REJECT status"
        mockWriterUserCircleMemberDomainModel.setStatus(CircleMemberStatus.REJECT)
        this.postService.delete(writerUser.getId(), ((PostDomainModel) this.mockPostDomainModel).getId())

        then:
        thrown(UnauthorizedException)

        when: "Exception case with LEAVE status"
        mockWriterUserCircleMemberDomainModel.setStatus(CircleMemberStatus.LEAVE)
        this.postService.delete(writerUser.getId(), ((PostDomainModel) this.mockPostDomainModel).getId())

        then:
        thrown(BadRequestException)

        when: "Exception case with DROP status"
        mockWriterUserCircleMemberDomainModel.setStatus(CircleMemberStatus.DROP)
        this.postService.delete(writerUser.getId(), ((PostDomainModel) this.mockPostDomainModel).getId())

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "Post delete not circle leader of this circle case"() {
        given:
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

        def writerUser = UserDomainModel.of(
                "test writer user id",
                "test-writer-user@cau.ac.kr",
                "test writer user name",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        ((BoardDomainModel) this.mockBoardDomainModel).setCircle((CircleDomainModel) this.mockCircleDomainModel)
        ((PostDomainModel) this.mockPostDomainModel).setWriter(writerUser)

        def deletedPostDomainModel = PostDomainModel.of(
                ((PostDomainModel) this.mockPostDomainModel).getId(),
                ((PostDomainModel) this.mockPostDomainModel).getTitle(),
                ((PostDomainModel) this.mockPostDomainModel).getContent(),
                ((PostDomainModel) this.mockPostDomainModel).getWriter(),
                true,
                ((PostDomainModel) this.mockPostDomainModel).getBoard(),
                ((PostDomainModel) this.mockPostDomainModel).getCreatedAt(),
                ((PostDomainModel) this.mockPostDomainModel).getUpdatedAt()
        )

        def mockRequestUserCircleMemberDomainModel = CircleMemberDomainModel.of(
                "test circle member id",
                CircleMemberStatus.MEMBER,
                (CircleDomainModel) this.mockCircleDomainModel,
                requestCircleLeaderUser.getId(),
                requestCircleLeaderUser.getName(),
                null,
                null
        )

        this.userPort.findById(requestCircleLeaderUser.getId()) >> Optional.of(requestCircleLeaderUser)
        this.postPort.findById(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(((PostDomainModel) this.mockPostDomainModel))

        this.circleMemberPort.findByUserIdAndCircleId(requestCircleLeaderUser.getId(), ((CircleDomainModel) this.mockCircleDomainModel).getId()) >> Optional.of(mockRequestUserCircleMemberDomainModel)

        this.postPort.delete(((PostDomainModel) this.mockPostDomainModel).getId()) >> Optional.of(deletedPostDomainModel)

        when:
        this.postService.delete(requestCircleLeaderUser.getId(), ((PostDomainModel) this.mockPostDomainModel).getId())

        then:
        thrown(UnauthorizedException)
    }
}
