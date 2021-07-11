package net.causw.infra.port;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.PostDomainModel;
import net.causw.domain.spi.PostPort;
import net.causw.infra.PostRepository;
import org.springframework.stereotype.Component;

@Component
public class PostPortImpl implements PostPort {
    private final PostRepository postRepository;

    public PostPortImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public PostDomainModel findById(String id) {
        return PostDomainModel.of(this.postRepository.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid post id"
                )
        ));
    }
}
