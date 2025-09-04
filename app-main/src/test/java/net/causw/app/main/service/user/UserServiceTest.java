package net.causw.app.main.service.user;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import net.causw.app.main.domain.model.entity.post.LikePost;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.user.UserAdmission;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.app.main.domain.model.enums.user.UserState;
import net.causw.app.main.dto.post.PostsResponseDto;
import net.causw.app.main.dto.user.UserCreateRequestDto;
import net.causw.app.main.dto.user.UserPostsResponseDto;
import net.causw.app.main.dto.user.UserResponseDto;
import net.causw.app.main.dto.user.UserSignInResponseDto;
import net.causw.app.main.dto.util.dtoMapper.PostDtoMapper;
import net.causw.app.main.dto.util.dtoMapper.UserDtoMapper;
import net.causw.app.main.infrastructure.redis.RedisUtils;
import net.causw.app.main.infrastructure.security.JwtTokenProvider;
import net.causw.app.main.repository.post.FavoritePostRepository;
import net.causw.app.main.repository.post.LikePostRepository;
import net.causw.app.main.repository.post.PostRepository;
import net.causw.app.main.repository.user.UserAdmissionRepository;
import net.causw.app.main.repository.user.UserRepository;
import net.causw.app.main.service.excel.UserExcelService;
import net.causw.app.main.service.pageable.PageableFactory;
import net.causw.app.main.service.post.PostService;
import net.causw.app.main.service.userBlock.UserBlockEntityService;
import net.causw.app.main.util.ObjectFixtures;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@InjectMocks
	UserService userService;

	@Mock
	UserExcelService userExcelService;

	@Mock
	UserBlockEntityService userBlockEntityService;

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
	PostService postService;

	@Mock
	HttpServletResponse response;

	@Mock
	PasswordEncoder passwordEncoder;

	@Mock
	Validator validator;

	@Mock
	JwtTokenProvider jwtTokenProvider;

	@Mock
	RedisUtils redisUtils;

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
			Set<String> blockedUserIds = Set.of();

			when(likePostRepository.findByUserId(user.getId(),
				blockedUserIds, pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE)))
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
			given(favoritePostRepository.countByPostId(mockPost.getId())).willReturn(1L);
			given(mockLikePost.getPost().getPostAttachImageList()).willReturn(List.of());
			given(mockPost.getVote()).willReturn(null);
			given(mockPost.getVote()).willReturn(null);
			// when
			UserPostsResponseDto result = userService.findLikePosts(user, pageNum);

			// then
			assertThat(result).isEqualTo(expectedResponseDto);

			verify(likePostRepository, times(1)).findByUserId(userId, blockedUserIds, pageable);
			verify(postDtoMapper, times(1)).toPostsResponseDto(any(), anyLong(), anyLong(), anyLong(), any(),
				anyBoolean(), anyBoolean());
			verify(userDtoMapper, times(1)).toUserPostsResponseDto(eq(user), any());

		}

	}

	@Nested
	@DisplayName("회원가입 테스트")
	class SignUpTest {

		private UserCreateRequestDto signUpRequest;
		private User existingUser;

		@BeforeEach
		void setUp() {
			signUpRequest = ObjectFixtures.getUserCreateRequestDto();
			existingUser = ObjectFixtures.getUser();

			ReflectionTestUtils.setField(existingUser, "id", "testUserId");

			// 공통 Mock
			lenient().when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
			lenient().when(userDtoMapper.toUserResponseDto(any(User.class), any(), any()))
				.thenReturn(mock(UserResponseDto.class));
		}

		@Test
		@DisplayName("신규 사용자 회원가입 성공")
		void signUp_NewUser_Success() {
			// given - 모든 조회에서 빈 결과 반환
			given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());
			given(userRepository.findByPhoneNumber(anyString())).willReturn(Optional.empty());
			given(userRepository.findByStudentId(anyString())).willReturn(Optional.empty());
			given(userRepository.findByNickname(anyString())).willReturn(Optional.empty());

			// when
			UserResponseDto result = userService.signUp(signUpRequest);

			// then
			verify(userRepository, times(1)).save(any(User.class));
			assertThat(result).isNotNull();
		}

		@Test
		@DisplayName("거절 상태 사용자의 재가입 성공")
		void signUp_RejectedUser_Success() {
			// given
			existingUser.setState(UserState.REJECT);
			given(userRepository.findByEmail(signUpRequest.getEmail()))
				.willReturn(Optional.of(existingUser));
			// 다른 필드들은 동일한 사용자로 반환
			given(userRepository.findByNickname(anyString())).willReturn(Optional.of(existingUser));
			given(userRepository.findByPhoneNumber(anyString())).willReturn(Optional.of(existingUser));
			given(userRepository.findByStudentId(anyString())).willReturn(Optional.of(existingUser));

			// when
			UserResponseDto result = userService.signUp(signUpRequest);

			// then
			verify(userRepository, times(1)).save(existingUser);
			assertThat(existingUser.getState()).isEqualTo(UserState.AWAIT);
			assertThat(result).isNotNull();
		}

		@Test
		@DisplayName("이메일 오타로 인한 유령 계정 복구 성공")
		void signUp_EmailTypoRecovery_Success() {
			// given
			User ghostUser = ObjectFixtures.getUser();
			ghostUser.setState(UserState.REJECT);
			ReflectionTestUtils.setField(ghostUser, "id", "ghostUserId");

			// 잘못 입력한 이메일, 제대로 입력한 전화번호 상황
			given(userRepository.findByEmail(signUpRequest.getEmail())).willReturn(Optional.empty());
			given(userRepository.findByPhoneNumber(signUpRequest.getPhoneNumber()))
				.willReturn(Optional.of(ghostUser));
			// 다른 정보들은 유령 계정과 일치
			given(userRepository.findByNickname(anyString())).willReturn(Optional.of(ghostUser));
			given(userRepository.findByStudentId(anyString())).willReturn(Optional.of(ghostUser));

			// when
			UserResponseDto result = userService.signUp(signUpRequest);

			// then - 유령 계정이 업데이트되어 저장되었는지 확인
			verify(userRepository, times(1)).save(ghostUser);
			assertThat(ghostUser.getEmail()).isEqualTo(signUpRequest.getEmail()); // 이메일 교정 확인
			assertThat(ghostUser.getState()).isEqualTo(UserState.AWAIT);
			assertThat(result).isNotNull();
		}

		@Test
		@DisplayName("ACTIVE 유저와 동일한 이메일로 가입 시도 시 실패")
		void signUp_DuplicateActiveUserEmail_ThrowsException() {
			// given
			existingUser.setState(UserState.ACTIVE);
			given(userRepository.findByEmail(signUpRequest.getEmail()))
				.willReturn(Optional.of(existingUser));

			// when & then
			BadRequestException exception = assertThrows(BadRequestException.class,
				() -> userService.signUp(signUpRequest));

			assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ROW_ALREADY_EXIST);
			verify(userRepository, never()).save(any(User.class));
		}

		@Test
		@DisplayName("추방(DROP)된 사용자의 재가입 시도 시 실패")
		void signUp_DroppedUser_ThrowsException() {
			// given
			existingUser.setState(UserState.DROP);
			given(userRepository.findByEmail(signUpRequest.getEmail()))
				.willReturn(Optional.of(existingUser));

			// when & then
			assertThrows(BadRequestException.class, () -> userService.signUp(signUpRequest));
			verify(userRepository, never()).save(any(User.class));
		}

		@Test
		@DisplayName("탈퇴(INACTIVE)한 사용자가 재가입 시도 시 실패")
		void signUp_InactiveUser_ThrowsException() {
			// given
			existingUser.setState(UserState.INACTIVE);
			given(userRepository.findByEmail(signUpRequest.getEmail()))
				.willReturn(Optional.of(existingUser));

			// when & then
			BadRequestException exception = assertThrows(BadRequestException.class,
				() -> userService.signUp(signUpRequest));

			assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ROW_ALREADY_EXIST);
			verify(userRepository, never()).save(any(User.class));
		}
	}

	@Nested
	class RecoverUserTest {

		@Test
		@DisplayName("INACTIVE 사용자 계정 복구 성공")
		void recoverUser_InactiveUser_Success() {

			User inactiveUser = ObjectFixtures.getCertifiedUserWithId("test-user-id");
			inactiveUser.setState(UserState.INACTIVE);
			inactiveUser.setRoles(new HashSet<>(Set.of(Role.NONE)));

			// given
			given(userRepository.findByEmail("test@cau.ac.kr"))
				.willReturn(Optional.of(inactiveUser));
			given(jwtTokenProvider.createRefreshToken()).willReturn("refresh-token");
			given(jwtTokenProvider.createAccessToken(
				eq("test-user-id"),
				eq(Set.of(Role.COMMON)),
				eq(UserState.ACTIVE)
			)).willReturn("access-token");

			// when
			UserSignInResponseDto result = userService.recoverUser("test@cau.ac.kr");

			// then
			verify(userRepository).save(inactiveUser);
			assertThat(inactiveUser.getState()).isEqualTo(UserState.ACTIVE);
			assertThat(inactiveUser.getRoles()).containsExactly(Role.COMMON);
			assertThat(result.getAccessToken()).isEqualTo("access-token");
			assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
		}
	}
}