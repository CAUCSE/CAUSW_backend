package net.causw.application;

import net.causw.application.dto.PostResponseDto;
import net.causw.application.spi.PostPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {
    private final PostPort postPort;

    public PostService(PostPort postPort) {
        this.postPort = postPort;
    }

    @Transactional(readOnly = true)
    public PostResponseDto findById(String id) {
        return PostResponseDto.from(this.postPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid post id"
                )
        ));
    }
}
