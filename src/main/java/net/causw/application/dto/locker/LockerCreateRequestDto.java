package net.causw.application.dto.locker;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LockerCreateRequestDto {

    @NotNull(message = "사물함 번호를 입력해 주세요.")
    private Long lockerNumber;

    @NotBlank(message = "사물함 위치 id를 입력해 주세요. locker id와 다릅니다.")
    private String lockerLocationId;

}
