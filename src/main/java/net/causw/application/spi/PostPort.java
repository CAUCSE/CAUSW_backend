package net.causw.application.spi;

import net.causw.domain.model.post.PostDomainModel;
import net.causw.domain.model.enums.SearchOption;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface PostPort {
    Optional<PostDomainModel> findById(String id);

    PostDomainModel create(PostDomainModel postDomainModel);

    Optional<PostDomainModel> delete(String id);

    Optional<PostDomainModel> update(String id, PostDomainModel postDomainModel);

    Page<PostDomainModel> findAll(String boardId, Integer pageNum);

    Page<PostDomainModel> findAll(String boardId, Integer pageNum, Integer pageSize);

    Page<PostDomainModel> search(SearchOption option, String keyword, Integer pageNum);

    Optional<PostDomainModel> findLatest(String boardId);

    Page<PostDomainModel> findByUserId(String userId, Integer pageNum);

    Optional<PostDomainModel> restore(String id, PostDomainModel postDomainModel);
}
