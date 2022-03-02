package net.causw.application.dto.circle;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CircleCreateRequestDto {
    private String name;
    private String mainImage;
    private String description;
    private String leaderId;
}
