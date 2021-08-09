package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.UserCircleRepository;
import net.causw.application.dto.UserCircleDto;
import net.causw.application.spi.UserCirclePort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserCirclePortImpl implements UserCirclePort {
    private final UserCircleRepository userCircleRepository;

    public UserCirclePortImpl(UserCircleRepository userCircleRepository) {
        this.userCircleRepository = userCircleRepository;
    }

    @Override
    public Optional<UserCircleDto> findById(String id) {
        return this.userCircleRepository.findById(id).map(UserCircleDto::from);
    }
}
