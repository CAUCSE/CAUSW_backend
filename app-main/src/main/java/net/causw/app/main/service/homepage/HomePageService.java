package net.causw.app.main.service.homepage;

import lombok.RequiredArgsConstructor;
import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.circle.Circle;
import net.causw.app.main.domain.policy.StatusPolicy;
import net.causw.app.main.service.pageable.PageableFactory;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.repository.board.BoardRepository;
import net.causw.app.main.repository.post.FavoritePostRepository;
import net.causw.app.main.repository.post.LikePostRepository;
import net.causw.app.main.repository.post.PostRepository;
import net.causw.app.main.repository.user.UserRepository;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.dto.homepage.HomePageResponseDto;
import net.causw.app.main.dto.board.BoardResponseDto;
import net.causw.app.main.dto.util.dtoMapper.BoardDtoMapper;
import net.causw.app.main.dto.util.dtoMapper.PostDtoMapper;
import net.causw.app.main.infrastructure.aop.annotation.MeasureTime;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.app.main.domain.validation.UserRoleIsNoneValidator;
import net.causw.app.main.domain.validation.UserStateValidator;
import net.causw.app.main.domain.validation.ValidatorBucket;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
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
                        postRepository.findAllByBoard_IdAndIsDeletedIsFalseOrderByCreatedAtDesc(board.getId(), pageableFactory.create(0, StaticValue.HOME_POST_PAGE_SIZE))
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

    private Long getNumOfPostLikes(Post post){
        return likePostRepository.countByPostId(post.getId());
    }

    private Long getNumOfPostFavorites(Post post){
        return favoritePostRepository.countByPostIdAndIsDeletedFalse(post.getId());
    }
}
