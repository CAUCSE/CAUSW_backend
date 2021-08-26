package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Circle;
import net.causw.adapter.persistence.CircleRepository;
import net.causw.adapter.persistence.User;
import net.causw.application.dto.CircleCreateRequestDto;
import net.causw.application.dto.CircleFullDto;
import net.causw.application.dto.UserFullDto;
import net.causw.application.spi.CirclePort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CirclePortImpl implements CirclePort {
    private final CircleRepository circleRepository;

    public CirclePortImpl(CircleRepository circleRepository) {
        this.circleRepository = circleRepository;
    }

    @Override
    public Optional<CircleFullDto> findById(String id) {
        return this.circleRepository.findById(id).map(CircleFullDto::from);
    }

    @Override
    public Optional<CircleFullDto> findByLeaderId(String leaderId) {
        return this.circleRepository.findByLeaderId(leaderId).map(CircleFullDto::from);
    }

    @Override
    public Optional<CircleFullDto> findByName(String name) {
        return this.circleRepository.findByName(name).map(CircleFullDto::from);
    }

    @Override
    public CircleFullDto create(CircleCreateRequestDto circleCreateRequestDto, UserFullDto leader) {
        return CircleFullDto.from(this.circleRepository.save(Circle.of(
                circleCreateRequestDto.getName(),
                circleCreateRequestDto.getMainImage(),
                circleCreateRequestDto.getDescription(),
                false,
                User.from(leader)
        )));
    }

    @Override
    public Optional<CircleFullDto> updateLeader(String id, UserFullDto newLeader) {
        return this.circleRepository.findById(id).map(
                srcCircle -> {
                    srcCircle.setLeader(User.from(newLeader));

                    return CircleFullDto.from(this.circleRepository.save(srcCircle));
                }
        );
    }
}
