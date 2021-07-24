package net.causw.adapter.persistence.port;

import net.causw.application.dto.CircleDto;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.application.spi.CirclePort;
import net.causw.adapter.persistence.CircleRepository;
import org.springframework.stereotype.Component;

@Component
public class CirclePortImpl implements CirclePort {
    private final CircleRepository circleRepository;

    public CirclePortImpl(CircleRepository circleRepository) {
        this.circleRepository = circleRepository;
    }

    @Override
    public CircleDto findById(String id) {
        return CircleDto.from(this.circleRepository.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid circle id"
                )
        ));
    }
}
