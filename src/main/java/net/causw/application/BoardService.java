package net.causw.application;

import net.causw.application.dto.BoardDetailDto;
import net.causw.application.spi.BoardPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoardService {
    private BoardPort boardPort;

    public BoardService(BoardPort boardPort) {
        this.boardPort = boardPort;
    }

    @Transactional(readOnly = true)
    public BoardDetailDto findById(String id) {
        return this.boardPort.findById(id);
    }
}
