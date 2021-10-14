package net.causw.application


import net.causw.application.dto.PostCreateRequestDto
import net.causw.application.dto.PostResponseDto
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
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator()
    private PostService postService = new PostService(
            this.postPort,
            this.userPort,
            this.boardPort,
            this.circleMemberPort,
            this.commentPort,
            this.validator
    )

    def mockPostDomainModel
    def mockBoardDomainModel
    def mockCircleLeaderUserDomainModel
    def mockCircleDomainModel

    def setup() {
        this.mockBoardDomainModel = BoardDomainModel.of(
                "test board id",
                "test board id",
                "test board description",
                Arrays.asList("PRESIDENT"),
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
        this.commentPort.findByPostId(((PostDomainModel) this.mockPostDomainModel).getId()) >> new ArrayList<CommentDomainModel>()

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
        this.postPort.findAll(((BoardDomainModel) this.mockBoardDomainModel).getId()) >> List.of(this.mockPostDomainModel)

        when: "post findById without circle"
        def postFind = this.postService.findAll("test user id", "test board id")

        then:
        postFind instanceof List<PostResponseDto>
        with(postFind) {
            get(0).getTitle() == "test post title"
            get(0).getContent() == "test post content"
        }

        when: "post findById with circle"
        ((BoardDomainModel) this.mockBoardDomainModel).setCircle((CircleDomainModel) this.mockCircleDomainModel)
        postFind = this.postService.findAll("test user id", "test board id")

        then:
        postFind instanceof List<PostResponseDto>
        with(postFind) {
            get(0).getTitle() == "test post title"
            get(0).getContent() == "test post content"
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
        this.postPort.findAll(((BoardDomainModel) this.mockBoardDomainModel).getId()) >> List.of(this.mockPostDomainModel)

        when:
        this.mockBoardDomainModel.setIsDeleted(true)
        this.postService.findAll("test user id", "test board id")

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
        this.postPort.findAll(((BoardDomainModel) this.mockBoardDomainModel).getId()) >> List.of(this.mockPostDomainModel)

        when: "bad request case - leave"
        ((BoardDomainModel) this.mockBoardDomainModel).setCircle((CircleDomainModel) this.mockCircleDomainModel)
        this.postService.findAll("test user id", "test board id")

        then:
        thrown(BadRequestException)

        when: "unauthorized case - drop"
        circleMemberDomainModel.setStatus(CircleMemberStatus.DROP)
        ((BoardDomainModel) this.mockBoardDomainModel).setCircle((CircleDomainModel) this.mockCircleDomainModel)
        this.postService.findAll("test user id", "test board id")

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

        when: "content blank"
        mockPostCreateRequestDto.setTitle("test post title")
        mockPostCreateRequestDto.setContent("")
        ((PostDomainModel) this.mockPostDomainModel).setTitle("test post title")
        ((PostDomainModel) this.mockPostDomainModel).setContent("")
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
}
