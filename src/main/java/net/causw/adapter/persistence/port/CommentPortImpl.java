package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Comment;
import net.causw.adapter.persistence.CommentRepository;
import net.causw.adapter.persistence.Post;
import net.causw.adapter.persistence.User;
import net.causw.application.dto.CommentCreateRequestDto;
import net.causw.application.dto.CommentFullDto;
import net.causw.application.dto.CommentResponseDto;
import net.causw.application.dto.PostDetailDto;
import net.causw.application.dto.UserFullDto;
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
    public Optional<CommentFullDto> findById(String id) {
        return this.commentRepository.findById(id).map(CommentFullDto::from);
    }

    @Override
    public CommentResponseDto create(
            CommentCreateRequestDto commentCreateDto,
            UserFullDto writer,
            PostDetailDto post
    ) {
        return CommentResponseDto.from(
                this.commentRepository.save(Comment.of(
                        commentCreateDto.getContent(),
                        false,
                        User.from(writer),
                        Post.from(post),
                        null
                ))
        );
    }

    @Override
    public CommentResponseDto create(
            CommentCreateRequestDto commentCreateDto,
            UserFullDto writer,
            PostDetailDto post,
            CommentFullDto parentCommentDto
    ) {
        return CommentResponseDto.from(
                this.commentRepository.save(Comment.of(
                    commentCreateDto.getContent(),
                    false,
                    User.from(writer),
                    Post.from(post),
                    Comment.from(parentCommentDto)
                ))
        );
    }
}
