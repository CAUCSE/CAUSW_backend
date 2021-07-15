package net.causw.application.spi;

import net.causw.application.dto.BoardDetailDto;

public interface BoardPort {
    BoardDetailDto findById(String id);
}
