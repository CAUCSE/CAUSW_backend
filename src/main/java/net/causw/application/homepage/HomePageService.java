package net.causw.application.homepage;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.page.PageableFactory;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.BoardRepository;
import net.causw.adapter.persistence.repository.LikePostRepository;
import net.causw.adapter.persistence.repository.PostRepository;
import net.causw.adapter.persistence.repository.UserRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.homepage.HomePageResponseDto;
import net.causw.application.dto.board.BoardResponseDto;
import net.causw.application.dto.util.DtoMapper;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.validation.UserRoleIsNoneValidator;
import net.causw.domain.validation.UserStateValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomePageService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final LikePostRepository likePostRepository;
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
                                .map(post -> DtoMapper.INSTANCE.toPostsResponseDto(
                                        post,
                                        postRepository.countAllCommentByPost_Id(post.getId()),
                                        getNumOfPostLikes(post)
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
        return DtoMapper.INSTANCE.toBoardResponseDto(
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
}
