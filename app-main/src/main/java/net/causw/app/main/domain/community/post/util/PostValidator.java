package net.causw.app.main.domain.community.post.util;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.user.account.util.UserStateIsDeletedValidator;
import net.causw.app.main.shared.ValidatorBucket;
import net.causw.global.exception.BadRequestException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostValidator {

	/**
	 * 게시글의 글쓴이가 삭제된 사용자인지 유효성 검사
	 * @param post 게시글
	 * @throws BadRequestException 작성자가 삭제된 사용자인 경우
	 */
	public void validateWriterNotDeleted(final Post post) {
		ValidatorBucket validatorBucket = ValidatorBucket.of();
		validatorBucket
			.consistOf(UserStateIsDeletedValidator.of(post.getWriter().getState()))
			.validate();
	}

}
