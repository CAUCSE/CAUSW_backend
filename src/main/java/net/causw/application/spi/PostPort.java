package net.causw.application.spi;

import net.causw.domain.model.post.PostDomainModel;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface PostPort {
    Optional<PostDomainModel> findPostById(String id);

    Long countAllComment(String postId);

    PostDomainModel createPost(PostDomainModel postDomainModel);

    Optional<PostDomainModel> deletePost(String id);

    Optional<PostDomainModel> updatePost(String id, PostDomainModel postDomainModel);

    Page<PostDomainModel> findAllPost(String boardId, Integer pageNum);

    Page<PostDomainModel> findAllPost(String boardId, Integer pageNum, boolean isDeleted);

    Page<PostDomainModel> findAllPost(String boardId, Integer pageNum, Integer pageSize);


    Page<PostDomainModel> searchPost(String keyword, String boardId, Integer pageNum);

    Page<PostDomainModel> searchPost(String keyword, String boardId, Integer pageNum, boolean isDeleted);

    Optional<PostDomainModel> findLatestPost(String boardId);

    Page<PostDomainModel> findPostByUserId(String userId, Integer pageNum);

    Optional<PostDomainModel> restorePost(String id, PostDomainModel postDomainModel);
}
