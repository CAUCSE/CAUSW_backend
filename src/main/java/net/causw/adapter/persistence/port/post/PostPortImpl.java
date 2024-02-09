package net.causw.adapter.persistence.port.post;

import net.causw.adapter.persistence.page.PageableFactory;
import net.causw.adapter.persistence.port.mapper.DomainModelMapper;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.PostRepository;
import net.causw.application.spi.PostPort;
import net.causw.domain.model.post.PostDomainModel;
import net.causw.domain.model.util.StaticValue;
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
    public Optional<PostDomainModel> findPostById(String id) {
        return this.postRepository.findById(id).map(this::entityToDomainModel);
    }

    @Override
    public PostDomainModel createPost(PostDomainModel postDomainModel) {
        return this.entityToDomainModel(this.postRepository.save(Post.from(postDomainModel)));
    }

    @Override
    public Optional<PostDomainModel> deletePost(String id) {
        return this.postRepository.findById(id).map(
                srcPost -> {
                    srcPost.setIsDeleted(true);

                    return this.entityToDomainModel(this.postRepository.save(srcPost));
                }
        );
    }

    @Override
    public Optional<PostDomainModel> updatePost(String id, PostDomainModel postDomainModel) {
        return this.postRepository.findById(id).map(
                srcPost -> {
                    srcPost.setTitle(postDomainModel.getTitle());
                    srcPost.setContent(postDomainModel.getContent());
                    srcPost.setAttachments(String.join(":::", postDomainModel.getAttachmentList()));

                    return this.entityToDomainModel(this.postRepository.save(srcPost));
                }
        );
    }

    @Override
    public Page<PostDomainModel> findAllPost(String boardId, Integer pageNum) {
        return this.postRepository.findAllByBoard_IdOrderByCreatedAtDesc(boardId, this.pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE))
                .map(this::entityToDomainModel);
    }

    @Override
    public Page<PostDomainModel> findAllPost(String boardId, Integer pageNum, boolean isDeleted) {
        return this.postRepository.findAllByBoard_IdAndIsDeletedOrderByCreatedAtDesc(boardId, this.pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE), isDeleted)
                .map(this::entityToDomainModel);
    }

    @Override
    public Page<PostDomainModel> findAllPost(String boardId, Integer pageNum, Integer pageSize) {
        return this.postRepository.findAllByBoard_IdAndIsDeletedIsFalseOrderByCreatedAtDesc(boardId, this.pageableFactory.create(pageNum, pageSize))
                .map(this::entityToDomainModel);
    }


    @Override
    public Page<PostDomainModel> searchPost(String keyword, String boardId, Integer pageNum) {
        return this.postRepository.searchByTitle(keyword, boardId, this.pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE))
                .map(this::entityToDomainModel);
    }

    @Override
    public Page<PostDomainModel> searchPost(String keyword, String boardId, Integer pageNum, boolean isDeleted) {
        return this.postRepository.searchByTitle(keyword, boardId, this.pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE), isDeleted)
                .map(this::entityToDomainModel);
    }

    @Override
    public Optional<PostDomainModel> findLatestPost(String boardId) {
        return this.postRepository.findTop1ByBoard_IdAndIsDeletedIsFalseOrderByCreatedAtDesc(boardId)
                .map(this::entityToDomainModel);
    }

    @Override
    public Page<PostDomainModel> findPostByUserId(String userId, Integer pageNum) {
        return this.postRepository.findByUserId(userId, this.pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE))
                .map(this::entityToDomainModel);
    }

    @Override
    public Optional<PostDomainModel> restorePost(String id, PostDomainModel postDomainModel) {
        return this.postRepository.findById(id).map(
                srcPost -> {
                    srcPost.setIsDeleted(false);
                    return this.entityToDomainModel(this.postRepository.save(srcPost));
                }
        );
    }
}
