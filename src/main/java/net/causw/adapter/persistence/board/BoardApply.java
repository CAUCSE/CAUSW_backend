package net.causw.adapter.persistence.board;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.BoardApplyStatus;
import org.hibernate.annotations.ColumnDefault;


@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_board_apply")
public class BoardApply extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
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

    @Column(name = "accept_status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BoardApplyStatus acceptStatus = BoardApplyStatus.AWAIT;

    @Column(name = "is_annonymous_allowed", nullable = false)
    @ColumnDefault("false")
    private Boolean isAnonymousAllowed;

    public static BoardApply of(
            User user,
            String boardName,
            String description,
            String category,
            Boolean isAnonymousAllowed
    ) {
        // description 비어있을 경우 처리
        if (description == null) {
            description = "";
        }

        return BoardApply.builder()
                .user(user)
                .boardName(boardName)
                .description(description)
                .createRoles("ALL") // 모든 권한. 이렇게 넘기면 Board.of에서 List.of에 넣어서 일관된 처리
                .category(category)
                .acceptStatus(BoardApplyStatus.AWAIT)
                .isAnonymousAllowed(isAnonymousAllowed)
                .build();
    }

    public void updateAcceptStatus(BoardApplyStatus status) {
        this.acceptStatus = status;
    }

}
