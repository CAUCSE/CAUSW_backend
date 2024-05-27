package net.causw.application.dto.locker;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LockerUpdateRequestDto {
    @ApiModelProperty(value = "\n" +
            "            ENABLE(\"ENABLE\"),\n" +
            "    DISABLE(\"DISABLE\"),\n" +
            "    REGISTER(\"REGISTER\"),\n" +
            "    RETURN(\"RETURN\"),\n" +
            "    EXTEND(\"EXTEND\");", example = "REGISTER")
    private String action;
    @ApiModelProperty(value = "로그에 남길 message", example = "hi(자율)")
    private String message;

    public Optional<String> getMessage() {
        return Optional.ofNullable(this.message);
    }
}
