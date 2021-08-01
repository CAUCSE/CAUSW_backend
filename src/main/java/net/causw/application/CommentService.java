package net.causw.application;

import net.causw.application.dto.CommentDetailDto;
import net.causw.application.spi.CommentPort;
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
        return this.commentPort.findById(id);
    }
}
