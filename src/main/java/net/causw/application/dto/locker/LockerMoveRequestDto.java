package net.causw.application.dto.locker;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LockerMoveRequestDto {
    private String locationId;
}
