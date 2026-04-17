package net.causw.app.main.domain.community.post.repository.query;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import net.causw.app.main.domain.community.post.entity.Post;
import org.springframework.stereotype.Repository;

import java.util.List;

// [위반 3] 동적 쿼리를 문자열 결합으로 작성 (섹션 5: DB/JPA/QueryDSL 규칙)
// 동적 쿼리는 반드시 QueryDSL의 BooleanBuilder를 사용해야 한다.
@Repository
public class PostConventionViolationQueryRepository {

    @PersistenceContext
    private EntityManager em;

    public List<Post> searchPosts(String title, String boardId) {
        String jpql = "select p from Post p where 1=1"; // ❌ 문자열 JPQL 동적 조합 시작

        if (title != null) {
            jpql += " and p.title like '%" + title + "%'"; // ❌ 문자열 결합 + SQL Injection 위험
        }
        if (boardId != null) {
            jpql += " and p.board.id = '" + boardId + "'"; // ❌ 파라미터 바인딩 미사용
        }

        return em.createQuery(jpql, Post.class).getResultList(); // ❌ 페이징 미적용
    }
}
