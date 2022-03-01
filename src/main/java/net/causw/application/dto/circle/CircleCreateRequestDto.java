package net.causw.application.dto.circle;

import lombok.Data;

@Data
public class CircleCreateRequestDto {
    private String name;
    private String mainImage;
    private String description;
    private String leaderId;
}
