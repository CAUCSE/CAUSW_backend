package net.causw.application.spi;

import net.causw.application.dto.PostDetailDto;

import java.util.Optional;

public interface PostPort {
    Optional<PostDetailDto> findById(String id);
}
