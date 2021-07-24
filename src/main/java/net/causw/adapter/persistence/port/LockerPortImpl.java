package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.LockerRepository;
import net.causw.application.dto.LockerDetailDto;
import net.causw.application.spi.LockerPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class LockerPortImpl implements LockerPort {
    private final LockerRepository lockerRepository;

    public LockerPortImpl(LockerRepository lockerRepository) {
        this.lockerRepository = lockerRepository;
    }

    @Override
    public Optional<LockerDetailDto> findById(String id) {
        return this.lockerRepository.findById(id).map(LockerDetailDto::from);
    }


    @Override
    public List<LockerDetailDto> findAll() {
        return this.lockerRepository.findAll()
                .stream()
                .map(LockerDetailDto::from)
                .collect(Collectors.toList());
    }
}
