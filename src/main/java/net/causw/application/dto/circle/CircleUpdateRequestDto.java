package net.causw.application.dto.circle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CircleUpdateRequestDto {
    private String name;
    private String mainImage;
    private String description;
}
