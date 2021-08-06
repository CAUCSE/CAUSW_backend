package net.causw.application;

import net.causw.application.dto.CircleDto;
import net.causw.application.spi.CirclePort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CircleService {
    private final CirclePort circlePort;

    public CircleService(CirclePort circlePort) {
        this.circlePort = circlePort;
    }

    @Transactional(readOnly = true)
    public CircleDto findById(String id) {
        return this.circlePort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid circle id"
                )
        );
    }
}
