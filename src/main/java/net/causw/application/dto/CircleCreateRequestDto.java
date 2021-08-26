package net.causw.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CircleCreateRequestDto {
    private String name;
    private String mainImage;
    private String description;
    private String leaderId;
}
