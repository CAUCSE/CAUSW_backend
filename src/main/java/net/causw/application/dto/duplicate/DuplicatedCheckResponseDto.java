package net.causw.application.dto.duplicate;

import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.application.dto.util.CircleServiceDtoMapper;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class DuplicatedCheckResponseDto {

    @ApiModelProperty(value = "중복 여부 boolean 값", example = "true")
    private Boolean result;

    public static DuplicatedCheckResponseDto from(boolean result) {
        return DuplicatedCheckResponseDto.builder()
                .result(result)
                .build();
    }

}
