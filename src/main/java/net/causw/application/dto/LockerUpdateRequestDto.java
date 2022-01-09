package net.causw.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.domain.model.LockerLogAction;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LockerUpdateRequestDto {
    String action;
    String message;
}
