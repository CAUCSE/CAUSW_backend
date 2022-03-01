package net.causw.application.dto.locker;

import lombok.Data;

@Data
public class LockerCreateRequestDto {
    private Long lockerNumber;
    private String lockerLocationId;
}
