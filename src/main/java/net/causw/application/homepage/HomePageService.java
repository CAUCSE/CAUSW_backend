package net.causw.application.homepage;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.page.PageableFactory;
import net.causw.adapter.persistence.repository.BoardRepository;
import net.causw.adapter.persistence.repository.PostRepository;
import net.causw.adapter.persistence.repository.UserRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.homepage.HomePageResponseDto;
import net.causw.application.dto.board.BoardResponseDto;
import net.causw.application.dto.util.DtoMapper;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.validation.UserRoleIsNoneValidator;
import net.causw.domain.validation.UserStateValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomePageService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final PageableFactory pageableFactory;

    public List<HomePageResponseDto> getHomePage(String userId) {
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND));

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
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
                        BoardResponseDto.of(board, user.getRole()),
                        postRepository.findAllByBoard_IdAndIsDeletedIsFalseOrderByCreatedAtDesc(board.getId(), pageableFactory.create(0, StaticValue.HOME_POST_PAGE_SIZE))
                                .map(post -> DtoMapper.INSTANCE.toPostsResponseDto(
                                        post,
                                        postRepository.countAllCommentByPost_Id(post.getId())
                                )))
                )
                .collect(Collectors.toList());
    }
}
