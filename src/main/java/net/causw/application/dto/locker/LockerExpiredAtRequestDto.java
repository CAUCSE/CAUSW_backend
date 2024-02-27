package net.causw.application.dto.locker;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LockerExpiredAtRequestDto {
    private LocalDateTime expiredAt;
}
