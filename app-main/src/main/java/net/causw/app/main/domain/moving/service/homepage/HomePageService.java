package net.causw.app.main.domain.moving.service.homepage;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import net.causw.app.main.core.aop.annotation.MeasureTime;
import net.causw.app.main.domain.community.entity.board.Board;
import net.causw.app.main.domain.community.entity.post.Post;
import net.causw.app.main.domain.community.event.board.BoardRepository;
import net.causw.app.main.domain.community.repository.post.FavoritePostRepository;
import net.causw.app.main.domain.community.repository.post.LikePostRepository;
import net.causw.app.main.domain.community.repository.post.PostRepository;
import net.causw.app.main.domain.moving.dto.board.BoardResponseDto;
import net.causw.app.main.domain.moving.dto.homepage.HomePageResponseDto;
import net.causw.app.main.domain.moving.dto.util.dtoMapper.BoardDtoMapper;
import net.causw.app.main.domain.moving.dto.util.dtoMapper.PostDtoMapper;
import net.causw.app.main.domain.moving.model.entity.circle.Circle;
import net.causw.app.main.domain.moving.policy.StatusPolicy;
import net.causw.app.main.domain.user.entity.user.User;
import net.causw.app.main.domain.user.enums.user.Role;
import net.causw.app.main.domain.user.repository.user.UserRepository;
import net.causw.app.main.domain.user.util.UserRoleIsNoneValidator;
import net.causw.app.main.domain.user.util.UserStateValidator;
import net.causw.app.main.shared.ValidatorBucket;
import net.causw.app.main.shared.pageable.PageableFactory;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@MeasureTime
@Service
@RequiredArgsConstructor
public class HomePageService {

	private final UserRepository userRepository;
	private final PostRepository postRepository;
	private final BoardRepository boardRepository;
	private final LikePostRepository likePostRepository;
	private final FavoritePostRepository favoritePostRepository;
	private final PageableFactory pageableFactory;

	public List<HomePageResponseDto> getHomePage(User user) {
		Set<Role> roles = user.getRoles();

		ValidatorBucket.of()
			.consistOf(UserStateValidator.of(user.getState()))
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.validate();

		List<Board> boards = boardRepository.findByCircle_IdIsNullAndIsDeletedOrderByCreatedAtAsc(false);
		if (boards.isEmpty()) {
			throw new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.BOARD_NOT_FOUND
			);
		}

		return boards
			.stream()
			.map(board -> HomePageResponseDto.of(
				toBoardResponseDto(board, roles),
				postRepository.findAllByBoard_IdAndIsDeletedIsFalseOrderByCreatedAtDesc(board.getId(),
						pageableFactory.create(0, StaticValue.HOME_POST_PAGE_SIZE))
					.map(post -> PostDtoMapper.INSTANCE.toPostsResponseDto(
						post,
						postRepository.countAllCommentByPost_Id(post.getId()),
						getNumOfPostLikes(post),
						getNumOfPostFavorites(post),
						!post.getPostAttachImageList().isEmpty() ? post.getPostAttachImageList().get(0) : null,
						StatusPolicy.isPostVote(post),
						post.getForm() != null
					)))
			)
			.collect(Collectors.toList());
	}

	public List<HomePageResponseDto> getAlumniHomePage(User user) {
		Set<Role> roles = user.getRoles();

		List<Board> boards = boardRepository.findByIsHomeTrueAndIsAlumniTrueAndIsDeletedFalse();
		if (boards.isEmpty()) {
			throw new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.BOARD_NOT_FOUND
			);
		}

		return boards
			.stream()
			.map(board -> HomePageResponseDto.of(
				toBoardResponseDto(board, roles),
				postRepository.findAllByBoard_IdAndIsDeletedIsFalseOrderByCreatedAtDesc(board.getId(),
						pageableFactory.create(0, StaticValue.ALUMNI_HOME_POST_PAGE_SIZE))
					.map(post -> PostDtoMapper.INSTANCE.toPostsResponseDto(
						post,
						postRepository.countAllCommentByPost_Id(post.getId()),
						getNumOfPostLikes(post),
						getNumOfPostFavorites(post),
						!post.getPostAttachImageList().isEmpty() ? post.getPostAttachImageList().get(0) : null,
						StatusPolicy.isPostVote(post),
						post.getForm() != null
					)))
			)
			.collect(Collectors.toList());
	}

	private BoardResponseDto toBoardResponseDto(Board board, Set<Role> userRoles) {
		List<String> roles = Arrays.asList(board.getCreateRoles().split(","));
		Boolean writable = userRoles.stream()
			.map(Role::getValue)
			.anyMatch(roles::contains);
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

	private Long getNumOfPostLikes(Post post) {
		return likePostRepository.countByPostId(post.getId());
	}

	private Long getNumOfPostFavorites(Post post) {
		return favoritePostRepository.countByPostId(post.getId());
	}
}
