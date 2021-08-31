package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.UserCircle;
import net.causw.domain.model.UserCircleStatus;

@Getter
@NoArgsConstructor
public class UserCircleDto {
    private String id;
    private UserCircleStatus status;
    private CircleFullDto circle;
    private UserResponseDto user;

    private UserCircleDto(
            String id,
            UserCircleStatus status,
            CircleFullDto circle,
            UserResponseDto user
    ) {
        this.id = id;
        this.status = status;
        this.circle = circle;
        this.user = user;
    }

    public static UserCircleDto from(UserCircle userCircle) {
        return new UserCircleDto(
                userCircle.getId(),
                userCircle.getStatus(),
                CircleFullDto.from(
                        userCircle.getCircle()
                ),
                UserResponseDto.from(
                        userCircle.getUser()
                )
        );
    }
}
