package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.User;
import net.causw.adapter.persistence.UserRepository;
import net.causw.application.dto.CircleDto;
import net.causw.application.dto.UserFullDto;
import net.causw.application.spi.CirclePort;
import net.causw.adapter.persistence.CircleRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CirclePortImpl implements CirclePort {
    private final CircleRepository circleRepository;

    public CirclePortImpl(CircleRepository circleRepository) {
        this.circleRepository = circleRepository;
    }

    @Override
    public Optional<CircleDto> findById(String id) {
        return this.circleRepository.findById(id).map(CircleDto::from);
    }

    @Override
    public Optional<CircleDto> findByLeaderId(String leaderId) {
        return this.circleRepository.findByLeaderId(leaderId).map(CircleDto::from);
    }

    @Override
    public Optional<CircleDto> updateLeader(String id, UserFullDto newLeader) {
        return this.circleRepository.findById(id).map(
                srcCircle -> {
                    srcCircle.setLeader(User.of(
                            newLeader.getId(),
                            newLeader.getEmail(),
                            newLeader.getName(),
                            newLeader.getPassword(),
                            newLeader.getStudentId(),
                            newLeader.getAdmissionYear(),
                            newLeader.getRole(),
                            newLeader.getState()
                    ));

                    return CircleDto.from(this.circleRepository.save(srcCircle));
                }
        );
    }
}
