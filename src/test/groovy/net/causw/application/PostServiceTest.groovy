package net.causw.application

import net.causw.application.dto.post.BoardPostsResponseDto
import net.causw.application.dto.post.PostCreateRequestDto
import net.causw.application.dto.post.PostResponseDto
import net.causw.application.dto.post.PostUpdateRequestDto
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
    private FavoriteBoardPort favoriteBoardPort = Mock(FavoriteBoardPort.class)
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator()
    private PostService postService = new PostService(
            this.postPort,
            this.userPort,
            this.boardPort,
            this.circleMemberPort,
            this.commentPort,
            this.childCommentPort,
            this.favoriteBoardPort,
            this.validator
    )

    def mockAdminDomainModel
    def mockUserDomainModel
    def mockPostDomainModel
    def mockBoardDomainModel
    def mockCircleLeaderUserDomainModel
    def mockCircleDomainModel
    def mockCircleMemberDomainModel

    def setup() {
        this.mockAdminDomainModel = UserDomainModel.of(
                "test1",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.ADMIN,
                null,
                UserState.ACTIVE
        )

        this.mockUserDomainModel = UserDomainModel.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        this.mockBoardDomainModel = BoardDomainModel.of(
                "test board id",
                "test board name",
                "test board description",
                Arrays.asList("PRESIDENT", "COMMON"),
                "category",
                false,
                null
        )

        this.mockPostDomainModel = PostDomainModel.of(
                "test post id",
                "test post title",
                "test post content",
                (UserDomainModel) this.mockUserDomainModel,
                false,
                (BoardDomainModel) this.mockBoardDomainModel,
                null,
                null,
                List.of()
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
                "test leader user id",
                "test leader user name",
                null,
                null
        )
    }

    /**
     * Test cases for post find by id
     */
    @Test
    def "Post find by id normal case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)
        this.commentPort.findByPostId("test post id", 0) >> new PageImpl<CommentDomainModel>(List.of())

        when: "without circle"
        def postResponseDto = this.postService.findById("test", "test post id")

        then:
        postResponseDto instanceof PostResponseDto
        with(postResponseDto) {
            getTitle() == "test post title"
            getContent() == "test post content"
        }

        when: "with circle"
        this.circleMemberPort.findByUserIdAndCircleId("test", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.mockBoardDomainModel.setCircle((CircleDomainModel) this.mockCircleDomainModel)
        postResponseDto = this.postService.findById("test", "test post id")

        then:
        postResponseDto instanceof PostResponseDto
        with(postResponseDto) {
            getTitle() == "test post title"
            getContent() == "test post content"
        }

        when: "with circle for admin"
        this.userPort.findById("test1") >> Optional.of(this.mockAdminDomainModel)
        this.mockBoardDomainModel.setCircle((CircleDomainModel) this.mockCircleDomainModel)
        postResponseDto = this.postService.findById("test1", "test post id")

        then:
        postResponseDto instanceof PostResponseDto
        with(postResponseDto) {
            getTitle() == "test post title"
            getContent() == "test post content"
        }
    }

    @Test
    def "Post find by id deleted case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        when: "deleted board"
        this.mockBoardDomainModel.setIsDeleted(true)
        this.postService.findById("test", "test post id")

        then:
        thrown(BadRequestException)

        when: "deleted post"
        this.mockBoardDomainModel.setIsDeleted(false)
        this.mockPostDomainModel.setIsDeleted(true)
        this.postService.findById("test", "test post id")

        then:
        thrown(BadRequestException)

        when: "deleted circle"
        this.circleMemberPort.findByUserIdAndCircleId("test", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.mockPostDomainModel.setIsDeleted(false)
        this.mockCircleDomainModel.setIsDeleted(true)
        this.mockBoardDomainModel.setCircle((CircleDomainModel) this.mockCircleDomainModel)
        this.postService.findById("test", "test post id")

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Post find by id unauthorized case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        this.mockBoardDomainModel.setCircle((CircleDomainModel) this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)

        when: "circle member is await"
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.postService.findById("test", "test post id")

        then:
        thrown(BadRequestException)

        when: "circle member is blocked"
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.REJECT)
        this.postService.findById("test", "test post id")

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for post find all by board id
     */
    @Test
    def "Post find all normal case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.boardPort.findById("test board id") >> Optional.of(this.mockBoardDomainModel)

        this.favoriteBoardPort.findByUserId("test") >> List.of()
        this.postPort.findAll("test board id", 0) >> new PageImpl<PostDomainModel>(List.of((PostDomainModel) this.mockPostDomainModel))

        when: "without circle"
        def boardPostsResponseDto = this.postService.findAll("test", "test board id", 0)

        then:
        boardPostsResponseDto instanceof BoardPostsResponseDto
        with(boardPostsResponseDto) {
            getPost().getContent().get(0).getTitle() == "test post title"
        }

        when: "with circle"
        this.circleMemberPort.findByUserIdAndCircleId("test", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.mockBoardDomainModel.setCircle((CircleDomainModel) this.mockCircleDomainModel)
        boardPostsResponseDto = this.postService.findAll("test", "test board id", 0)

        then:
        boardPostsResponseDto instanceof BoardPostsResponseDto
        with(boardPostsResponseDto) {
            getPost().getContent().get(0).getTitle() == "test post title"
        }

        when: "with circle for admin"
        this.userPort.findById("test1") >> Optional.of(this.mockAdminDomainModel)
        this.favoriteBoardPort.findByUserId("test1") >> List.of()
        this.mockBoardDomainModel.setCircle((CircleDomainModel) this.mockCircleDomainModel)
        boardPostsResponseDto = this.postService.findAll("test1", "test board id", 0)

        then:
        boardPostsResponseDto instanceof BoardPostsResponseDto
        with(boardPostsResponseDto) {
            getPost().getContent().get(0).getTitle() == "test post title"
        }
    }

    @Test
    def "Post find all deleted case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.boardPort.findById("test board id") >> Optional.of(this.mockBoardDomainModel)

        when: "deleted board"
        this.mockBoardDomainModel.setIsDeleted(true)
        this.postService.findAll("test", "test board id", 0)

        then:
        thrown(BadRequestException)

        when: "deleted circle"
        this.mockBoardDomainModel.setIsDeleted(false)
        this.mockCircleDomainModel.setIsDeleted(true)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.mockBoardDomainModel.setCircle((CircleDomainModel) this.mockCircleDomainModel)
        this.postService.findAll("test", "test board id", 0)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Post find all unauthorized case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.boardPort.findById("test board id") >> Optional.of(this.mockBoardDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)

        when: "circle member is await"
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.mockBoardDomainModel.setCircle((CircleDomainModel) this.mockCircleDomainModel)
        this.postService.findAll("test", "test board id", 0)

        then:
        thrown(BadRequestException)

        when: "circle member is blocked"
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.REJECT)
        this.mockBoardDomainModel.setCircle((CircleDomainModel) this.mockCircleDomainModel)
        this.postService.findAll("test", "test board id", 0)

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for post search
     */
    @Test
    def "Post search normal case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.boardPort.findById(((BoardDomainModel) this.mockBoardDomainModel).getId()) >> Optional.of(this.mockBoardDomainModel)

        this.favoriteBoardPort.findByUserId("test") >> List.of()
        this.postPort.search(SearchOption.TITLE, "keyword", 0) >> new PageImpl<PostDomainModel>(List.of((PostDomainModel) this.mockPostDomainModel))

        when: "without circle"
        def boardPostsResponseDto = this.postService.search("test", "test board id", "title", "keyword", 0)

        then:
        boardPostsResponseDto instanceof BoardPostsResponseDto
        with(boardPostsResponseDto) {
            getPost().getContent().get(0).getTitle() == "test post title"
        }

        when: "with circle"
        this.circleMemberPort.findByUserIdAndCircleId("test", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.mockBoardDomainModel.setCircle((CircleDomainModel) this.mockCircleDomainModel)
        boardPostsResponseDto = this.postService.search("test", "test board id", "title", "keyword", 0)

        then:
        boardPostsResponseDto instanceof BoardPostsResponseDto
        with(boardPostsResponseDto) {
            getPost().getContent().get(0).getTitle() == "test post title"
        }

        when: "with circle for admin"
        this.userPort.findById("test1") >> Optional.of(this.mockAdminDomainModel)
        this.mockBoardDomainModel.setCircle((CircleDomainModel) this.mockCircleDomainModel)
        boardPostsResponseDto = this.postService.search("test", "test board id", "title", "keyword", 0)

        then:
        boardPostsResponseDto instanceof BoardPostsResponseDto
        with(boardPostsResponseDto) {
            getPost().getContent().get(0).getTitle() == "test post title"
        }
    }

    @Test
    def "Post search deleted case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.boardPort.findById("test board id") >> Optional.of(this.mockBoardDomainModel)

        when: "deleted board"
        this.mockBoardDomainModel.setIsDeleted(true)
        this.postService.search("test", "test board id", "title", "keyword", 0)

        then:
        thrown(BadRequestException)

        when: "deleted circle"
        this.mockBoardDomainModel.setIsDeleted(false)
        this.mockCircleDomainModel.setIsDeleted(true)
        this.mockBoardDomainModel.setCircle((CircleDomainModel) this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.postService.search("test", "test board id", "title", "keyword", 0)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Post search invalid parameter"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.boardPort.findById("test board id") >> Optional.of(this.mockBoardDomainModel)

        when:
        this.postService.search("test", "test board id", "invalid", "keyword", 0)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Post search unauthorized case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.boardPort.findById("test board id") >> Optional.of(this.mockBoardDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)

        when: "circle member is await"
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.mockBoardDomainModel.setCircle((CircleDomainModel) this.mockCircleDomainModel)
        this.postService.search("test", "test board id", "title", "keyword", 0)

        then:
        thrown(BadRequestException)

        when: "circle member is blocked"
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.REJECT)
        this.mockBoardDomainModel.setCircle((CircleDomainModel) this.mockCircleDomainModel)
        this.postService.search("test", "test board id", "title", "keyword", 0)

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for post create
     */
    @Test
    def "Post create normal case"() {
        given:
        def mockPostCreateRequestDto = new PostCreateRequestDto(
                "test post title",
                "test post content",
                ((BoardDomainModel) this.mockBoardDomainModel).getId(),
                List.of()
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.boardPort.findById("test board id") >> Optional.of(this.mockBoardDomainModel)
        this.postPort.create((PostDomainModel) this.mockPostDomainModel) >> this.mockPostDomainModel

        PowerMockito.mockStatic(PostDomainModel.class)

        when: "without circle"
        PowerMockito.when(PostDomainModel.of(
                mockPostCreateRequestDto.getTitle(),
                mockPostCreateRequestDto.getContent(),
                (UserDomainModel) this.mockUserDomainModel,
                (BoardDomainModel) this.mockBoardDomainModel,
                List.of()
        )).thenReturn((PostDomainModel) this.mockPostDomainModel)
        def postResponseDto = this.postService.create("test", mockPostCreateRequestDto)

        then:
        postResponseDto instanceof PostResponseDto
        with(postResponseDto) {
            getTitle() == "test post title"
            getContent() == "test post content"
        }

        when: "with circle"
        this.circleMemberPort.findByUserIdAndCircleId("test", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.mockBoardDomainModel.setCreateRoleList(Arrays.asList("COMMON"))
        this.mockBoardDomainModel.setCircle((CircleDomainModel) this.mockCircleDomainModel)
        PowerMockito.when(PostDomainModel.of(
                mockPostCreateRequestDto.getTitle(),
                mockPostCreateRequestDto.getContent(),
                (UserDomainModel) this.mockUserDomainModel,
                (BoardDomainModel) this.mockBoardDomainModel,
                List.of()
        )).thenReturn((PostDomainModel) this.mockPostDomainModel)
        postResponseDto = this.postService.create("test", mockPostCreateRequestDto)

        then:
        postResponseDto instanceof PostResponseDto
        with(postResponseDto) {
            getTitle() == "test post title"
            getContent() == "test post content"
        }

        when: "with circle for admin"
        this.userPort.findById("test1") >> Optional.of(this.mockAdminDomainModel)
        this.mockBoardDomainModel.setCircle((CircleDomainModel) this.mockCircleDomainModel)
        PowerMockito.when(PostDomainModel.of(
                mockPostCreateRequestDto.getTitle(),
                mockPostCreateRequestDto.getContent(),
                (UserDomainModel) this.mockAdminDomainModel,
                (BoardDomainModel) this.mockBoardDomainModel,
                List.of()
        )).thenReturn((PostDomainModel) this.mockPostDomainModel)
        postResponseDto = this.postService.create("test1", mockPostCreateRequestDto)

        then:
        postResponseDto instanceof PostResponseDto
        with(postResponseDto) {
            getTitle() == "test post title"
            getContent() == "test post content"
        }
    }

    @Test
    def "Post create deleted case"() {
        given:
        def mockPostCreateRequestDto = new PostCreateRequestDto(
                "test post title",
                "test post content",
                ((BoardDomainModel) this.mockBoardDomainModel).getId(),
                List.of()
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.boardPort.findById("test board id") >> Optional.of(this.mockBoardDomainModel)

        when: "deleted board"
        this.mockBoardDomainModel.setIsDeleted(true)
        this.postService.create("test", mockPostCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "deleted circle"
        this.mockBoardDomainModel.setIsDeleted(false)
        this.mockCircleDomainModel.setIsDeleted(true)
        this.mockBoardDomainModel.setCircle((CircleDomainModel) this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.postService.create("test", mockPostCreateRequestDto)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Post create invalid parameter"() {
        given:
        def mockPostCreateRequestDto = new PostCreateRequestDto(
                "test post title",
                "test post content",
                ((BoardDomainModel) this.mockBoardDomainModel).getId(),
                List.of()
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.boardPort.findById("test board id") >> Optional.of(this.mockBoardDomainModel)

        PowerMockito.mockStatic(PostDomainModel.class)

        when: "title is blank"
        mockPostCreateRequestDto.setTitle("")
        ((PostDomainModel) this.mockPostDomainModel).setTitle("")
        PowerMockito.when(PostDomainModel.of(
                mockPostCreateRequestDto.getTitle(),
                mockPostCreateRequestDto.getContent(),
                (UserDomainModel) this.mockUserDomainModel,
                (BoardDomainModel) this.mockBoardDomainModel,
                List.of()
        )).thenReturn((PostDomainModel) this.mockPostDomainModel)
        this.postService.create("test", mockPostCreateRequestDto)

        then:
        thrown(ConstraintViolationException)

        when: "exceeded number of attachments"
        mockPostCreateRequestDto.setTitle("title")
        mockPostCreateRequestDto.setAttachmentList(List.of("a", "b", "c", "d"))
        this.postService.create("test", mockPostCreateRequestDto)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Post create unauthorized case"() {
        given:
        def mockPostCreateRequestDto = new PostCreateRequestDto(
                "test post title",
                "test post content",
                ((BoardDomainModel) this.mockBoardDomainModel).getId(),
                List.of()
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.boardPort.findById("test board id") >> Optional.of(this.mockBoardDomainModel)

        when: "creator is COUNCIL when create role is PRESIDENT and COMMON"
        this.mockUserDomainModel.setRole(Role.COUNCIL)
        this.postService.create("test", mockPostCreateRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "circle member is blocked"
        this.mockUserDomainModel.setRole(Role.COMMON)
        this.mockBoardDomainModel.setCircle((CircleDomainModel) this.mockCircleDomainModel)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.REJECT)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.postService.create("test", mockPostCreateRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "circle member is await"
        this.mockBoardDomainModel.setCircle((CircleDomainModel) this.mockCircleDomainModel)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.postService.create("test", mockPostCreateRequestDto)

        then:
        thrown(BadRequestException)
    }

    /**
     * Test cases for post delete
     */
    @Test
    def "Post delete normal case"() {
        given:
        def deletedPostDomainModel = PostDomainModel.of(
                "test post id",
                "test post title",
                "test post content",
                (UserDomainModel) this.mockUserDomainModel,
                true,
                (BoardDomainModel) this.mockBoardDomainModel,
                null,
                null,
                List.of()
        )

        this.mockAdminDomainModel.setRole(Role.PRESIDENT)
        this.userPort.findById("test1") >> Optional.of(this.mockAdminDomainModel)
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        this.postPort.delete("test post id") >> Optional.of(deletedPostDomainModel)

        when: "with president user"
        def postResponseDto = this.postService.delete("test1", "test post id")

        then:
        postResponseDto instanceof PostResponseDto
        with(postResponseDto) {
            getIsDeleted()
        }

        when: "with writer"
        postResponseDto = this.postService.delete("test", "test post id")

        then:
        postResponseDto instanceof PostResponseDto
        with(postResponseDto) {
            getIsDeleted()
        }

        when: "with circle and leader circle"
        this.userPort.findById("test leader user id") >> Optional.of(this.mockCircleLeaderUserDomainModel)
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test leader user id", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)

        postResponseDto = this.postService.delete("test leader user id", "test post id")

        then:
        postResponseDto instanceof PostResponseDto
        with(postResponseDto) {
            getIsDeleted()
        }

        when: "with circle and writer"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        postResponseDto = this.postService.delete("test", "test post id")

        then:
        postResponseDto instanceof PostResponseDto
        with(postResponseDto) {
            getIsDeleted()
        }

        when: "with admin"
        this.mockAdminDomainModel.setRole(Role.ADMIN)
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test1", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)

        postResponseDto = this.postService.delete("test1", "test post id")

        then:
        postResponseDto instanceof PostResponseDto
        with(postResponseDto) {
            getIsDeleted()
        }

        when: "with app notice"
        this.mockAdminDomainModel.setRole(Role.ADMIN)
        this.mockBoardDomainModel.setCircle(null)
        this.mockBoardDomainModel.setCategory("APP_NOTICE")
        postResponseDto = this.postService.delete("test1", "test post id")

        then:
        postResponseDto instanceof PostResponseDto
        with(postResponseDto) {
            getIsDeleted()
        }
    }

    @Test
    def "Post delete deleted case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        when: "deleted post"
        this.mockPostDomainModel.setIsDeleted(true)
        this.postService.delete("test", "test post id")

        then:
        thrown(BadRequestException)

        when: "deleted circle"
        this.mockPostDomainModel.setIsDeleted(false)
        this.mockCircleDomainModel.setIsDeleted(true)
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.postService.delete("test", "test post id")

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Post delete unauthorized case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userPort.findById("test1") >> Optional.of(this.mockAdminDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        this.circleMemberPort.findByUserIdAndCircleId("test", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test1", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)

        when: "invalid role"
        this.mockAdminDomainModel.setRole(Role.COMMON)
        this.postService.delete("test1", "test post id")

        then:
        thrown(UnauthorizedException)

        when: "invalid role with circle"
        this.mockPostDomainModel.setWriter((UserDomainModel)this.mockAdminDomainModel)
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.postService.delete("test", "test post id")

        then:
        thrown(UnauthorizedException)

        when: "invalid leader of circle"
        this.mockAdminDomainModel.setRole(Role.LEADER_CIRCLE)
        this.mockPostDomainModel.setWriter((UserDomainModel)this.mockUserDomainModel)
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.postService.delete("test1", "test post id")

        then:
        thrown(UnauthorizedException)

        when: "circle member is await"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.postService.delete("test", "test post id")

        then:
        thrown(BadRequestException)

        when: "circle member is blocked"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.REJECT)
        this.postService.delete("test", "test post id")

        then:
        thrown(UnauthorizedException)

        when: "app board unauthorized case"
        this.mockBoardDomainModel.setCategory("APP_NOTICE")
        this.postService.delete("test", "test post id")

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for post update
     */
    @Test
    def "Post update normal case"() {
        given:
        def mockUpdatedPostDomainModel = PostDomainModel.of(
                "test post id",
                "test post title",
                "test post content",
                (UserDomainModel) this.mockUserDomainModel,
                false,
                (BoardDomainModel) this.mockBoardDomainModel,
                null,
                null,
                List.of()
        )

        def postUpdateRequestDto = new PostUpdateRequestDto("update post title", "test", List.of())

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userPort.findById("test1") >> Optional.of(this.mockAdminDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        this.postPort.update("test post id", (PostDomainModel)this.mockPostDomainModel) >> Optional.of(mockUpdatedPostDomainModel)
        this.commentPort.findByPostId("test post id", 0) >> new PageImpl<CommentDomainModel>(List.of())

        when: "without circle, with writer"
        def postResponseDto = this.postService.update("test", "test post id", postUpdateRequestDto)

        then:
        postResponseDto instanceof PostResponseDto
        with(postResponseDto) {
            getTitle() == "update post title"
        }

        when: "with circle"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        postResponseDto = this.postService.update("test", "test post id", postUpdateRequestDto)

        then:
        postResponseDto instanceof PostResponseDto
        with(postResponseDto) {
            getTitle() == "update post title"
        }

        when: "with circle for admin"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        postResponseDto = this.postService.update("test1", "test post id", postUpdateRequestDto)

        then:
        postResponseDto instanceof PostResponseDto
        with(postResponseDto) {
            getTitle() == "update post title"
        }

        when: "app notice"
        this.mockBoardDomainModel.setCategory("APP_NOTICE")
        this.mockBoardDomainModel.setCircle(null)
        postResponseDto = this.postService.update("test1", "test post id", postUpdateRequestDto)

        then:
        postResponseDto instanceof PostResponseDto
        with(postResponseDto) {
            getTitle() == "update post title"
        }
    }

    @Test
    def "Post update unauthorized case"() {
        given:
        def postUpdateRequestDto = new PostUpdateRequestDto("update post title", "test", List.of())

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        when: "not writer"
        this.mockAdminDomainModel.setRole(Role.PRESIDENT)
        this.userPort.findById("test1") >> Optional.of(this.mockAdminDomainModel)
        this.postService.update("test1", "test post id", postUpdateRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "circle member is await"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.postService.update("test", "test post id", postUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "circle member is await"
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.REJECT)
        this.postService.update("test", "test post id", postUpdateRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "app notice, not admin"
        this.mockBoardDomainModel.setCategory("APP_NOTICE")
        this.postService.update("test", "test post id", postUpdateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "Post update deleted case"() {
        given:
        def postUpdateRequestDto = new PostUpdateRequestDto("update post title", "test", List.of())

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        when: "deleted board"
        this.mockBoardDomainModel.setIsDeleted(true)
        this.postService.update("test", "test post id", postUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "deleted post"
        this.mockBoardDomainModel.setIsDeleted(false)
        this.mockPostDomainModel.setIsDeleted(true)
        this.postService.update("test", "test post id", postUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "deleted circle"
        this.mockPostDomainModel.setIsDeleted(false)
        this.mockCircleDomainModel.setIsDeleted(true)
        this.mockBoardDomainModel.setCircle((CircleDomainModel)this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test circle id") >> Optional.of(this.mockCircleMemberDomainModel)
        this.postService.update("test", "test post id", postUpdateRequestDto)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Post update invalid parameter"() {
        given:
        def postUpdateRequestDto = new PostUpdateRequestDto("", "test", List.of())

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.postPort.findById("test post id") >> Optional.of(this.mockPostDomainModel)

        when: "title is blank"
        this.postService.update("test", "test post id", postUpdateRequestDto)

        then:
        thrown(ConstraintViolationException)

        when: "exceeded number of attachments"
        postUpdateRequestDto.setTitle("update title")
        postUpdateRequestDto.setAttachmentList(List.of("a", "b", "c", "d"))
        this.postService.update("test", "test post id", postUpdateRequestDto)

        then:
        thrown(BadRequestException)
    }
}
