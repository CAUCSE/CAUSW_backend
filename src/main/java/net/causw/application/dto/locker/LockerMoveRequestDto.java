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
public class LockerMoveRequestDto {

    @NotBlank(message = "사물함 위치 id(locker location id)를 입력해 주세요. locker id와 다릅니다.")
    private String lockerLocationId;

}
