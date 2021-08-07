package net.causw.application;

import net.causw.application.dto.CommentDetailDto;
import net.causw.application.spi.CommentPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {
    private final CommentPort commentPort;

    public CommentService(CommentPort commentPort) {
        this.commentPort = commentPort;
    }

    @Transactional(readOnly = true)
    public CommentDetailDto findById(String id) {
        return this.commentPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid comment id"
                )
        );
    }
}
