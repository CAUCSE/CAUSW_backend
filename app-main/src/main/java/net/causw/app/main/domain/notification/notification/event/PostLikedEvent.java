package net.causw.app.main.domain.notification.notification.event;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.user.account.entity.user.User;

public record PostLikedEvent(Post post, User liker) {
}
