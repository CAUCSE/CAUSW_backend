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
    @NotBlank(message = "사물함 위치 id를 입력해 주세요.")
    private String locationId;
}
