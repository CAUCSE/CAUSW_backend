package net.causw.application.dto.locker;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LockerCreateRequestDto {
    @NotBlank(message = "사물함 번호를 입력해 주세요.")
    private Long lockerNumber;
    @NotBlank // 사물한 위치 id TODO
    private String lockerLocationId;
}
