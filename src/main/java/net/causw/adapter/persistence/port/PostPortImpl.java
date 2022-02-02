package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.PageableFactory;
import net.causw.adapter.persistence.Post;
import net.causw.adapter.persistence.PostRepository;
import net.causw.application.spi.PostPort;
import net.causw.domain.model.PostDomainModel;
import net.causw.domain.model.SearchOption;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class PostPortImpl extends DomainModelMapper implements PostPort {
    private final PostRepository postRepository;
    private final PageableFactory pageableFactory;

    public PostPortImpl(
            PostRepository postRepository,
            PageableFactory pageableFactory
    ) {
        this.postRepository = postRepository;
        this.pageableFactory = pageableFactory;
    }

    @Override
    public Optional<PostDomainModel> findById(String id) {
        return this.postRepository.findById(id).map(this::entityToDomainModel);
    }

    @Override
    public PostDomainModel create(PostDomainModel postDomainModel) {
        return this.entityToDomainModel(this.postRepository.save(Post.from(postDomainModel)));
    }

    @Override
    public Optional<PostDomainModel> delete(String id) {
        return this.postRepository.findById(id).map(
                srcPost -> {
                    srcPost.setIsDeleted(true);

                    return this.entityToDomainModel(this.postRepository.save(srcPost));
                }
        );
    }

    @Override
    public Optional<PostDomainModel> update(String id, PostDomainModel postDomainModel) {
        return this.postRepository.findById(id).map(
                srcPost -> {
                    srcPost.setTitle(postDomainModel.getTitle());
                    srcPost.setContent(postDomainModel.getContent());

                    return this.entityToDomainModel(this.postRepository.save(srcPost));
                }
        );
    }

    @Override
    public Page<PostDomainModel> findAll(String boardId, Integer pageNum) {
        return this.postRepository.findAllByBoard_IdAndIsDeletedIsFalseOrderByCreatedAtDesc(boardId, this.pageableFactory.create(pageNum))
                .map(this::entityToDomainModel);
    }

    @Override
    public Page<PostDomainModel> findAll(String boardId, Integer pageNum, Integer pageSize) {
        return this.postRepository.findAllByBoard_IdAndIsDeletedIsFalseOrderByCreatedAtDesc(boardId, this.pageableFactory.create(pageNum, pageSize))
                .map(this::entityToDomainModel);
    }

    @Override
    public Page<PostDomainModel> search(SearchOption option, String keyword, Integer pageNum) {
        switch (option){
            case TITLE:
                return this.postRepository.searchByTitle(keyword, this.pageableFactory.create(pageNum)).map(this::entityToDomainModel);
            case WRITER:
                return this.postRepository.searchByWriter(keyword, this.pageableFactory.create(pageNum)).map(this::entityToDomainModel);
            default:
                return null;
        }
    }

    @Override
    public Optional<PostDomainModel> findLatest(String boardId) {
        return this.postRepository.findTop1ByBoard_IdAndIsDeletedIsFalseOrderByCreatedAtDesc(boardId)
                .map(this::entityToDomainModel);
    }
}
