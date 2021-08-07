package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.CommentRepository;
import net.causw.application.dto.CommentDetailDto;
import net.causw.application.spi.CommentPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CommentPortImpl implements CommentPort {
    private final CommentRepository commentRepository;

    public CommentPortImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public Optional<CommentDetailDto> findById(String id) {
        return this.commentRepository.findById(id).map(CommentDetailDto::from);
    }
}
