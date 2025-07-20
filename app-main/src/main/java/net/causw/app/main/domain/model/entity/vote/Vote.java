package net.causw.app.main.domain.model.entity.vote;

import jakarta.persistence.*;
import lombok.*;
import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.post.Post;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_vote")
public class Vote extends BaseEntity {
    private String title;
    private boolean allowAnonymous;
    private boolean allowMultiple;
    private boolean isEnd;

    @OneToMany(mappedBy = "vote", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VoteOption> voteOptions = new ArrayList<>();

    @OneToOne(mappedBy = "vote", fetch = FetchType.EAGER)
    private Post post;

    public static Vote of(String title, boolean allowAnonymous, boolean allowMultiple, List<VoteOption> voteOptions, Post post) {
        return Vote.builder()
                .title(title)
                .allowAnonymous(allowAnonymous)
                .allowMultiple(allowMultiple)
                .voteOptions(voteOptions)
                .post(post)
                .build();
    }

    public void endVote(){
        this.isEnd = true;
    }

    public void restartVote(){
        this.isEnd = false;
    }
}
