package net.causw.application.spi;

import net.causw.application.dto.PostDetailDto;

public interface PostPort {
    PostDetailDto findById(String id);
}
