package net.causw.app.main.service.user;

import jakarta.servlet.http.HttpServletResponse;

import java.time.format.DateTimeFormatter;

import lombok.RequiredArgsConstructor;

import net.causw.app.main.domain.event.CertifiedUserCreatedEvent;
import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.circle.Circle;
import net.causw.app.main.domain.model.entity.circle.CircleMember;
import net.causw.app.main.domain.model.entity.locker.LockerLog;
import net.causw.app.main.dto.post.PostsResponseDto;
import net.causw.app.main.infrastructure.firebase.FcmUtils;
import net.causw.app.main.repository.userAcademicRecord.UserAcademicRecordApplicationRepository;
import net.causw.app.main.repository.uuidFile.UserAcademicRecordApplicationAttachImageRepository;
import net.causw.app.main.repository.uuidFile.UserProfileImageRepository;
import net.causw.app.main.domain.model.entity.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.UserAcademicRecordApplicationAttachImage;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.UserAdmissionAttachImage;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.UserProfileImage;
import net.causw.app.main.domain.policy.StatusPolicy;
import net.causw.app.main.service.excel.UserExcelService;
import net.causw.app.main.service.pageable.PageableFactory;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.repository.board.BoardRepository;
import net.causw.app.main.repository.circle.CircleMemberRepository;
import net.causw.app.main.repository.circle.CircleRepository;
import net.causw.app.main.repository.comment.ChildCommentRepository;
import net.causw.app.main.repository.comment.CommentRepository;
import net.causw.app.main.repository.locker.LockerLogRepository;
import net.causw.app.main.repository.locker.LockerRepository;
import net.causw.app.main.repository.post.FavoritePostRepository;
import net.causw.app.main.repository.post.LikePostRepository;
import net.causw.app.main.repository.post.PostRepository;
import net.causw.app.main.repository.user.UserAdmissionLogRepository;
import net.causw.app.main.repository.user.UserAdmissionRepository;
import net.causw.app.main.repository.user.UserRepository;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.user.UserAdmission;
import net.causw.app.main.domain.model.entity.user.UserAdmissionLog;
import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;
import net.causw.app.main.dto.duplicate.DuplicatedCheckResponseDto;
import net.causw.app.main.dto.board.BoardResponseDto;
import net.causw.app.main.dto.circle.CircleResponseDto;
import net.causw.app.main.dto.user.*;
import net.causw.app.main.dto.util.dtoMapper.BoardDtoMapper;
import net.causw.app.main.dto.util.dtoMapper.CircleDtoMapper;
import net.causw.app.main.dto.util.dtoMapper.PostDtoMapper;
import net.causw.app.main.dto.util.dtoMapper.UserDtoMapper;
import net.causw.app.main.service.post.PostService;
import net.causw.app.main.service.uuidFile.UuidFileService;
import net.causw.app.main.infrastructure.security.JwtTokenProvider;
import net.causw.app.main.infrastructure.aop.annotation.MeasureTime;
import net.causw.app.main.domain.model.enums.circle.CircleMemberStatus;
import net.causw.app.main.domain.model.enums.locker.LockerLogAction;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.app.main.domain.model.enums.user.UserAdmissionLogAction;
import net.causw.app.main.domain.model.enums.user.UserState;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.model.enums.uuidFile.FilePath;
import net.causw.global.constant.MessageUtil;
import net.causw.app.main.infrastructure.redis.RedisUtils;
import net.causw.global.constant.StaticValue;
import net.causw.app.main.domain.validation.*;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.InternalServerException;
import net.causw.global.exception.NotFoundException;
import net.causw.global.exception.UnauthorizedException;
import net.causw.app.main.infrastructure.mail.GoogleMailSender;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Validator;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@MeasureTime
@Service
@RequiredArgsConstructor
public class UserService {
	private final JwtTokenProvider jwtTokenProvider;
	private final UuidFileService uuidFileService;
	private final GoogleMailSender googleMailSender;
	private final PasswordGenerator passwordGenerator;
	private final PasswordEncoder passwordEncoder;
	private final Validator validator;
	private final ApplicationEventPublisher eventPublisher;

	private final UserRepository userRepository;
	private final CircleRepository circleRepository;
	private final CircleMemberRepository circleMemberRepository;
	private final PostRepository postRepository;
	private final PageableFactory pageableFactory;
	private final CommentRepository commentRepository;
	private final ChildCommentRepository childCommentRepository;
	private final UserAdmissionRepository userAdmissionRepository;
	private final RedisUtils redisUtils;
	private final FcmUtils fcmUtils;
	private final LockerRepository lockerRepository;
	private final LockerLogRepository lockerLogRepository;
	private final UserAdmissionLogRepository userAdmissionLogRepository;
	private final BoardRepository boardRepository;
	private final FavoritePostRepository favoritePostRepository;
	private final LikePostRepository likePostRepository;
	private final UserProfileImageRepository userProfileImageRepository;
	private final UserExcelService userExcelService;
	private final UserAcademicRecordApplicationRepository userAcademicRecordApplicationRepository;
	private final UserAcademicRecordApplicationAttachImageRepository userAcademicRecordApplicationAttachImageRepository;

	private final UserRoleService userRoleService;

	private final UserDtoMapper userDtoMapper;
	private final PostDtoMapper postDtoMapper;
	private final PostService postService;

	@Transactional
	public void findPassword(
		UserFindPasswordRequestDto userFindPasswordRequestDto
	) {
		User requestUser = userRepository.findByEmailAndName(
			userFindPasswordRequestDto.getEmail().trim(),
			userFindPasswordRequestDto.getName().trim()
		).orElseThrow(() ->
			new NotFoundException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND));

