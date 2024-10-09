package net.causw.application.notification;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.notification.UserBoardSubscribe;
import net.causw.adapter.persistence.repository.board.BoardRepository;
import net.causw.adapter.persistence.repository.notification.NotificationRepository;
import net.causw.adapter.persistence.repository.notification.UserBoardSubscribeRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.notification.NotificationResponseDto;
import net.causw.application.dto.util.dtoMapper.NotificationDtoMapper;
import net.causw.domain.aop.annotation.MeasureTime;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.util.MessageUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@MeasureTime
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserBoardSubscribeRepository userBoardSubscribeRepository;
    private final BoardRepository boardRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> findUserNotice(User user) {
        return notificationRepository.findUserNotice(user.getId()).stream()
                .map(NotificationDtoMapper.INSTANCE::toNotificationResponseDto)
                .toList();
    }

    @Transactional
    public void setNotice(User user, String boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.BOARD_NOT_FOUND
                )
        );

        Optional<UserBoardSubscribe> find = userBoardSubscribeRepository.findByUser_IdAndBoard_Id(user.getId(), boardId);
        if (find.isPresent()) {
            UserBoardSubscribe userBoardSubscribe = find.get();
            userBoardSubscribeRepository.save(userBoardSubscribe.toggle());
        } else {
            userBoardSubscribeRepository.save(UserBoardSubscribe.of(user, board, true));
        }
    }

    @Transactional
    public Boolean checkNotice(User user, String boardId) {
        return userBoardSubscribeRepository.findByUser_IdAndBoard_Id(user.getId(), boardId)
                .map(UserBoardSubscribe::getIsSubscribed)
                .orElse(false);
    }
}
