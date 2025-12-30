package net.causw.app.main.shared.seed;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardApply;
import net.causw.app.main.domain.community.board.entity.BoardApplyStatus;
import net.causw.app.main.domain.community.board.repository.BoardApplyRepository;
import net.causw.app.main.domain.community.board.repository.BoardRepository;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.global.constant.StaticValue;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Profile("seed")
@RequiredArgsConstructor
@Slf4j
public class BoardSeeder {
    private final EntityManager em;
    private final BoardApplyRepository boardApplyRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public void seed() {
        if (boardRepository.count() > 0) {
            log.warn("🚫 Seed skipped: boards already exist");
            return;
        }

        User user = userRepository.findTopBy()
                .orElseThrow(() -> new IllegalStateException("🚫 Seed skipped: User not found for seeding boards"));

        process(user);
    }

    private void process(User user) {
        // 게시판 카테고리 이름 예시 (20개)
        List<String> boardNames = List.of(
                "자유게시판", "익명게시판", "질문게시판", "정보공유", "취업게시판",
                "동아리홍보", "스터디모집", "중고장터", "분실물센터", "학교생활",
                "유머게시판", "건의사항", "컴공게시판", "학사공지", "장학공지",
                "학생회 공지 게시판", "크자회 공지 게시판", "서비스 공지", "졸업생게시판", "새내기게시판"
        );

        int count = 0;
        for (String name : boardNames) {
            createActiveBoard(user, name, count++);
        }

        em.flush();
        em.clear();

        log.info("✅ Seeded {} boards completed.", boardNames.size());
    }

    private void createActiveBoard(User user, String name, int index) {
        BoardApply boardApply = BoardApply.of( // 일반 게시판 신청
                user,
                name,
                null,
                StaticValue.BOARD_NAME_APP_FREE,
                false,
                null
        );
        boardApply.updateAcceptStatus(BoardApplyStatus.ACCEPTED);
        this.boardApplyRepository.save(boardApply);
        em.persist(boardApply);

        Board newBoard = Board.of( // 일반 게시판 생성
                boardApply.getBoardName(),
                boardApply.getDescription(),
                boardApply.getCategory(),
                boardApply.getIsAnonymousAllowed(),
                null
        );

        this.boardRepository.save(newBoard);
        em.persist(newBoard);
    }
}
