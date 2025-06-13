package net.causw.application.user;

import jakarta.servlet.http.HttpServletResponse;

import java.util.*;
import java.util.stream.Collectors;

import net.causw.adapter.persistence.post.LikePost;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.post.FavoritePostRepository;
import net.causw.adapter.persistence.repository.post.LikePostRepository;
import net.causw.adapter.persistence.repository.post.PostRepository;
import net.causw.adapter.persistence.repository.user.UserAdmissionRepository;
import net.causw.adapter.persistence.repository.user.UserRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.user.UserAdmission;
import net.causw.application.dto.post.PostsResponseDto;
import net.causw.application.dto.user.UserPostsResponseDto;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.application.dto.user.UserUpdateRoleRequestDto;
import net.causw.application.dto.util.dtoMapper.PostDtoMapper;
import net.causw.application.dto.util.dtoMapper.UserDtoMapper;
import net.causw.application.excel.UserExcelService;
import net.causw.application.pageable.PageableFactory;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.enums.user.UserState;

import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.ObjectFixtures;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.validation.GrantableRoleValidator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @InjectMocks
  UserService userService;

  @Mock
  UserExcelService userExcelService;
  @Mock
  UserRepository userRepository;
  @Mock
  UserAdmissionRepository userAdmissionRepository;
  @Mock
  LikePostRepository likePostRepository;
  @Mock
  PageableFactory pageableFactory;
  @Mock
  PostDtoMapper postDtoMapper;
  @Mock
  UserDtoMapper userDtoMapper;
  @Mock
  PostRepository postRepository;
  @Mock
  FavoritePostRepository favoritePostRepository;

  @Mock
  HttpServletResponse response;


  @Nested
  class ExportUserListToExcelTest {

    @DisplayName("Excel로 데이터 내보내기 성공 - 가입 대기 유저 목록")
    @Test
    void testExportAwaitUserListToExcelSuccess() {
      //given
      UserState state = UserState.AWAIT;
      String sheetName = state.getDescription() + " 유저";
      UserAdmission userAdmission = ObjectFixtures.getUserAdmission();
      userAdmission.getUser().setState(state);

      given(userAdmissionRepository.findAll()).willReturn(List.of(userAdmission));

      //when
      userService.exportUserListToExcel(response);

      //then
      LinkedHashMap<String, List<UserResponseDto>> exportedUserDataMap = captureGeneratedExcelData();
      List<UserResponseDto> exportedUserList = exportedUserDataMap.get(sheetName);

      verifyUserResponseDto(exportedUserList, state);
    }

    @DisplayName("Excel로 데이터 내보내기 성공 - 활성 유저 목록")
    @Test
    void testExportActiveUserListToExcelSuccess() {
      //given
      UserState state = UserState.ACTIVE;
      String sheetName = state.getDescription() + " 유저";
      User user = ObjectFixtures.getUser();
      user.setState(state);

      given(userRepository.findAllByState(state)).willReturn(List.of(user));

      //when
      userService.exportUserListToExcel(response);

      //then
      LinkedHashMap<String, List<UserResponseDto>> exportedUserDataMap = captureGeneratedExcelData();
      List<UserResponseDto> exportedUserList = exportedUserDataMap.get(sheetName);

      verifyUserResponseDto(exportedUserList, state);
    }

    private LinkedHashMap<String, List<UserResponseDto>> captureGeneratedExcelData() {
      ArgumentCaptor<LinkedHashMap<String, List<UserResponseDto>>> captor =
          ArgumentCaptor.forClass(LinkedHashMap.class);
      verify(userExcelService, times(1))
          .generateExcel(eq(response), anyString(), anyList(), captor.capture());

      return captor.getValue();
    }

    private void verifyUserResponseDto(
        List<UserResponseDto> exportedUserList,
        UserState userState
    ) {
      for (UserResponseDto userResponseDto : exportedUserList) {
        assertThat(userResponseDto).isNotNull();
        assertThat(userResponseDto.getState())
            .as("실제 UserResponseDto의 state가 %s이어야 합니다.", userState.getValue())
            .isEqualTo(userState);
      }
    }
  }

  @Nested
  @DisplayName("유저 게시글 모아보기 테스트")
  class UserFindPostsTest {

    private User user;

    @BeforeEach
    void setUp() {
      user = mock(User.class);
    }

    @DisplayName("유저 좋아요 게시글 모아보기 성공")
    @Test
    void findLikePosts_ShouldSuccess() {
      // given
      Integer pageNum = 0;
      PageRequest pageable = PageRequest.of(0, 10);
      String userId = "dummyId";

      when(user.getRoles()).thenReturn(Set.of(Role.COMMON));
      when(user.getState()).thenReturn(UserState.ACTIVE);
      when(user.getId()).thenReturn(userId);

      String mockPostId = "dummyPostId";
      Post mockPost = mock(Post.class);
      given(mockPost.getId()).willReturn(mockPostId);

      LikePost mockLikePost = mock(LikePost.class);
      List<LikePost> mockLikePosts = List.of(mockLikePost);
      Page<LikePost> mockLikePostPages = new PageImpl<>(mockLikePosts);

      when(mockLikePost.getPost()).thenReturn(mockPost);

      when(pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE)).thenReturn(
          pageable);
      when(likePostRepository.findByUserId(user.getId(),
          pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE)))
          .thenReturn(mockLikePostPages);

      PostsResponseDto mockPostDto = mock(PostsResponseDto.class);
      UserPostsResponseDto expectedResponseDto = mock(UserPostsResponseDto.class);

      given(postDtoMapper.toPostsResponseDto(
          eq(mockLikePost.getPost()),
          anyLong(),
          anyLong(),
          anyLong(),
          any(),
          anyBoolean(),
          anyBoolean()
      )).willReturn(mockPostDto);

      given(userDtoMapper.toUserPostsResponseDto(eq(user), any()))
          .willReturn(expectedResponseDto);

      given(postRepository.countAllCommentByPost_Id(mockPost.getId())).willReturn(1L);
      given(likePostRepository.countByPostId(mockPost.getId())).willReturn(1L);
      given(favoritePostRepository.countByPostIdAndIsDeletedFalse(mockPost.getId())).willReturn(1L);
      given(mockLikePost.getPost().getPostAttachImageList()).willReturn(List.of());
      given(mockPost.getVote()).willReturn(null);
      given(mockPost.getVote()).willReturn(null);
      // when
      UserPostsResponseDto result = userService.findLikePosts(user, pageNum);

      // then
      assertThat(result).isEqualTo(expectedResponseDto);

      verify(likePostRepository, times(1)).findByUserId(userId, pageable);
      verify(postDtoMapper, times(1)).toPostsResponseDto(any(), anyLong(), anyLong(), anyLong(), any(), anyBoolean(), anyBoolean());
      verify(userDtoMapper, times(1)).toUserPostsResponseDto(eq(user), any());

    }

  }

  @Nested
  class GrantRoleTest {

    private final User grantor = ObjectFixtures.getUser();
    private final User grantee = ObjectFixtures.getUser();
    private final String granteeId = "dummyGranteeId";
    private static final Set<Role> grantableRoles = GrantableRoleValidator.getGrantableRoles();

    private static Set<Role> getGrantableRoles() {
      return GrantableRoleValidator.getGrantableRoles();
    }

    private static Set<Role> getNonGrantableRoles() {
      return EnumSet.allOf(Role.class).stream()
              .filter(role -> !grantableRoles.contains(role))
              .collect(Collectors.toSet());
    }

    private static Set<Role> getSpecialRoles() {
      return EnumSet.allOf(Role.class).stream()
              .filter(role -> !role.equals(Role.COMMON))
              .collect(Collectors.toSet());
    }

    private static Set<Role> getGrantableRolesWithoutEdge() {
      return grantableRoles.stream()
              .filter(role -> !role.equals(Role.PRESIDENT))
              .collect(Collectors.toSet());
    }

    void assertServiceSuccess(User grantor, Role targetRole) {
      UserUpdateRoleRequestDto userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(String.valueOf(targetRole));
      userService.grantRole(grantor, granteeId, userUpdateRoleRequestDto);
      assertThat(grantor.getRoles()).isEqualTo(Set.of(Role.COMMON));
      assertThat(grantee.getRoles()).isEqualTo(Set.of(targetRole));
    }

    void assertValidatorFail(User grantor, Role targetRole) {
      UserUpdateRoleRequestDto userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(String.valueOf(targetRole));
      assertThatThrownBy(() -> userService.grantRole(grantor, granteeId, userUpdateRoleRequestDto))
              .isInstanceOf(UnauthorizedException.class)
              .hasMessageContaining(MessageUtil.GRANT_ROLE_NOT_ALLOWED)
              .extracting("errorCode")
              .isEqualTo(ErrorCode.GRANT_ROLE_NOT_ALLOWED);
    }

    @BeforeEach
    void setUp() {
      //when
      when(userRepository.findById(granteeId)).thenReturn(Optional.of(grantee));
    }

    @ParameterizedTest
    @MethodSource("getGrantableRoles")
    @DisplayName("위임 권한이 위임 가능 대상일 경우 성공")
    void a_Success(Role role) {
      // given
      grantor.setRoles(Set.of(role));
      grantee.setRoles(Set.of(Role.COMMON));

      // when & then
      assertServiceSuccess(grantor, role);
    }

    @ParameterizedTest
    @MethodSource("getNonGrantableRoles")
    @DisplayName("위임 권한이 위임 가능 대상이 아닐 경우 실패")
    void a_Failure(Role role) {
      // given
      grantor.setRoles(Set.of(role));
      grantee.setRoles(Set.of(Role.COMMON));

      // when & then
      assertValidatorFail(grantee, role);
    }

    @Test
    @DisplayName("위임자가 위임 권한을 가지고 있을 경우 성공")
    void b_Success() {
      // given
      Role grantableRole = grantableRoles.iterator().next();
      grantor.setRoles(Set.of(grantableRole));
      grantee.setRoles(Set.of(Role.COMMON));

      // when & then
      assertServiceSuccess(grantor, grantableRole);
    }

    @Test
    @DisplayName("위임자가 위임 권한을 가지고 있지 않을 경우 실패")
    void b_Failure() {
      // given
      grantor.setRoles(Set.of(Role.COMMON));
      grantee.setRoles(Set.of(Role.COMMON));

      // when & then
      assertValidatorFail(grantor, grantableRoles.iterator().next());
    }

    @ParameterizedTest
    @MethodSource("getGrantableRolesWithoutEdge")
    @DisplayName("피위임자가 일반 권한일 경우 성공")
    void c_Success1(Role role) {
      // given
      grantor.setRoles(Set.of(role));
      grantee.setRoles(Set.of(Role.COMMON));

      // when & then
      assertServiceSuccess(grantor, role);
    }

    @Test
    @DisplayName("위임자가 학생회장일 때 피위임자가 부학생회장과 학생회 또는 일반 권한일 경우 성공")
    void c_Success2() {
      for (Role role : Set.of(Role.VICE_PRESIDENT, Role.COUNCIL, Role.COMMON)) {
        // given
        grantor.setRoles(Set.of(Role.PRESIDENT));
        grantee.setRoles(Set.of(role));

        // when & then
        assertServiceSuccess(grantor, Role.PRESIDENT);
      }
    }

    @ParameterizedTest
    @MethodSource("getGrantableRolesWithoutEdge")
    @DisplayName("피위임자가 특수 권한일 경우 실패(특수 조건을 가진 권한 제외)")
    void c_Failure(Role role) {
      for (Role specialRole : getSpecialRoles()) {
        // given
        grantor.setRoles(Set.of(role));
        grantee.setRoles(Set.of(specialRole));

        // when & then
        assertValidatorFail(grantor, role);
      }
    }
  }
}