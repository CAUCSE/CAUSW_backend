package net.causw.application.dto.board;

import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
public class BoardCreateRequestDto {
    private String name;
    private String description;
    private List<String> createRoleList;
    private String category;

    private String circleId;

    public Optional<String> getCircleId() {
        return Optional.ofNullable(this.circleId);
    }
}
