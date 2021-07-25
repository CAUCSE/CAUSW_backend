package net.causw.application;

import net.causw.application.dto.PostDetailDto;
import net.causw.application.spi.PostPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {
    private final PostPort postPort;

    public PostService(PostPort postPort) {
        this.postPort = postPort;
    }

    @Transactional(readOnly = true)
    public PostDetailDto findById(String id) {
        return this.postPort.findById(id);
    }
}
