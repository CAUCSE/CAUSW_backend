package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.Circle;

@Getter
@NoArgsConstructor
public class CircleDto {
    private String id;
    private String name;
    private String mainImage;
    private String description;
    private Boolean isDeleted;
    private UserResponseDto manager;

    private CircleDto(
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

    public static CircleDto from(Circle circle) {
        return new CircleDto(
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
}
