package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.CommentRepository;
import net.causw.application.dto.CommentDetailDto;
import net.causw.application.spi.CommentPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class CommentPortImpl implements CommentPort {
    private final CommentRepository commentRepository;

    public CommentPortImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public CommentDetailDto findById(String id) {
        return CommentDetailDto.from(this.commentRepository.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid comment id"
                )
        ));
    }
}
