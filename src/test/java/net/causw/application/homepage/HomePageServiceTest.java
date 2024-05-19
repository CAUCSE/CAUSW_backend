package net.causw.application.homepage;

import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.page.PageableFactory;
import net.causw.adapter.persistence.repository.BoardRepository;
import net.causw.adapter.persistence.repository.PostRepository;
import net.causw.adapter.persistence.repository.UserRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.board.BoardResponseDto;
import net.causw.application.dto.homepage.HomePageResponseDto;
import net.causw.application.util.ObjectFixtures;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.model.util.StaticValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {HomePageService.class})
class HomePageServiceTest {

    @Autowired
    HomePageService homePageService;

    @MockBean
    PostRepository postRepository;

    @MockBean
    UserRepository userRepository;

    @MockBean
    BoardRepository boardRepository;

    @MockBean
    PageableFactory pageableFactory;
    
    User user;
    Board board;

    @BeforeEach
    void setUp() {
        user = ObjectFixtures.getUser();
        board = ObjectFixtures.getBoard();
    }

    @DisplayName("홈페이지 조회")
    @Test
    void getHomePage() {
        // Given
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(boardRepository.findByCircle_IdIsNullAndIsDeletedOrderByCreatedAtAsc(false)).willReturn(List.of(board));
        given(pageableFactory.create(0, StaticValue.HOME_POST_PAGE_SIZE)).willReturn(PageRequest.of(0, StaticValue.HOME_POST_PAGE_SIZE));
        given(postRepository.findAllByBoard_IdAndIsDeletedIsFalseOrderByCreatedAtDesc(board.getId(), PageRequest.of(0, StaticValue.HOME_POST_PAGE_SIZE)))
                .willReturn(new PageImpl<>(List.of()));

        // When
        List<HomePageResponseDto> result = homePageService.getHomePage(user.getId());

        // Then
        verify(userRepository).findById(user.getId());
        assertEquals(1, result.size());
        BoardResponseDto boardResponseDto = result.get(0).getBoard();
        assertEquals(board.getId(), boardResponseDto.getId());
        assertEquals(board.getName(), boardResponseDto.getName());
        assertEquals(board.getDescription(), boardResponseDto.getDescription());
        assertEquals(board.getCategory(), boardResponseDto.getCategory());
        assertEquals(board.getCreateRoles(), boardResponseDto.getCreateRoleList().get(0));
    }

    @DisplayName("유저가 존재하지 않을 때")
    @Test
    void getHomePage_WhenUserNotFound_ThrowsException() {
        // Given
        given(userRepository.findById(user.getId())).willReturn(Optional.empty());

        // When & Then
        assertThrows(BadRequestException.class, () -> homePageService.getHomePage(user.getId()));
    }

    @DisplayName("게시판이 존재하지 않을 때")
    @Test
    void getHomePage_WhenBoardNotFound_ThrowsException() {
        // Given
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(boardRepository.findByCircle_IdIsNullAndIsDeletedOrderByCreatedAtAsc(false)).willReturn(List.of());

        // When & Then
        assertThrows(BadRequestException.class, () -> homePageService.getHomePage(user.getId()));
    }
}
