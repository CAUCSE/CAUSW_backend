package net.causw.application.dto.locker;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LockerCreateRequestDto {
    private Long lockerNumber;
    private String lockerLocationId;
}
