package net.causw.application;

import net.causw.application.dto.PostDetailDto;
import net.causw.domain.spi.PostPort;
import org.springframework.stereotype.Service;

@Service
public class PostService {
    private final PostPort postPort;

    public PostService(PostPort postPort) {
        this.postPort = postPort;
    }

    public PostDetailDto findById(String id) {
        return PostDetailDto.of(this.postPort.findById(id));
    }
}
