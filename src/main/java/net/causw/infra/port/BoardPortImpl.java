package net.causw.infra.port;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.spi.BoardPort;
import net.causw.infra.BoardRepository;
import org.springframework.stereotype.Component;

@Component
public class BoardPortImpl implements BoardPort {
    private final BoardRepository boardRepository;

    public BoardPortImpl(BoardRepository boardRepository) { this.boardRepository = boardRepository; }

    @Override
    public BoardDomainModel findById(String id) {
        return BoardDomainModel.of(this.boardRepository.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid board id"
                )
        ));
    }
}
