package net.causw.application.spi;

import net.causw.application.dto.CircleDto;

public interface CirclePort {
    CircleDto findById(String id);
}