		if (!requestUser.getPhoneNumber().equals(userFindPasswordRequestDto.getPhoneNumber())) {
			throw new NotFoundException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND);
		}

		// 임시 비밀번호 생성
		String newPassword = this.passwordGenerator.generate();

		// 메일 전송
		this.googleMailSender.sendNewPasswordMail(requestUser.getEmail(), newPassword);

		// 비밀번호 변경
		requestUser.setPassword(this.passwordEncoder.encode(newPassword));
		// ! dirty cecking 때문에 save 필요 없음
	}

	// Find process of another user
	@Transactional(readOnly = true)
	public UserResponseDto findByUserId(String targetUserId, User requestUser) {
		Set<Role> roles = requestUser.getRoles();

		ValidatorBucket.of()
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.consistOf(UserStateValidator.of(requestUser.getState()))
			.consistOf(UserRoleValidator.of(roles,
				Set.of(Role.LEADER_CIRCLE)))
			.validate();

		if (roles.contains(Role.LEADER_CIRCLE)) {
			List<Circle> ownCircles = this.circleRepository.findByLeader_Id(requestUser.getId());
			if (ownCircles.isEmpty()) {
				throw new InternalServerException(
					ErrorCode.INTERNAL_SERVER,
					MessageUtil.NO_ASSIGNED_CIRCLE_FOR_LEADER
				);
			}

			boolean isMemberOfAnyCircle = ownCircles.stream()
				.anyMatch(circleEntity ->
					this.circleMemberRepository.findByUser_IdAndCircle_Id(targetUserId, circleEntity.getId())
						.map(circleMemberEntity -> circleMemberEntity.getStatus() == CircleMemberStatus.MEMBER)
						.orElse(false)
				);

			if (!isMemberOfAnyCircle) {
				throw new BadRequestException(ErrorCode.NOT_MEMBER, MessageUtil.CIRCLE_MEMBER_NOT_FOUND);
			}
		}

		User entity = this.userRepository.findById(targetUserId)
			.orElseThrow(() ->
				new BadRequestException(
					ErrorCode.ROW_DOES_NOT_EXIST,
					MessageUtil.USER_NOT_FOUND
				));
		return UserDtoMapper.INSTANCE.toUserResponseDto(entity, null, null);
	}

	@Transactional(readOnly = true)
	public UserResponseDto findCurrentUser(User requestUser) {
		Set<Role> roles = requestUser.getRoles();

		ValidatorBucket.of()
			.consistOf(UserStateValidator.of(requestUser.getState()))
			.validate();

		if (roles.contains(Role.LEADER_CIRCLE)) {
			List<Circle> ownCircles = this.circleRepository.findByLeader_Id(requestUser.getId());
			if (ownCircles.isEmpty()) {
				throw new InternalServerException(
					ErrorCode.INTERNAL_SERVER,
					MessageUtil.NO_ASSIGNED_CIRCLE_FOR_LEADER
				);
			}

			return UserDtoMapper.INSTANCE.toUserResponseDto(
				requestUser,
				ownCircles.stream().map(Circle::getId).collect(Collectors.toList()),
				ownCircles.stream().map(Circle::getName).collect(Collectors.toList())
			);
		}
		return UserDtoMapper.INSTANCE.toUserResponseDto(requestUser, null, null);
	}

	@Transactional(readOnly = true)
	public UserPostsResponseDto findPosts(User requestUser, Integer pageNum) {
		Set<Role> roles = requestUser.getRoles();

		ValidatorBucket.of()
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.consistOf(UserStateValidator.of(requestUser.getState()))
			.validate();

		return UserDtoMapper.INSTANCE.toUserPostsResponseDto(
			requestUser,
			this.postRepository.findByUserId(requestUser.getId(),
				this.pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE)
			).map(post -> PostDtoMapper.INSTANCE.toPostsResponseDto(
				post,
				getNumOfComment(post),
				getNumOfPostLikes(post),
				getNumOfPostFavorites(post),
				!post.getPostAttachImageList().isEmpty() ? post.getPostAttachImageList().get(0) : null,
				StatusPolicy.isPostVote(post),
				StatusPolicy.isPostForm(post)
			))
		);
	}

	@Transactional(readOnly = true)
	public UserPostsResponseDto findFavoritePosts(User requestUser, Integer pageNum) {
		Set<Role> roles = requestUser.getRoles();

		ValidatorBucket.of()
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.consistOf(UserStateValidator.of(requestUser.getState()))
			.validate();

		return UserDtoMapper.INSTANCE.toUserPostsResponseDto(
			requestUser,
			this.favoritePostRepository.findByUserId(requestUser.getId(),
					this.pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE))
				.map(favoritePost -> {
					Post post = favoritePost.getPost();
					PostsResponseDto dto = PostDtoMapper.INSTANCE.toPostsResponseDto(
						post,
						getNumOfComment(post),
						getNumOfPostLikes(post),
						getNumOfPostFavorites(post),
						!post.getPostAttachImageList().isEmpty() ? post.getPostAttachImageList().get(0) : null,
						StatusPolicy.isPostVote(post),
						StatusPolicy.isPostForm(post)
					);

					// 화면에 표시될 작성자 닉네임 설정
					String displayNickname = postService.getDisplayWriterNickname(
						post.getWriter(),
						post.getIsAnonymous(),
						post.getWriter() != null ? post.getWriter().getNickname() : null
					);
					dto.setDisplayWriterNickname(displayNickname);

					if (dto.getIsAnonymous()) {
						dto.updateAnonymousWriterInfo();
					}

					return dto;
				})
		);
	}

	/**
	 *
	 * @param requestUser 유저
	 * @param pageNum 페이지 넘버
	 * @return 회원 좋아요 게시글 dto
	 */
	@Transactional(readOnly = true)
	public UserPostsResponseDto findLikePosts(User requestUser, Integer pageNum) {
		Set<Role> roles = requestUser.getRoles();

		ValidatorBucket.of()
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.consistOf(UserStateValidator.of(requestUser.getState()))
			.validate();

		return userDtoMapper.toUserPostsResponseDto(
			requestUser,
			this.likePostRepository.findByUserId(requestUser.getId(),
					this.pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE))
				.map(likePost -> {
					Post post = likePost.getPost();
					PostsResponseDto dto = postDtoMapper.toPostsResponseDto(
						post,
						getNumOfComment(post),
						getNumOfPostLikes(post),
						getNumOfPostFavorites(post),
						!post.getPostAttachImageList().isEmpty() ? post.getPostAttachImageList().get(0) : null,
						StatusPolicy.isPostVote(post),
						StatusPolicy.isPostForm(post)
					);

					// 화면에 표시될 작성자 닉네임 설정
					String displayNickname = postService.getDisplayWriterNickname(
						post.getWriter(),
						post.getIsAnonymous(),
						post.getWriter() != null ? post.getWriter().getNickname() : null
					);
					dto.setDisplayWriterNickname(displayNickname);

					return dto;
				})
		);
	}

	@Transactional(readOnly = true)
	public UserPostsResponseDto findCommentedPosts(User requestUser, Integer pageNum) {
		Set<Role> roles = requestUser.getRoles();

		ValidatorBucket.of()
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.consistOf(UserStateValidator.of(requestUser.getState()))
			.validate();

		Pageable pageable = this.pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE / 2 + 5);

		Page<Post> postsFromComments = this.commentRepository.findPostsByUserId(requestUser.getId(), pageable);
		Page<Post> postsFromChildComments = this.childCommentRepository.findPostsByUserId(requestUser.getId(),
			pageable);

		//Comment와 ChildComment의 Post 중복 제거
		Set<Post> combinedPosts = new HashSet<>();
		combinedPosts.addAll(postsFromComments.getContent());
		combinedPosts.addAll(postsFromChildComments.getContent());

		//Set에서 Page로 타입 변환 및 정렬
		List<Post> combinedPostsList = new ArrayList<>(combinedPosts);
		combinedPostsList.sort((post1, post2) -> post2.getCreatedAt().compareTo(post1.getCreatedAt()));
		Page<Post> combinedPostsPage = new PageImpl<>(combinedPostsList, pageable, combinedPostsList.size());

		return UserDtoMapper.INSTANCE.toUserPostsResponseDto(
			requestUser,
			combinedPostsPage.map(post -> {
				PostsResponseDto dto = PostDtoMapper.INSTANCE.toPostsResponseDto(
					post,
					getNumOfComment(post),
					getNumOfPostLikes(post),
					getNumOfPostFavorites(post),
					!post.getPostAttachImageList().isEmpty() ? post.getPostAttachImageList().get(0) : null,
					StatusPolicy.isPostVote(post),
					StatusPolicy.isPostForm(post)
				);

				// 화면에 표시될 작성자 닉네임 설정
				String displayNickname = postService.getDisplayWriterNickname(
					post.getWriter(),
					post.getIsAnonymous(),
					post.getWriter() != null ? post.getWriter().getNickname() : null
				);
				dto.setDisplayWriterNickname(displayNickname);

				return dto;
			})
		);
	}

	@Transactional(readOnly = true)
	public UserCommentsResponseDto findComments(User requestUser, Integer pageNum) {
		Set<Role> roles = requestUser.getRoles();

		ValidatorBucket.of()
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.consistOf(UserStateValidator.of(requestUser.getState()))
			.validate();

		return UserDtoMapper.INSTANCE.toUserCommentsResponseDto(
			requestUser,
			this.commentRepository.findByUserId(requestUser.getId(),
					this.pageableFactory.create(pageNum, StaticValue.DEFAULT_COMMENT_PAGE_SIZE))
				.map(comment -> {
					Post post = this.postRepository.findById(comment.getPost().getId())
						.orElseThrow(() -> new BadRequestException(
							ErrorCode.ROW_DOES_NOT_EXIST,
							MessageUtil.POST_NOT_FOUND
						));
					return UserDtoMapper.INSTANCE.toCommentsOfUserResponseDto(
						comment,
						post.getBoard().getId(),
						post.getBoard().getName(),
						post.getId(),
						post.getTitle(),
						post.getBoard().getCircle() != null ? post.getBoard().getCircle().getId() : null,
						post.getBoard().getCircle() != null ? post.getBoard().getCircle().getName() : null
					);
				})
		);
	}

	@Transactional(readOnly = true)
	public List<UserResponseDto> findByName(User requestUser, String name) {
		Set<Role> roles = requestUser.getRoles();

		ValidatorBucket.of()
			.consistOf(UserStateValidator.of(requestUser.getState()))
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.consistOf(UserRoleValidator.of(roles,
				Set.of(Role.LEADER_CIRCLE
				)))
			.validate();

		if (roles.contains(Role.LEADER_CIRCLE)) {
			List<Circle> ownCircles = this.circleRepository.findByLeader_Id(requestUser.getId());
			if (ownCircles.isEmpty()) {
				throw new InternalServerException(
					ErrorCode.INTERNAL_SERVER,
					MessageUtil.NO_ASSIGNED_CIRCLE_FOR_LEADER
				);
			}

			return this.userRepository.findByName(name)
				.stream()
				.filter(user -> user.getState().equals(UserState.ACTIVE))
				.filter(user ->
					ownCircles.stream()
						.anyMatch(circle ->
							this.circleMemberRepository.findByUser_IdAndCircle_Id(user.getId(), circle.getId())
								.map(circleMemberEntity -> circleMemberEntity.getStatus() == CircleMemberStatus.MEMBER)
								.orElse(false)))
				.map(user -> UserDtoMapper.INSTANCE.toUserResponseDto(
					user,
					ownCircles.stream().map(Circle::getId).collect(Collectors.toList()),
					ownCircles.stream().map(Circle::getName).collect(Collectors.toList())))
				.collect(Collectors.toList());
		}

		return this.userRepository.findByName(name)
			.stream()
			.filter(user -> user.getState().equals(UserState.ACTIVE))
			.map(user -> UserDtoMapper.INSTANCE.toUserResponseDto(user, null, null))
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public UserPrivilegedResponseDto findPrivilegedUsers(User user) {
		Set<Role> roles = user.getRoles();

		ValidatorBucket.of()
			.consistOf(UserStateValidator.of(user.getState()))
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.consistOf(UserRoleValidator.of(roles, Set.of()))
			.validate();

		//todo: 현재 겸직을 고려하기 위해 _N_ 사용 중이나 port 와 domain model 삭제를 위해 배제
		//때문에 추후 userRole 관리 리팩토링 후 겸직을 고려하게 변경 필요
		return UserDtoMapper.INSTANCE.toUserPrivilegedResponseDto(
			this.userRepository.findByRoleAndState(Role.PRESIDENT, UserState.ACTIVE)
				.stream()
				.map(president -> UserDtoMapper.INSTANCE.toUserResponseDto(president, null, null))
				.collect(Collectors.toList()),
			this.userRepository.findByRoleAndState(Role.VICE_PRESIDENT, UserState.ACTIVE)
				.stream()
				.map(vicePresident -> UserDtoMapper.INSTANCE.toUserResponseDto(vicePresident, null, null))
				.collect(Collectors.toList()),
			this.userRepository.findByRoleAndState(Role.COUNCIL, UserState.ACTIVE)
				.stream()
				.map(council -> UserDtoMapper.INSTANCE.toUserResponseDto(council, null, null))
				.collect(Collectors.toList()),
			this.userRepository.findByRoleAndState(Role.LEADER_1, UserState.ACTIVE)
				.stream()
				.map(leader1 -> UserDtoMapper.INSTANCE.toUserResponseDto(leader1, null, null))
				.collect(Collectors.toList()),
			this.userRepository.findByRoleAndState(Role.LEADER_2, UserState.ACTIVE)
				.stream()
				.map(leader2 -> UserDtoMapper.INSTANCE.toUserResponseDto(leader2, null, null))
				.collect(Collectors.toList()),
			this.userRepository.findByRoleAndState(Role.LEADER_3, UserState.ACTIVE)
				.stream()
				.map(leader3 -> UserDtoMapper.INSTANCE.toUserResponseDto(leader3, null, null))
				.collect(Collectors.toList()),
			this.userRepository.findByRoleAndState(Role.LEADER_4, UserState.ACTIVE)
				.stream()
				.map(leader4 -> UserDtoMapper.INSTANCE.toUserResponseDto(leader4, null, null))
				.collect(Collectors.toList()),
			this.userRepository.findByRoleAndState(Role.LEADER_CIRCLE, UserState.ACTIVE)
				.stream()
				.map(userDomainModel -> {
					List<Circle> ownCircles = this.circleRepository.findByLeader_Id(userDomainModel.getId());
					if (ownCircles.isEmpty()) {
						throw new InternalServerException(
							ErrorCode.INTERNAL_SERVER,
							MessageUtil.NO_ASSIGNED_CIRCLE_FOR_LEADER
						);
					}
					return UserDtoMapper.INSTANCE.toUserResponseDto(
						userDomainModel,
						ownCircles.stream().map(Circle::getId).collect(Collectors.toList()),
						ownCircles.stream().map(Circle::getName).collect(Collectors.toList())
					);
				})
				.collect(Collectors.toList()),
			this.userRepository.findByRoleAndState(Role.LEADER_ALUMNI, UserState.ACTIVE)
				.stream()
				.map(alumni -> UserDtoMapper.INSTANCE.toUserResponseDto(alumni, null, null))
				.collect(Collectors.toList())
		);
	}

	@Transactional
	public Page<UserResponseDto> findByState(
		User user,
		String state,
		String name,
		Integer pageNum
	) {
		Set<Role> roles = user.getRoles();

		ValidatorBucket.of()
			.consistOf(UserStateValidator.of(user.getState()))
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.consistOf(UserRoleValidator.of(roles, Set.of()))
			.validate();

		//portimpl 내부 로직 서비스단으로 이동
		Page<User> usersPage;
		if ("INACTIVE_N_DROP".equals(state)) {
			List<String> statesToSearch = Arrays.asList("INACTIVE", "DROP");
			usersPage = userRepository.findByStateInAndNameContaining(
				statesToSearch,
				name,
				PageRequest.of(pageNum, StaticValue.USER_LIST_PAGE_SIZE)
			);
		} else {
			usersPage = userRepository.findByStateAndName(
				state,
				name,
				PageRequest.of(pageNum, StaticValue.USER_LIST_PAGE_SIZE)
			);
		}

		return usersPage.map(srcUser -> {
			if (srcUser.getRoles().contains(Role.LEADER_CIRCLE) && !"INACTIVE_N_DROP".equals(state)) {
				List<Circle> ownCircles = circleRepository.findByLeader_Id(srcUser.getId());
				if (ownCircles.isEmpty()) {
					userRoleService.removeRole(srcUser, Role.LEADER_CIRCLE);
					userRepository.save(srcUser);
				}

				return UserDtoMapper.INSTANCE.toUserResponseDto(
					srcUser,
					ownCircles.stream().map(Circle::getId).collect(Collectors.toList()),
					ownCircles.stream().map(Circle::getName).collect(Collectors.toList())
				);
			} else {
				return UserDtoMapper.INSTANCE.toUserResponseDto(srcUser, null, null);
			}
		});
	}

	@Transactional(readOnly = true)
	public List<CircleResponseDto> getCircleList(User user) {
		Set<Role> roles = user.getRoles();

		ValidatorBucket.of()
			.consistOf(UserStateValidator.of(user.getState()))
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.validate();

		if (roles.contains(Role.ADMIN) || roles.contains(Role.PRESIDENT)) {
			return this.circleRepository.findAllByIsDeletedIsFalse()
				.stream()
				.map(circle -> {
					User leader = circle.getLeader()
						.orElse(null);
					return CircleDtoMapper.INSTANCE.toCircleResponseDto(circle, leader);
				})
				.collect(Collectors.toList());
		}

		return this.circleMemberRepository.findByUser_Id(user.getId())
			.stream()
			.filter(circleMember -> circleMember.getStatus() == CircleMemberStatus.MEMBER && !circleMember.getCircle()
				.getIsDeleted())
			.map(circleMember -> {
				User leader = circleMember.getCircle().getLeader()
					.orElse(null);
				return CircleDtoMapper.INSTANCE.toCircleResponseDto(circleMember.getCircle(), leader);
			})
			.collect(Collectors.toList());
	}

	@Transactional
	public UserResponseDto signUp(UserCreateRequestDto dto) {

		// 이메일 우선 조회 -> 가장 기본이 되는 식별자
		Optional<User> byEmail = userRepository.findByEmail(dto.getEmail());
		if (byEmail.isPresent()) {
			User user = byEmail.get();
			UserState state = user.getState();

			//AWAIT, REJECT인 경우 정보 업데이트 진행
			if (state == UserState.AWAIT || state == UserState.REJECT) {
				validateUniqueness(dto.getNickname(), dto.getPhoneNumber(), dto.getStudentId(), user);
				user.updateInfo(dto, passwordEncoder.encode(dto.getPassword()));
				validateUser(dto, user);
				userRepository.save(user);
				return UserDtoMapper.INSTANCE.toUserResponseDto(user, null, null);
			}
			// INACTIVE는 복구 API를 통해 처리
			else if (state == UserState.INACTIVE) {
				throw new BadRequestException(ErrorCode.ROW_ALREADY_EXIST, MessageUtil.USER_INACTIVE_CAN_REJOIN);
			}
			// ACTIVE, DROP은 가입 허용 X
			else if (state == UserState.ACTIVE) {
				throw new BadRequestException(ErrorCode.ROW_ALREADY_EXIST, MessageUtil.EMAIL_ALREADY_EXIST);
			} else if (state == UserState.DROP) {
				throw new BadRequestException(ErrorCode.ROW_ALREADY_EXIST, MessageUtil.USER_DROPPED_CONTACT_EMAIL);
			}
			throw new InternalServerException(ErrorCode.INTERNAL_SERVER, MessageUtil.INTERNAL_SERVER_ERROR);
		}

		// 유령 계정 복구 : 이메일로 사용자 찾지 못한 경우, 다른 필드의 오타로 인해 버려진 계정일 가능성 확인
		Optional<User> byPhone = userRepository.findByPhoneNumber(dto.getPhoneNumber());
		if (byPhone.isPresent()) {
			User ghostuser = byPhone.get();
			UserState state = ghostuser.getState();

			// 찾은 계정이 AWAIT, REJECT인지 확인
			if (state == UserState.AWAIT || state == UserState.REJECT) {

				// 복구하려는 새 이메일이 다른 계정에서 사용중인지 확인(이중 확인)
				userRepository.findByEmail(dto.getEmail()).ifPresent(gu -> {
					if (!gu.getId().equals(ghostuser.getId())) {
						throw new BadRequestException(ErrorCode.ROW_ALREADY_EXIST, MessageUtil.EMAIL_ALREADY_EXIST);
					}
				});
				validateUniqueness(dto.getNickname(), dto.getPhoneNumber(), dto.getStudentId(), ghostuser);
				ghostuser.updateInfo(dto, passwordEncoder.encode(dto.getPassword()));
				validateUser(dto, ghostuser);
				userRepository.save(ghostuser);
				return UserDtoMapper.INSTANCE.toUserResponseDto(ghostuser, null, null);
			}
			// INACTIVE는 복구 API를 통해 처리
			else if (state == UserState.INACTIVE) {
				throw new BadRequestException(ErrorCode.ROW_ALREADY_EXIST, MessageUtil.USER_INACTIVE_CAN_REJOIN);
			} else if (state == UserState.ACTIVE) {
				throw new BadRequestException(ErrorCode.ROW_ALREADY_EXIST, MessageUtil.PHONE_NUMBER_ALREADY_EXIST);
			} else if (state == UserState.DROP) {
				throw new BadRequestException(ErrorCode.ROW_ALREADY_EXIST, MessageUtil.USER_DROPPED_CONTACT_EMAIL);
			}
			throw new InternalServerException(ErrorCode.INTERNAL_SERVER, MessageUtil.INTERNAL_SERVER_ERROR);
		}

		// 신규 사용자 생성
		validateUniqueness(dto.getNickname(), dto.getPhoneNumber(), dto.getStudentId(), null);
		User newUser = User.from(dto, passwordEncoder.encode(dto.getPassword()));
		validateUser(dto, newUser);
		userRepository.save(newUser);
		return UserDtoMapper.INSTANCE.toUserResponseDto(newUser, null, null);
	}

	/**
	 * 졸업생을 사용자로 등록하는 메소드
	 * <p>
	 * 가입 신청이나 학적 증빙 절차를 거치지 않고 바로 등록하며,
	 * 등록 완료 후 {@link CertifiedUserCreatedEvent} 이벤트를 발행하여 기본 리소스를 초기화합니다.
	 * {@link org.springframework.transaction.annotation.Propagation#REQUIRES_NEW}로 개별 사용자를 등록하고 즉시 커밋하여,
	 * 다른 사용자 등록에는 영향을 주지 않습니다.
	 * </p>
	 *
	 * @param dto
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void registerGraduatedUser(GraduatedUserRegisterRequestDto dto) {
		validateEmailUniqueness(dto.email(), null);
		validateUniqueness(dto.nickname(), dto.phoneNumber(), dto.studentId(), null);

		User registeredUser = userRepository.save(
			User.createGraduatedUser(dto.toCreateGraduatedUserCommand(), passwordEncoder.encode(dto.password())));

		eventPublisher.publishEvent(new CertifiedUserCreatedEvent(registeredUser.getId()));
	}

	// 유효성 검사 메서드
	private void validateUser(UserCreateRequestDto dto, User user) {
		ValidatorBucket.of()
			.consistOf(ConstraintValidator.of(user, this.validator))
			.consistOf(PasswordFormatValidator.of(dto.getPassword()))
			.consistOf(AdmissionYearValidator.of(dto.getAdmissionYear()))
			.consistOf(PhoneNumberFormatValidator.of(dto.getPhoneNumber()))
			.validate();
	}

	@Transactional
	public UserSignInResponseDto signIn(UserSignInRequestDto userSignInRequestDto) {
		User user = userRepository.findByEmail(userSignInRequestDto.getEmail()).orElseThrow(
			() -> new UnauthorizedException(
				ErrorCode.INVALID_SIGNIN,
				MessageUtil.EMAIL_INVALID
			)
		);

		/* Validate the input password and user state
		 * The sign-in process is rejected if the user is in BLOCKED, WAIT, or INACTIVE state.
		 */
		ValidatorBucket.of()
			.consistOf(PasswordCorrectValidator.of(
				this.passwordEncoder,
				user.getPassword(),
				userSignInRequestDto.getPassword()))
			.validate();

		ValidatorBucket.of()
			.consistOf(UserStateValidator.of(user.getState()))
			.validate();

		// refreshToken은 redis에 보관
		String refreshToken = jwtTokenProvider.createRefreshToken();
		redisUtils.setRefreshTokenData(refreshToken, user.getId(), StaticValue.JWT_REFRESH_TOKEN_VALID_TIME);

		return UserDtoMapper.INSTANCE.toUserSignInResponseDto(
			jwtTokenProvider.createAccessToken(user.getId(), user.getRoles(), user.getState()),
			refreshToken
		);
	}

	/**
	 * 이메일 중복 확인 메소드
	 *
	 * @param email
	 * @return DuplicatedCheckResponseDto
	 */
	@Transactional(readOnly = true)
	public DuplicatedCheckResponseDto isDuplicatedEmail(String email) {
		Optional<User> userFoundByEmail = userRepository.findByEmail(email);
		if (userFoundByEmail.isPresent()) {
			UserState state = userFoundByEmail.get().getState();
			// ACTIVE, INACTIVE 상태일 경우 중복
			if (state == UserState.ACTIVE || state == UserState.INACTIVE) {
				return UserDtoMapper.INSTANCE.toDuplicatedCheckResponseDto(true);
			}
			// DROP 상태일 경우, 문의 메시지
			else if (state == UserState.DROP) {
				throw new BadRequestException(
					ErrorCode.ROW_ALREADY_EXIST,
					MessageUtil.USER_DROPPED_CONTACT_EMAIL
				);
			}
		}
		return UserDtoMapper.INSTANCE.toDuplicatedCheckResponseDto(false);
	}

	/**
	 * 닉네임 중복 확인 메소드
	 *
	 * @param nickname
	 * @return D
	 * uplicatedCheckResponseDto
	 */
	@Transactional(readOnly = true)
	public DuplicatedCheckResponseDto isDuplicatedNickname(String nickname) {
		Optional<User> userFoundByNickname = userRepository.findByNickname(nickname);
		if (userFoundByNickname.isPresent()) {
			UserState state = userFoundByNickname.get().getState();

			// ACTIVE, INACTIVE 상태일 경우 중복
			if (state == UserState.ACTIVE || state == UserState.INACTIVE) {
				return UserDtoMapper.INSTANCE.toDuplicatedCheckResponseDto(true);
			}
			// DROP 상태일 경우, 문의 메시지
			else if (state == UserState.DROP) {
				throw new BadRequestException(
					ErrorCode.ROW_ALREADY_EXIST,
					MessageUtil.USER_DROPPED_CONTACT_EMAIL
				);
			}
		}
		return UserDtoMapper.INSTANCE.toDuplicatedCheckResponseDto(false);
	}

	@Transactional(readOnly = true)
	public DuplicatedCheckResponseDto isDuplicatedStudentId(String studentId) {
		Optional<User> userFoundByStudentId = userRepository.findByStudentId(studentId);
		if (userFoundByStudentId.isPresent()) {
			UserState state = userFoundByStudentId.get().getState();
			// ACTIVE, INACTIVE 상태일 경우 중복
			if (state == UserState.ACTIVE || state == UserState.INACTIVE) {
				return UserDtoMapper.INSTANCE.toDuplicatedCheckResponseDto(true);
			}
			// DROP 상태일 경우, 문의 메시지
			else if (state == UserState.DROP) {
				throw new BadRequestException(
					ErrorCode.ROW_ALREADY_EXIST,
					MessageUtil.USER_DROPPED_CONTACT_EMAIL
				);
			}
		}
		return UserDtoMapper.INSTANCE.toDuplicatedCheckResponseDto(false);
	}

	/**
	 * 전화번호 중복 확인 메소드
	 *
	 * @param phoneNumber
	 * @return DuplicatedCheckResponseDto
	 */
	@Transactional(readOnly = true)
	public DuplicatedCheckResponseDto isDuplicatedPhoneNumber(String phoneNumber) {
		Optional<User> userFoundByPhoneNumber = userRepository.findByPhoneNumber(phoneNumber);
		if (userFoundByPhoneNumber.isPresent()) {
			UserState state = userFoundByPhoneNumber.get().getState();
			// ACTIVE, INACTIVE 상태일 경우 중복
			if (state == UserState.ACTIVE || state == UserState.INACTIVE) {
				return UserDtoMapper.INSTANCE.toDuplicatedCheckResponseDto(true);
			}
			// DROP 상태일 경우, 문의 메시지
			else if (state == UserState.DROP) {
				throw new BadRequestException(
					ErrorCode.ROW_ALREADY_EXIST,
					MessageUtil.USER_DROPPED_CONTACT_EMAIL
				);
			}
		}
		return UserDtoMapper.INSTANCE.toDuplicatedCheckResponseDto(false);
	}

	@Transactional
	public UserResponseDto update(User user, UserUpdateRequestDto userUpdateRequestDto, MultipartFile profileImage) {
		User srcUser = userRepository.findById(user.getId()).orElseThrow(
			() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.USER_NOT_FOUND
			)
		);

		// 닉네임이 변경되었을 때 중복 체크
		if (srcUser.getNickname() == null
			|| !srcUser.getNickname().equals(userUpdateRequestDto.getNickname())
		) {
			validateNicknameUniqueness(userUpdateRequestDto.getNickname(), srcUser);
		}

		// 전화번호가 변경되었을 때 중복 체크
		if (srcUser.getPhoneNumber() == null
			|| !srcUser.getPhoneNumber().equals(userUpdateRequestDto.getPhoneNumber())
		) {
			validatePhoneNumberUniqueness(userUpdateRequestDto.getPhoneNumber(), srcUser);
		}

		UserProfileImage userProfileImage = srcUser.getUserProfileImage();

		if (profileImage != null && !profileImage.isEmpty()) {
			if (srcUser.getUserProfileImage() == null) {
				userProfileImage = UserProfileImage.of(
					user,
					uuidFileService.saveFile(profileImage, FilePath.USER_PROFILE)
				);
			} else {
				userProfileImage.setUuidFile(
					uuidFileService.updateFile(
						srcUser.getUserProfileImage().getUuidFile(),
						profileImage,
						FilePath.USER_PROFILE
					)
				);
			}
		}

		srcUser.update(userUpdateRequestDto.getNickname(), userProfileImage, userUpdateRequestDto.getPhoneNumber());

		User updatedUser = userRepository.save(srcUser);

		return UserDtoMapper.INSTANCE.toUserResponseDto(updatedUser, null, null);
	}

	// private method
	@Transactional
	public UserResponseDto updatePassword(
		User user,
		UserUpdatePasswordRequestDto userUpdatePasswordRequestDto
	) {
		Set<Role> roles = user.getRoles();

		ValidatorBucket.of()
			.consistOf(UserStateValidator.of(user.getState()))
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.consistOf(PasswordCorrectValidator.of(
				this.passwordEncoder,
				user.getPassword(),
				userUpdatePasswordRequestDto.getOriginPassword())
			)
			.consistOf(PasswordFormatValidator.of(userUpdatePasswordRequestDto.getUpdatedPassword()))
			.validate();

		user.setPassword(this.passwordEncoder.encode(userUpdatePasswordRequestDto.getUpdatedPassword()));
		User updatedUser = this.userRepository.save(user);

		return UserDtoMapper.INSTANCE.toUserResponseDto(updatedUser, null, null);
	}

	//유저정보 완전 삭제
	@Transactional
	public UserResponseDto eraseUserData(User requestuser, String userId) {
		Set<Role> roles = requestuser.getRoles();

		//삭제할 유저
		User deleteUser = getUser(userId);

		//관리자 or 대표만 삭제 가능
		ValidatorBucket.of()
			.consistOf(UserStateValidator.of(requestuser.getState()))
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.consistOf(UserRoleWithoutAdminValidator.of(roles, Set.of(Role.ADMIN, Role.PRESIDENT)))
			.validate();

		//사용중인 사물함이 있을경우 사물함 반환처리
		this.lockerRepository.findByUser_Id(deleteUser.getId())
			.ifPresent(locker -> {
				locker.returnLocker();
				this.lockerRepository.save(locker);

				LockerLog lockerLog = LockerLog.of(
					locker.getLockerNumber(),
					locker.getLocation().getName(),
					deleteUser.getEmail(),
					deleteUser.getName(),
					LockerLogAction.RETURN,
					"사용자 탈퇴"
				);

				this.lockerLogRepository.save(lockerLog);

			});

		// 가입된 동아리가 있다면 탈퇴
		this.circleMemberRepository.findByUser_Id(deleteUser.getId())
			.forEach(circleMember ->
				this.updateStatus(circleMember.getId(), CircleMemberStatus.LEAVE)
			);

		// FIXME: 영구삭제 시 UuidFile, User FK 제약 조건 위반 발생
		List<UserAcademicRecordApplication> userAcademicRecordApplicationList = userAcademicRecordApplicationRepository.findByUserId(
			deleteUser.getId());

		if (!userAcademicRecordApplicationList.isEmpty()) {
			// 재학 인증 신청 이미지 파일이 있다면 삭제
			uuidFileService.deleteFileList(
				userAcademicRecordApplicationList
					.stream()
					.flatMap(
						userAcademicRecordApplication -> userAcademicRecordApplication.getUserAcademicRecordAttachImageList()
							.stream()
							.map(UserAcademicRecordApplicationAttachImage::getUuidFile)).toList()
			);

			// 재학 인증 신청 기록이 있다면 삭제
			userAcademicRecordApplicationRepository.deleteAll(userAcademicRecordApplicationList);
		}
		this.userRepository.delete(deleteUser);

		return UserDtoMapper.INSTANCE.toUserResponseDto(deleteUser, null, null);
	}

	@Transactional
	public UserResponseDto leave(User user) {
		Set<Role> roles = user.getRoles();

		ValidatorBucket.of()
			.consistOf(UserStateValidator.of(user.getState()))
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.consistOf(UserRoleWithoutAdminValidator.of(roles, Set.of(Role.COMMON, Role.PROFESSOR)))
			.validate();

		this.lockerRepository.findByUser_Id(user.getId())
			.ifPresent(locker -> {
				locker.returnLocker();
				this.lockerRepository.save(locker);

				LockerLog lockerLog = LockerLog.of(
					locker.getLockerNumber(),
					locker.getLocation().getName(),
					user.getEmail(),
					user.getName(),
					LockerLogAction.RETURN,
					"사용자 탈퇴"
				);

				this.lockerLogRepository.save(lockerLog);

			});

		// Change user role to NONE
		userRoleService.updateRole(user, Role.NONE);

		// Leave from circle where user joined
		this.circleMemberRepository.findByUser_Id(user.getId())
			.forEach(circleMember ->
				this.updateStatus(circleMember.getId(), CircleMemberStatus.LEAVE)
			);

		User entity = this.updateState(user.getId(), UserState.INACTIVE)
			.orElseThrow(() -> new InternalServerException(
				ErrorCode.INTERNAL_SERVER,
				MessageUtil.INTERNAL_SERVER_ERROR
			));
		return UserDtoMapper.INSTANCE.toUserResponseDto(entity, null, null);
	}

	@Scheduled(cron = "0 0 0 * * ?")
	public void deleteUser() {
		LocalDateTime dueDate = LocalDateTime.now().minusYears(5);

		userRepository.findAllByState(UserState.INACTIVE).stream()
			.filter(user -> user.getUpdatedAt().isBefore(dueDate))
			.forEach(user -> {
				user.delete();
				userRepository.save(user);
			});
	}

	private Optional<CircleMember> updateStatus(String applicationId, CircleMemberStatus targetStatus) {
		return this.circleMemberRepository.findById(applicationId).map(
			circleMember -> {
				circleMember.setStatus(targetStatus);
				return this.circleMemberRepository.save(circleMember);
			}
		);
	}

	private Optional<User> updateState(String id, UserState state) {
		return this.userRepository.findById(id).map(
			srcUser -> {
				srcUser.setState(state);

				this.userRepository.save(srcUser);
				return srcUser;
			}
		);
	}

	@Transactional
	public UserResponseDto dropUser(User requestUser, String userId, String dropReason) {
		Set<Role> roles = requestUser.getRoles();

		User droppedUser = this.userRepository.findById(userId).orElseThrow(
			() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.USER_NOT_FOUND
			)
		);

		String droppedUserEmail = droppedUser.getEmail();
		String droppedUserName = droppedUser.getName();

		ValidatorBucket.of()
			.consistOf(UserStateValidator.of(requestUser.getState()))
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.consistOf(UserRoleValidator.of(roles, Set.of()))
			.consistOf(UserRoleWithoutAdminValidator.of(droppedUser.getRoles(), Set.of(Role.COMMON, Role.PROFESSOR)))
			.validate();

		lockerRepository.findByUser_Id(userId)
			.ifPresent(locker -> {
				locker.returnLocker();
				this.lockerRepository.save(locker);

				LockerLog lockerLog = LockerLog.of(
					locker.getLockerNumber(),
					locker.getLocation().getName(),
					droppedUserEmail,
					droppedUserName,
					LockerLogAction.RETURN,
					"사용자 추방"
				);

				this.lockerLogRepository.save(lockerLog);
			});

		userRoleService.updateRole(droppedUser, Role.NONE);

		droppedUser.updateRejectionOrDropReason(dropReason);
		droppedUser = this.updateState(userId, UserState.DROP)
			.orElseThrow(() -> new InternalServerException(
				ErrorCode.INTERNAL_SERVER,
				MessageUtil.INTERNAL_SERVER_ERROR
			));

		userRepository.save(droppedUser);

		return UserDtoMapper.INSTANCE.toUserResponseDto(droppedUser, null, null);
	}

	/**
	 * 사용자 정보의 유일성을 검증하는 헬퍼 메소드.
	 * DB에 저장하기 전에 호출, 애플리케이션 단에서 미리 중복 확인
	 *
	 * @param nickName
	 * @param phoneNumber
	 * @param studentId
	 * @param currentUser 정보를 수정하려는 기존 사용자 (신규 가입 시에는 null)
	 */
	private void validateUniqueness(String nickName, String phoneNumber, String studentId, User currentUser) {
		validateNicknameUniqueness(nickName, currentUser);
		validatePhoneNumberUniqueness(phoneNumber, currentUser);

		if (studentId != null) {
			validateStudentIdUniqueness(studentId, currentUser);
		}
	}

	private void validateNicknameUniqueness(String nickname, User currentUser) {
		userRepository.findByNickname(nickname).ifPresent(foundUser -> {
			if (currentUser == null || !foundUser.getId().equals(currentUser.getId())) {
				if (foundUser.getState() == UserState.ACTIVE || foundUser.getState() == UserState.INACTIVE) {
					throw new BadRequestException(
						ErrorCode.ROW_ALREADY_EXIST,
						MessageUtil.NICKNAME_ALREADY_EXIST);

				} else if (foundUser.getState() == UserState.DROP) {
					throw new BadRequestException(
						ErrorCode.ROW_ALREADY_EXIST,
						MessageUtil.USER_DROPPED_CONTACT_EMAIL);
				}
			}
		});
	}

	private void validatePhoneNumberUniqueness(String phoneNumber, User currentUser) {
		userRepository.findByPhoneNumber(phoneNumber).ifPresent(foundUser -> {
			if (currentUser == null || !foundUser.getId().equals(currentUser.getId())) {
				if (foundUser.getState() == UserState.ACTIVE || foundUser.getState() == UserState.INACTIVE) {
					throw new BadRequestException(
						ErrorCode.ROW_ALREADY_EXIST,
						MessageUtil.PHONE_NUMBER_ALREADY_EXIST);

				} else if (foundUser.getState() == UserState.DROP) {
					throw new BadRequestException(
						ErrorCode.ROW_ALREADY_EXIST,
						MessageUtil.USER_DROPPED_CONTACT_EMAIL);
				}
			}
		});
	}

	private void validateStudentIdUniqueness(String studentId, User currentUser) {
		userRepository.findByStudentId(studentId).ifPresent(foundUser -> {
			if (currentUser == null || !foundUser.getId().equals(currentUser.getId())) {
				if (foundUser.getState() == UserState.ACTIVE || foundUser.getState() == UserState.INACTIVE) {
					throw new BadRequestException(
						ErrorCode.ROW_ALREADY_EXIST,
						MessageUtil.STUDENT_ID_ALREADY_EXIST);

				} else if (foundUser.getState() == UserState.DROP) {
					throw new BadRequestException(
						ErrorCode.ROW_ALREADY_EXIST,
						MessageUtil.USER_DROPPED_CONTACT_EMAIL);
				}
			}
		});
	}

	private void validateEmailUniqueness(String email, User currentUser) {
		userRepository.findByEmail(email).ifPresent(foundUser -> {
			if (currentUser == null || !foundUser.getId().equals(currentUser.getId())) {
				if (foundUser.getState() == UserState.ACTIVE || foundUser.getState() == UserState.INACTIVE) {
					throw new BadRequestException(
						ErrorCode.ROW_ALREADY_EXIST,
						MessageUtil.EMAIL_ALREADY_EXIST);

				} else if (foundUser.getState() == UserState.DROP) {
					throw new BadRequestException(
						ErrorCode.ROW_ALREADY_EXIST,
						MessageUtil.USER_DROPPED_CONTACT_EMAIL);
				}
			}
		});
	}

	@Transactional(readOnly = true)
	public UserAdmissionResponseDto findAdmissionById(User requestUser, String admissionId) {
		Set<Role> roles = requestUser.getRoles();

		ValidatorBucket.of()
			.consistOf(UserStateValidator.of(requestUser.getState()))
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.consistOf(UserRoleValidator.of(roles, Set.of()))
			.validate();

		return UserDtoMapper.INSTANCE.toUserAdmissionResponseDto(
			this.userAdmissionRepository.findById(admissionId)
				.orElseThrow(() -> new BadRequestException(
					ErrorCode.ROW_DOES_NOT_EXIST,
					MessageUtil.USER_APPLY_NOT_FOUND
				))
		);
	}

	@Transactional(readOnly = true)
	public Page<UserAdmissionsResponseDto> findAllAdmissions(
		User requestUser,
		String name,
		Integer pageNum
	) {
		Set<Role> roles = requestUser.getRoles();

		ValidatorBucket.of()
			.consistOf(UserStateValidator.of(requestUser.getState()))
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.consistOf(UserRoleValidator.of(roles, Set.of()))
			.validate();

		if (name == null) {
			return userAdmissionRepository.findAll(this.pageableFactory.create(pageNum, StaticValue.DEFAULT_PAGE_SIZE))
				.map(UserDtoMapper.INSTANCE::toUserAdmissionsResponseDto);
		} else {
			return this.userAdmissionRepository.findAllByUserName(name,
					this.pageableFactory.create(pageNum, StaticValue.DEFAULT_PAGE_SIZE))
				.map(UserDtoMapper.INSTANCE::toUserAdmissionsResponseDto);
		}
	}

	@Transactional
	public UserAdmissionResponseDto createAdmission(User user,
		UserAdmissionCreateRequestDto userAdmissionCreateRequestDto, List<MultipartFile> userAdmissionAttachImageList) {
		if (!user.getEmail().equals(userAdmissionCreateRequestDto.getEmail())) {
			throw new BadRequestException(
				ErrorCode.INVALID_PARAMETER,
				MessageUtil.EMAIL_INVALID
			);
		}

		if (this.userAdmissionRepository.existsByUser_Id(user.getId())) {
			throw new BadRequestException(
				ErrorCode.ROW_ALREADY_EXIST,
				MessageUtil.USER_ALREADY_APPLY
			);
		}

		if (!(user.getState().equals(UserState.AWAIT) || user.getState().equals(UserState.REJECT))) {
			throw new BadRequestException(
				ErrorCode.INVALID_REQUEST_USER_STATE,
				MessageUtil.INVALID_USER_APPLICATION_USER_STATE
			);
		}

		if (userAdmissionAttachImageList.isEmpty()) {
			throw new BadRequestException(
				ErrorCode.INVALID_PARAMETER,
				MessageUtil.USER_ADMISSION_MUST_HAVE_IMAGE
			);
		}

		List<UuidFile> uuidFileList = uuidFileService.saveFileList(userAdmissionAttachImageList,
			FilePath.USER_ADMISSION);

		UserAdmission userAdmission = UserAdmission.of(
			user,
			uuidFileList,
			userAdmissionCreateRequestDto.getDescription()
		);

		ValidatorBucket.of()
			.consistOf(UserStateIsNotDropAndActiveValidator.of(user.getState()))
			.consistOf(ConstraintValidator.of(userAdmission, this.validator))
			.validate();

		userRepository.save(updateState(user.getId(), UserState.AWAIT)
			.orElseThrow(() -> new InternalServerException(
				ErrorCode.INTERNAL_SERVER,
				MessageUtil.ADMISSION_EXCEPTION
			))
		);

		return UserDtoMapper.INSTANCE.toUserAdmissionResponseDto(this.userAdmissionRepository.save(userAdmission));
	}

	public UserAdmissionResponseDto getCurrentUserAdmission(User user) {
		UserAdmission userAdmission = userAdmissionRepository.findByUser_Id(user.getId()).orElseThrow(
			() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.USER_APPLY_NOT_FOUND
			)
		);

		return UserDtoMapper.INSTANCE.toUserAdmissionResponseDto(userAdmission);
	}

	@Transactional
	public UserAdmissionResponseDto accept(
		User requestUser,
		String admissionId
	) {
		Set<Role> roles = requestUser.getRoles();

		UserAdmission userAdmission = this.userAdmissionRepository.findById(admissionId).orElseThrow(
			() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_APPLY_NOT_FOUND)
		);

		ValidatorBucket.of()
			.consistOf(UserStateValidator.of(requestUser.getState()))
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.consistOf(UserRoleValidator.of(roles, Set.of()))
			.validate();

		//승인 전 중복체크
		validateDuplicateBeforeAccept(userAdmission.getUser());

		// 사용자 역할을 NONE에서 COMMON으로 변경
		userRoleService.updateRole(userAdmission.getUser(), Role.COMMON);

		// Add admission log
		UserAdmissionLog userAdmissionLog = UserAdmissionLog.of(
			userAdmission.getUser().getEmail(),
			userAdmission.getUser().getName(),
			requestUser.getEmail(),
			requestUser.getName(),
			UserAdmissionLogAction.ACCEPT,
			userAdmission.getUserAdmissionAttachImageList()
				.stream()
				.map(UserAdmissionAttachImage::getUuidFile)
				.toList(),
			userAdmission.getDescription(),
			null
		);

		this.userAdmissionLogRepository.save(userAdmissionLog);

		// Remove the admission
		this.userAdmissionRepository.delete(userAdmission);

		return UserDtoMapper.INSTANCE.toUserAdmissionResponseDto(
			userAdmissionLog,
			this.updateState(userAdmission.getUser().getId(), UserState.ACTIVE)
				.orElseThrow(() -> new InternalServerException(
					ErrorCode.INTERNAL_SERVER,
					MessageUtil.ADMISSION_EXCEPTION
				))
		);
	}

	@Transactional
	public UserAdmissionResponseDto reject(
		User requestUser,
		String admissionId,
		String rejectReason
	) {
		Set<Role> roles = requestUser.getRoles();

		UserAdmission userAdmission = this.userAdmissionRepository.findById(admissionId).orElseThrow(
			() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_APPLY_NOT_FOUND)
		);

		User targetUser = userAdmission.getUser();

		ValidatorBucket.of()
			.consistOf(UserStateValidator.of(requestUser.getState()))
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.consistOf(UserRoleValidator.of(roles, Set.of()))
			.validate();

		// Add admission log
		UserAdmissionLog userAdmissionLog = UserAdmissionLog.of(
			userAdmission.getUser().getEmail(),
			userAdmission.getUser().getName(),
			requestUser.getEmail(),
			requestUser.getName(),
			UserAdmissionLogAction.REJECT,
			userAdmission.getUserAdmissionAttachImageList()
				.stream()
				.map(UserAdmissionAttachImage::getUuidFile)
				.toList(),
			userAdmission.getDescription(),
			rejectReason
		);

		userAdmissionLogRepository.save(userAdmissionLog);

		userAdmissionRepository.delete(userAdmission);

		targetUser.updateRejectionOrDropReason(rejectReason);

		targetUser = this.updateState(targetUser.getId(), UserState.REJECT)
			.orElseThrow(() -> new InternalServerException(
				ErrorCode.INTERNAL_SERVER,
				MessageUtil.ADMISSION_EXCEPTION
			));

		userRepository.save(targetUser);

		return UserDtoMapper.INSTANCE.toUserAdmissionResponseDto(
			userAdmissionLog,
			targetUser
		);
	}

	@Transactional
	public UserResponseDto restore(
		User requestUser,
		String userId
	) {
		Set<Role> roles = requestUser.getRoles();

		User restoredUser = this.userRepository.findById(userId).orElseThrow(
			() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND)
		);
		ValidatorBucket.of()
			.consistOf(UserRoleValidator.of(roles, Set.of()))
			.consistOf(UserStateIsDropOrIsInActiveValidator.of(restoredUser.getState()))
			.validate();

		userRoleService.updateRole(restoredUser, Role.COMMON);

		User entity = this.updateState(restoredUser.getId(), UserState.ACTIVE)
			.orElseThrow(() -> new InternalServerException(
				ErrorCode.INTERNAL_SERVER,
				MessageUtil.INTERNAL_SERVER_ERROR
			));
		return UserDtoMapper.INSTANCE.toUserResponseDto(entity, null, null);
	}

	@Transactional
	public UserSignInResponseDto updateToken(String refreshToken) {
		// STEP1 : refreshToken으로 맵핑된 유저 찾기
		User user = this.userRepository.findById(this.getUserIdFromRefreshToken(refreshToken)).orElseThrow(
			() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.INVALID_REFRESH_TOKEN)
		);

		this.userRepository.findById(getUserIdFromRefreshToken(refreshToken));

		ValidatorBucket.of()
			.consistOf(UserRoleIsNoneValidator.of(user.getRoles()))
			.consistOf(UserStateValidator.of(user.getState()))
			.validate();

		// STEP2 : 새로운 accessToken 제공
		String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRoles(), user.getState());

		return UserDtoMapper.INSTANCE.toUserSignInResponseDto(newAccessToken, refreshToken);
	}

	private String getUserIdFromRefreshToken(String refreshToken) {
		return Optional.ofNullable(redisUtils.getRefreshTokenData(refreshToken))
			.orElseThrow(() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.INVALID_REFRESH_TOKEN
			));
	}

	public UserSignOutResponseDto signOut(User user, UserSignOutRequestDto userSignOutRequestDto) {
		redisUtils.addToBlacklist(userSignOutRequestDto.getAccessToken());
		redisUtils.deleteRefreshTokenData(userSignOutRequestDto.getRefreshToken());
		String fcmToken = userSignOutRequestDto.getFcmToken();

		if (fcmToken != null) {
			fcmUtils.removeFcmToken(user, fcmToken);
		}

		return UserDtoMapper.INSTANCE.toUserSignOutResponseDto("로그아웃 성공");
	}

	public UserFindIdResponseDto findUserId(UserFindIdRequestDto userIdFindRequestDto) {
		User user = this.userRepository.findByPhoneNumberAndName(
			userIdFindRequestDto.getPhoneNumber().trim(),
			userIdFindRequestDto.getName().trim()
		).orElseThrow(() -> new BadRequestException(
			ErrorCode.ROW_DOES_NOT_EXIST,
			MessageUtil.USER_NOT_FOUND
		));

		return UserDtoMapper.INSTANCE.toUserfindIdResponseDto(user);
	}

	@Transactional(readOnly = true)
	public List<UserResponseDto> findByStudentId(String studentId) {
		List<User> userList = this.userRepository.findByStudentIdAndStateAndAcademicStatus(studentId, UserState.ACTIVE,
			AcademicStatus.ENROLLED);

		if (userList.isEmpty()) {
			throw new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.USER_NOT_FOUND
			);
		}

		return userList.stream()
			.map(user -> {
				if (user.getRoles().contains(Role.LEADER_CIRCLE)) {
					List<String> circleIdIfLeader = getCircleIdsIfLeader(user);
					List<String> circleNameIfLeader = getCircleNamesIfLeader(user);
					return UserDtoMapper.INSTANCE.toUserResponseDto(user, circleIdIfLeader, circleNameIfLeader);
				} else {
					return UserDtoMapper.INSTANCE.toUserResponseDto(user, null, null);
				}
			})
			.collect(Collectors.toList());
	}

	public void exportUserListToExcel(HttpServletResponse response) {
		String timePrefix = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(LocalDateTime.now());
		String fileName = timePrefix + "_사용자명단";

		List<String> headerStringList = List.of(
			"아이디(이메일)",
			"이름",
			"학번",
			"입학년도",
			"역할",
			"상태",
			"닉네임",
			"학과(학부)",
			"학적 상태",
			"현재 등록 완료된 학기",
			"졸업 년도",
			"졸업 시기",
			"전화 번호",
			"동아리명 목록(동아리장일 경우)",
			"가입 거절/추방 사유",
			"동문네트워크 가입일",
			"사용자 정보 최종 수정일"
		);

		LinkedHashMap<String, List<UserResponseDto>> sheetDataMap = new LinkedHashMap<>();
		for (UserState state : UserState.values()) {
			if (state == UserState.DELETED) {
				continue;
			}
			String sheetName = state.getDescription() + " 유저";
			List<UserResponseDto> sheetData = getUserResponseDtosByState(state);

			sheetDataMap.put(sheetName, sheetData);
		}

		userExcelService.generateExcel(response, fileName, headerStringList, sheetDataMap);
	}

	@Transactional
	public UserResponseDto updateUserIsV2(User user) {
		user = this.getUser(user.getId());

		user.setIsV2(true);

		Set<Role> roles = user.getRoles();

		if (roles.contains(Role.LEADER_CIRCLE)) {
			List<Circle> ownCircles = this.circleRepository.findByLeader_Id(user.getId());
			if (ownCircles.isEmpty()) {
				throw new InternalServerException(
					ErrorCode.INTERNAL_SERVER,
					MessageUtil.NO_ASSIGNED_CIRCLE_FOR_LEADER
				);
			}

			return UserDtoMapper.INSTANCE.toUserResponseDto(
				user,
				ownCircles.stream().map(Circle::getId).collect(Collectors.toList()),
				ownCircles.stream().map(Circle::getName).collect(Collectors.toList())
			);
		}

		userRepository.save(user);

		return UserDtoMapper.INSTANCE.toUserResponseDto(user, null, null);
	}

	@Transactional
	public UserFcmTokenResponseDto registerFcmToken(User user, UserFcmCreateRequestDto userFcmCreateRequestDto) {
		String fcmToken = userFcmCreateRequestDto.getFcmToken();
		String refreshToken = userFcmCreateRequestDto.getRefreshToken();
		String userIdFromRedis = getUserIdFromRefreshToken(refreshToken);

		// 1. 유효한 refreshToken인지 검증
		if (!user.getId().equals(userIdFromRedis)) {
			throw new BadRequestException(
				ErrorCode.INVALID_SIGNIN,
				MessageUtil.INVALID_REFRESH_TOKEN
			);
		}
		// 2. fcmToken 최신화
		fcmUtils.cleanInvalidFcmTokens(user);
		// 3. fcmToken 추가
		fcmUtils.addFcmToken(user, refreshToken, fcmToken);
		return UserDtoMapper.INSTANCE.toUserFcmTokenResponseDto(user);
	}

	@Transactional
	public UserFcmTokenResponseDto getUserFcmToken(User user) {
		fcmUtils.cleanInvalidFcmTokens(user);
		return UserDtoMapper.INSTANCE.toUserFcmTokenResponseDto(user);
	}

	// private methods
	private List<String> getCircleNamesIfLeader(User user) {
		List<Circle> circleList = this.circleRepository.findByLeader_Id(user.getId());

		return circleList.stream()
			.map(Circle::getName)
			.collect(Collectors.toList());
	}

	private List<String> getCircleIdsIfLeader(User user) {
		List<Circle> circleList = this.circleRepository.findByLeader_Id(user.getId());

		return circleList.stream()
			.map(Circle::getId)
			.collect(Collectors.toList());
	}

	private BoardResponseDto toBoardResponseDto(Board board, Role userRole) {
		List<String> roles = new ArrayList<>(Arrays.asList(board.getCreateRoles().split(",")));
		Boolean writable = roles.stream().anyMatch(str -> userRole.getValue().contains(str));
		String circleId = Optional.ofNullable(board.getCircle()).map(Circle::getId).orElse(null);
		String circleName = Optional.ofNullable(board.getCircle()).map(Circle::getName).orElse(null);
		return BoardDtoMapper.INSTANCE.toBoardResponseDto(
			board,
			roles,
			writable,
			circleId,
			circleName
		);
	}

	private User getUser(String userId) {
		return userRepository.findById(userId).orElseThrow(
			() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.USER_NOT_FOUND
			)
		);
	}

	private Board getBoard(String boardId) {
		return boardRepository.findById(boardId).orElseThrow(
			() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.BOARD_NOT_FOUND
			)
		);
	}

	private Long getNumOfComment(Post post) {
		return this.postRepository.countAllCommentByPost_Id(post.getId());
	}

	private Long getNumOfPostLikes(Post post) {
		return likePostRepository.countByPostId(post.getId());
	}

	private Long getNumOfPostFavorites(Post post) {
		return favoritePostRepository.countByPostId(post.getId());
	}

	private List<User> getUsersByState(UserState state) {
		if (state.equals(UserState.AWAIT)) {
			return userAdmissionRepository.findAll()
				.stream()
				.map(UserAdmission::getUser)
				.toList();
		} else {
			return userRepository.findAllByState(state);
		}
	}

	private List<UserResponseDto> getUserResponseDtosByState(UserState state) {
		List<User> users = getUsersByState(state);
		return users.stream()
			.map(user -> {
				if (user.getRoles().contains(Role.LEADER_CIRCLE)) {
					List<String> circleIdIfLeader = getCircleIdsIfLeader(user);
					List<String> circleNameIfLeader = getCircleNamesIfLeader(user);
					return UserDtoMapper.INSTANCE.toUserResponseDto(user, circleIdIfLeader, circleNameIfLeader);
				} else {
					return UserDtoMapper.INSTANCE.toUserResponseDto(user);
				}
			}).toList();
	}

	private void validateDuplicateBeforeAccept(User user) {
		// 이메일, 닉네임, 전화번호, 학번이 다른 ACTIVE 사용자와 중복되는지 확인
		userRepository.findByEmail(user.getEmail()).ifPresent(existingUser -> {
			if (!existingUser.getId().equals(user.getId()) && existingUser.getState() == UserState.ACTIVE) {
				throw new BadRequestException(
					ErrorCode.ROW_ALREADY_EXIST,
					MessageUtil.EMAIL_ALREADY_EXIST
				);
			}
		});

		userRepository.findByNickname(user.getNickname()).ifPresent(existingUser -> {
			if (!existingUser.getId().equals(user.getId()) && existingUser.getState() == UserState.ACTIVE) {
				throw new BadRequestException(
					ErrorCode.ROW_ALREADY_EXIST,
					MessageUtil.NICKNAME_ALREADY_EXIST
				);
			}
		});

		userRepository.findByPhoneNumber(user.getPhoneNumber()).ifPresent(existingUser -> {
			if (!existingUser.getId().equals(user.getId()) && existingUser.getState() == UserState.ACTIVE) {
				throw new BadRequestException(
					ErrorCode.ROW_ALREADY_EXIST,
					MessageUtil.PHONE_NUMBER_ALREADY_EXIST
				);
			}
		});

		if (user.getStudentId() != null) {
			userRepository.findByStudentId(user.getStudentId()).ifPresent(existingUser -> {
				if (!existingUser.getId().equals(user.getId()) && existingUser.getState() == UserState.ACTIVE) {
					throw new BadRequestException(
						ErrorCode.ROW_ALREADY_EXIST,
						MessageUtil.STUDENT_ID_ALREADY_EXIST
					);
				}
			});
		}
	}

	/**
	 * INACTIVE 사용자 계정 복구 API
	 *
	 * @param email 복구할 사용자 이메일
	 * @return UserSignInResponseDto 로그인 응답 (액세스 토큰, 리프레시 토큰)
	 */
	@Transactional
	public UserSignInResponseDto recoverUser(String email) {
		User user = userRepository.findByEmail(email).orElseThrow(
			() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.USER_NOT_FOUND
			)
		);

		// 사용자 상태가 INACTIVE인지 확인
		if (user.getState() != UserState.INACTIVE) {
			throw new BadRequestException(
				ErrorCode.INVALID_PARAMETER,
				MessageUtil.USER_RECOVER_INVALID_STATE
			);
		}

		// 사용자 상태를 ACTIVE로 변경
		user.setState(UserState.ACTIVE);

		// 역할을 COMMON으로 설정
		Set<Role> roles = user.getRoles();
		roles.clear();
		roles.add(Role.COMMON);

		userRepository.save(user);

		// 토큰 발급
		String refreshToken = jwtTokenProvider.createRefreshToken();
		redisUtils.setRefreshTokenData(refreshToken, user.getId(), StaticValue.JWT_REFRESH_TOKEN_VALID_TIME);

		return UserDtoMapper.INSTANCE.toUserSignInResponseDto(
			jwtTokenProvider.createAccessToken(user.getId(), user.getRoles(), user.getState()),
			refreshToken
		);
	}

}
