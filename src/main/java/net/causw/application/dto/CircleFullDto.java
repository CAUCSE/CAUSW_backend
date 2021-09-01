package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.Circle;

@Getter
@NoArgsConstructor
public class CircleFullDto {
    private String id;
    private String name;
    private String mainImage;
    private String description;
    private Boolean isDeleted;
    private UserFullDto manager;

    private CircleFullDto(
            String id,
            String name,
            String mainImage,
            String description,
            Boolean isDeleted,
            UserFullDto manager
    ) {
        this.id = id;
        this.name = name;
        this.mainImage = mainImage;
        this.description = description;
        this.isDeleted = isDeleted;
        this.manager = manager;
    }

    public static CircleFullDto from(Circle circle) {
        return new CircleFullDto(
                circle.getId(),
                circle.getName(),
                circle.getMainImage(),
                circle.getDescription(),
                circle.getIsDeleted(),
                UserFullDto.from(
                        circle.getLeader()
                )
        );
    }
}
