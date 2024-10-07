package net.causw.adapter.persistence.uuidFile.joinEntity;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.uuidFile.UuidFile;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tb_post_attach_image_uuid_file",
indexes = {
    @Index(name = "idx_post_attach_image_post_id", columnList = "post_id"),
    @Index(name = "idx_post_attach_image_uuid_file_id", columnList = "uuid_file_id")
})
public class PostAttachImage extends JoinEntity {

    @Getter
    @Setter(AccessLevel.PUBLIC)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uuid_file_id", nullable = false, unique = true)
    public UuidFile uuidFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    public static PostAttachImage of(Post post, UuidFile uuidFile) {
        return PostAttachImage.builder()
                .uuidFile(uuidFile)
                .post(post)
                .build();
    }

}
