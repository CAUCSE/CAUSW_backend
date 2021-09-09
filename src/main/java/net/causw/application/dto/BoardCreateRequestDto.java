package net.causw.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardCreateRequestDto {
    private String name;
    private String description;
    private List<String> createRoleList;
    private List<String> modifyRoleList;
    private List<String> readRoleList;

    private String circleId;
}
