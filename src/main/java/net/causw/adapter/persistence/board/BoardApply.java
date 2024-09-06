package net.causw.adapter.persistence.board;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.Role;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_board_apply")
public class BoardApply extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "board_name", nullable = false)
    private String boardName;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "create_role_list", nullable = false)
    private String createRoles;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "is_accepted", nullable = false)
    @ColumnDefault("false")
    private Boolean isAccepted;

    public static BoardApply of(
            User user,
            String boardName,
            String description,
            String category
    ) {
        // description 비어있을 경우 처리
        if (description == null) {
            description = "";
        }

        return new BoardApply(
                user,
                boardName,
                description,
                "ALL", // 모든 권한. 이렇게 넘기면 Board.of에서 처리
                category,
                false
        );
    }

}
