package net.causw.application.spi;

import net.causw.application.dto.LockerDetailDto;

import java.util.List;
import java.util.Optional;

public interface LockerPort {
    Optional<LockerDetailDto> findById(String id);

    List<LockerDetailDto> findAll();
}
