package net.causw.application;

import net.causw.application.dto.BoardDetailDto;
import net.causw.domain.spi.BoardPort;
import org.springframework.stereotype.Service;

@Service
public class BoardService {
    private BoardPort boardPort;

    public BoardService(BoardPort boardPort) {
        this.boardPort = boardPort;
    }

    public BoardDetailDto findById(String id) {
        return BoardDetailDto.of(this.boardPort.findById(id));
    }
}
