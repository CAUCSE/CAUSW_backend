package net.causw.application.spi;

import net.causw.application.dto.PostFullDto;

import java.util.Optional;

public interface PostPort {
    Optional<PostFullDto> findById(String id);
}
