package net.causw.adapter.persistence.port;

import net.causw.application.dto.PostDetailDto;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.application.spi.PostPort;
import net.causw.adapter.persistence.PostRepository;
import org.springframework.stereotype.Component;

@Component
public class PostPortImpl implements PostPort {
    private final PostRepository postRepository;

    public PostPortImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public PostDetailDto findById(String id) {
        return PostDetailDto.from(this.postRepository.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid post id"
                )
        ));
    }
}
