package net.causw.application.dto;

import net.causw.infra.Circle;

public class CircleDto {
    private String id;
    private String name;
    private String mainImage;
    private String description;
    private String isDeleted;
    private UserDetailDto manager;

    private CircleDto(
            String id,
            String name,
            String mainImage,
            String description,
            String isDeleted,
            UserDetailDto manager
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
                UserDetailDto.from(
                        circle.getManager()
                )
        );
    }
}
