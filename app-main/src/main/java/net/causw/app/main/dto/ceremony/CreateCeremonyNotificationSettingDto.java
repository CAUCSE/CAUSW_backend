package net.causw.app.main.dto.ceremony;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import java.util.Set;

@Getter
public class CreateCeremonyNotificationSettingDto {

    // 여러 학번을 받을 수 있는 변수 추가
    @NotNull
    @Schema(description = "구독을 원하는 입학년도를 입력해주세요")
    private Set<Integer> subscribedAdmissionYears;

    @NotNull
    @Schema(description = "모든 입학년도 설정에 대한 flag")
    private boolean setAll;

    @NotNull
    @Schema(description = "푸시알람 수신 여부에 대한 flag")
    private boolean notificationActive;
}
