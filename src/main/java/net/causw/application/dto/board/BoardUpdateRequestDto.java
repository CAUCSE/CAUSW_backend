package net.causw.application.dto.board;

import lombok.Data;

import java.util.List;

@Data
public class BoardUpdateRequestDto {
    private String name;
    private String description;
    private List<String> createRoleList;
    private String category;
}
