package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.PostRepository;
import net.causw.application.dto.PostDetailDto;
import net.causw.application.spi.PostPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PostPortImpl implements PostPort {
    private final PostRepository postRepository;

    public PostPortImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public Optional<PostDetailDto> findById(String id) {
        return this.postRepository.findById(id).map(PostDetailDto::from);
    }
}
