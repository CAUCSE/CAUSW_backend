package net.causw.application.crawler;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.crawled.CrawledFileLink;
import net.causw.adapter.persistence.crawled.CrawledNotice;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.board.BoardRepository;
import net.causw.adapter.persistence.repository.crawled.CrawledNoticeRepository;
import net.causw.adapter.persistence.repository.post.PostRepository;
import net.causw.adapter.persistence.repository.user.UserRepository;
import net.causw.adapter.persistence.repository.uuidFile.UuidFileRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.util.MessageUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CrawledToPostTransferService {

    private final CrawledNoticeRepository crawledNoticeRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final UuidFileRepository uuidFileRepository;

    @Scheduled(fixedRate = 5000) // 테스트용, 5초마다 실행
//    @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시
    @Transactional
    public void transferCrawledNoticesToPosts() {
        Board board = boardRepository.findByName("소프트웨어학부 학부 공지")
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.BOARD_NOT_FOUND
                ));

        User user = userRepository.findByStudentId("20220881")
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                ));

        List<CrawledNotice> notices = crawledNoticeRepository.findTop20ByOrderByAnnounceDateDesc();

        for (CrawledNotice notice : notices) {
            String title = (notice.getTitle() == null || notice.getTitle().isBlank()) ? "제목 없음" : notice.getTitle();
            String content = (notice.getContent() == null || notice.getContent().isBlank()) ? "내용 없음" : notice.getContent();

            // 본문 끝에 링크 추가하여 중복 체크
            StringBuilder contentBuilder = new StringBuilder(content);

            List<UuidFile> uuidFileList = new ArrayList<>();

            // 이미지 처리
            if (notice.getImageLink() != null && !notice.getImageLink().isBlank()) {
                contentBuilder.append("\n\n![공지 이미지](").append(notice.getImageLink()).append(")");
            }

            // 파일 처리
            if (notice.getCrawledFileLinks() != null) {
                for (CrawledFileLink fileLink : notice.getCrawledFileLinks()) {
                    contentBuilder.append("\n\n[첨부파일: ")
                            .append(fileLink.getFileName())
                            .append("](")
                            .append(fileLink.getFileLink())
                            .append(")");
                }
            }

            // 원본 링크 삽입
            contentBuilder.append("\n\n[공지 링크](").append(notice.getLink()).append(")");

            // 게시판에 같은 링크 포함한 글이 이미 있으면 넘김
            if (postRepository.existsByBoardAndContentContains(board, notice.getLink())) continue;

            Post post = Post.of(
                    title,
                    contentBuilder.toString(),
                    user,
                    false,
                    false,
                    board,
                    null,
                    uuidFileList
            );

            postRepository.save(post);
        }
    }
}