package net.causw.app.main.domain.community.comment.service.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.comment.entity.CommentAnonymousNickname;
import net.causw.app.main.domain.community.comment.repository.CommentAnonymousNicknameRepository;

import lombok.RequiredArgsConstructor;

/**
 * 익명 닉네임 매핑을 별도 트랜잭션에서 저장한다.
 *
 * <p>{@link CommentAnonymousNicknameResolver}는 댓글 생성 트랜잭션 도중 유니크 제약 충돌을
 * 전제로 새 후보를 재시도한다. 같은 트랜잭션(영속성 컨텍스트)에서 flush 실패를 반복하면
 * Hibernate 세션이 불안정한 상태가 되어 이후 조회/재시도가 깨질 수 있으므로, 각 저장 시도를
 * {@link Propagation#REQUIRES_NEW}로 완전히 분리된 트랜잭션에서 실행해 실패가 바깥 댓글 생성
 * 트랜잭션에 영향을 주지 않도록 한다.</p>
 */
@Component
@RequiredArgsConstructor
public class CommentAnonymousNicknameWriter {

	private final CommentAnonymousNicknameRepository commentAnonymousNicknameRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void save(String postId, String userId, String nickname) {
		commentAnonymousNicknameRepository.saveAndFlush(CommentAnonymousNickname.of(postId, userId, nickname));
	}
}
