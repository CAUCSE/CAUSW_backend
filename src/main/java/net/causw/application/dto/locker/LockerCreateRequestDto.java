package net.causw.application.dto.locker;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LockerCreateRequestDto {
    private Long lockerNumber;
    private String lockerLocationId;
}
