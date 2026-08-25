package net.causw.app.main.domain.community.post.repository.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.util.ObjectFixtures;

@DisplayName("PostReadQueryContext")
class PostReadQueryContextTest {

	@Test
	@DisplayName("시스템 관리자 여부와 차단 목록을 조회 컨텍스트에 반영한다")
	void from_shouldReflectSystemAdminAndBlockedWriters() {
		User admin = ObjectFixtures.getCertifiedUserWithId("admin-id");
		admin.setRoles(Set.of(Role.SYSTEM_ADMIN));
		Set<String> blockedWriterIds = Set.of("blocked-writer-id");

		PostReadQueryContext context = PostReadQueryContext.from(admin, blockedWriterIds);

		assertThat(context.systemAdmin()).isTrue();
		assertThat(context.blockedWriterIds()).isEqualTo(blockedWriterIds);
	}

	@Test
	@DisplayName("일반 재학생의 읽기 범위를 BOTH와 ENROLLED로 계산한다")
	void from_shouldReflectAcademicReadScopes() {
		User viewer = ObjectFixtures.getCertifiedUserWithId("viewer-id");

		PostReadQueryContext context = PostReadQueryContext.from(viewer, Set.of());

		assertThat(context.systemAdmin()).isFalse();
		assertThat(context.readableScopes()).containsExactlyInAnyOrder(
			BoardReadScope.BOTH,
			BoardReadScope.ENROLLED);
	}
}
