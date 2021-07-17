package net.causw.application;

import net.causw.application.dto.CircleDto;
import net.causw.application.spi.CirclePort;
import org.springframework.stereotype.Service;

@Service
public class CircleService {
    private final CirclePort circlePort;

    public CircleService(CirclePort circlePort) {
        this.circlePort = circlePort;
    }

    public CircleDto findById(String id) { return this.circlePort.findById(id); }
}
