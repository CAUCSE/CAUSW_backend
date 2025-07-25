package net.causw.app.main.domain.model.entity.vote;

import jakarta.persistence.*;
import lombok.*;
import net.causw.app.main.domain.model.entity.base.BaseEntity;
import org.jetbrains.annotations.TestOnly;

@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "tb_vote_option")
public class VoteOption extends BaseEntity {

    private String optionName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote;

    public static VoteOption of(String optionName) {
        return VoteOption.builder().optionName(optionName)
                .build();
    }

    public void updateVote(Vote vote){
        this.vote = vote;
    }
}
