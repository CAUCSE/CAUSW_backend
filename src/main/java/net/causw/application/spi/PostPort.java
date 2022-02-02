package net.causw.application.spi;

import net.causw.domain.model.PostDomainModel;
import net.causw.domain.model.SearchOption;
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
}
