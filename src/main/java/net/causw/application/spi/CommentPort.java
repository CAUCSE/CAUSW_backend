package net.causw.application.spi;

import net.causw.domain.model.CommentDomainModel;
import net.causw.domain.model.PostDomainModel;
import net.causw.domain.model.UserDomainModel;

import java.util.Optional;

public interface CommentPort {
    Optional<CommentDomainModel> findById(String id);

    CommentDomainModel create(
            CommentDomainModel commentDomainModel,
            UserDomainModel writer,
            PostDomainModel post
    );

    CommentDomainModel create(
            CommentDomainModel commentDomainModel,
            UserDomainModel writer,
            PostDomainModel post,
            CommentDomainModel parentComment
    );
}
