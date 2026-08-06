package net.causw.app.main.domain.notification.notification.service.policy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LikePostNotificationPolicy {

	public static boolean isMilestone(long likeCount) {
		if ((likeCount >= 1 && likeCount <= 5)
			|| likeCount == 10
			|| likeCount == 50
			|| likeCount == 100
			|| likeCount == 500) {
			return true;
		}
		return likeCount >= 1000 && likeCount % 1000 == 0;
	}
}
