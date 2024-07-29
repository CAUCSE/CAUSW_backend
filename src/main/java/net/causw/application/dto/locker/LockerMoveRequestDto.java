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
    @NotBlank // 사물함 위치 id
    private String locationId;
}
