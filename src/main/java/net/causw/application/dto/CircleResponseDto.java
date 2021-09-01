package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.Circle;

@Getter
@NoArgsConstructor
public class CircleResponseDto {
    private String id;
    private String name;
    private String mainImage;
    private String description;
    private Boolean isDeleted;
    private UserResponseDto manager;

    private CircleResponseDto(
            String id,
            String name,
            String mainImage,
            String description,
            Boolean isDeleted,
            UserResponseDto manager
    ) {
        this.id = id;
        this.name = name;
        this.mainImage = mainImage;
        this.description = description;
        this.isDeleted = isDeleted;
        this.manager = manager;
    }

    public static CircleResponseDto from(Circle circle) {
        return new CircleResponseDto(
                circle.getId(),
                circle.getName(),
                circle.getMainImage(),
                circle.getDescription(),
                circle.getIsDeleted(),
                UserResponseDto.from(
                        circle.getLeader()
                )
        );
    }

    public static CircleResponseDto from(CircleFullDto circle) {
        return new CircleResponseDto(
                circle.getId(),
                circle.getName(),
                circle.getMainImage(),
                circle.getDescription(),
                circle.getIsDeleted(),
                UserResponseDto.from(
                        circle.getManager()
                )
        );
    }
}
