package net.causw.application.user;

import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
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
import net.causw.application.dto.util.dtoMapper.PostDtoMapper;
import net.causw.application.dto.util.dtoMapper.UserDtoMapper;
import net.causw.application.excel.UserExcelService;
import net.causw.application.pageable.PageableFactory;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.enums.user.UserState;

import net.causw.domain.model.util.StaticValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
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

  HttpServletResponse response;
  User user;

  @BeforeEach
  void setUp() {
    response = mock(HttpServletResponse.class);
    user = mock(User.class);
  }

  @Nested
  class ExportUserListToExcelTest {

    @DisplayName("Excel로 데이터 내보내기 성공 - 가입 대기 유저 목록")
    @Test
    void testExportAwaitUserListToExcelSuccess() {
      //given
      UserState state = UserState.AWAIT;
      String sheetName = state.getDescription() + " 유저";
      UserAdmission userAdmission = mock(UserAdmission.class);
      List<UserAdmission> userAdmissionList = List.of(userAdmission);

      given(user.getState()).willReturn(state);
      given(userAdmission.getUser()).willReturn(user);
      given(userAdmissionRepository.findAll()).willReturn(userAdmissionList);

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
      List<User> userList = List.of(user);

      given(user.getState()).willReturn(state);
      given(userRepository.findAllByState(state)).willReturn(userList);

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
}