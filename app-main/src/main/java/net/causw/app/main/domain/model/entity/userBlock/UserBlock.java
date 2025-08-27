package net.causw.app.main.domain.model.entity.userBlock;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.report.BlockScope;

@Entity
@Table(name = "tb_user_block",
    uniqueConstraints = {
        // 활성 차단은 동일 스코프로 중복 생성 방지
        @UniqueConstraint(name = "uq_block_active",
            columnNames = {"blocker_id", "blockee_id", "scope", "scope_ref_id", "active"})
    }
)
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserBlock extends BaseEntity {

    /**
     * 누가 차단했는지
     */
    @Column(name = "blocker_id", nullable = false, updatable = false)
    private String blockerId;

    /**
     * 누구를 차단했는지
     */
    @Column(name = "blockee_id", nullable = false, updatable = false)
    private String blockeeId;

    /**
     * 차단 경로
     * - ex. CHILD_COMMENT, COMMENT, POST
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 20)
    private BlockScope scope;

    /**
     *  스코프 대상 ID
     *  - (POST/COMMENT/CHILD_COMMENT 등)
     */
    @Column(name = "scope_ref_id", nullable = false)
    private String scopeRefId;

    /**
     * 상대가 당시 익명 노출이었는지(차단 해제 리스트 UI용 메타)
     */
    @Column(name = "blockee_anonymous", nullable = false)
    private boolean blockeeAnonymous;

    /**
     * 당시 본 컨텐츠 스냅샷(최대 길이 제한 권장)
     */
    @Lob
    @Comment("차단 트리거가 된 게시글/댓글 내용 일부 스냅샷")
    @Column(name = "content_snapshot")
    private String contentSnapshot;

    /**
     * 활성/비활성(해제) 플래그
     */
    @Column(name = "active", nullable = false)
    private boolean active;

    /**
     * 새로운 차단 생성
     *
     * @param blockerId 차단한 사용자 ID
     * @param blockeeId 차단당한 사용자 ID
     * @param scope 차단 경로
     * @param scopeRefId 스코프 대상 ID
     * @param blockeeAnonymous 피차단자 익명 여부
     * @param contentSnapshot 컨텐츠 스냅샷
     * @return 새로운 UserBlock 인스턴스
     */
    public static UserBlock create(
        Long blockerId,
        Long blockeeId,
        BlockScope scope,
        Long scopeRefId,
        boolean blockeeAnonymous,
        String contentSnapshot) {

        validateBlockRequest(blockerId, blockeeId);

        return UserBlock.builder()
            .blockerId(blockerId)
            .blockeeId(blockeeId)
            .scope(scope)
            .scopeRefId(scopeRefId)
            .blockeeAnonymous(blockeeAnonymous)
            .contentSnapshot(truncateContentSnapshot(contentSnapshot))
            .active(true)
            .build();
    }

    /**
     * 댓글 차단 생성
     *
     * @param blockerId 차단한 사용자 ID
     * @param blockeeId 차단당한 사용자 ID
     * @param commentId 댓글 ID
     * @param isAnonymous 익명 여부
     * @param commentContent 댓글 내용
     * @return 댓글 차단 UserBlock 인스턴스
     */
    public static UserBlock createForComment(
        Long blockerId,
        Long blockeeId,
        Long commentId,
        boolean isAnonymous,
        String commentContent) {

        return create(
            blockerId,
            blockeeId,
            BlockScope.COMMENT,
            commentId,
            isAnonymous,
            commentContent
        );
    }

    /**
     * 대댓글 차단 생성
     *
     * @param blockerId 차단한 사용자 ID
     * @param blockeeId 차단당한 사용자 ID
     * @param childCommentId 대댓글 ID
     * @param isAnonymous 익명 여부
     * @param childCommentContent 댓글 내용
     * @return 대댓글 차단 UserBlock 인스턴스
     */
    public static UserBlock createForChildComment(
        Long blockerId,
        Long blockeeId,
        Long childCommentId,
        boolean isAnonymous,
        String childCommentContent) {

        return create(
            blockerId,
            blockeeId,
            BlockScope.CHILD_COMMENT,
            childCommentId,
            isAnonymous,
            childCommentContent
        );
    }

    /**
     * 게시글 차단 생성
     *
     * @param blockerId 차단한 사용자 ID
     * @param blockeeId 차단당한 사용자 ID
     * @param postId 게시글 ID
     * @param isAnonymous 익명 여부
     * @param postContent 게시글 내용
     * @return 게시글 차단 UserBlock 인스턴스
     */
    public static UserBlock createForPost(
        Long blockerId,
        Long blockeeId,
        Long postId,
        boolean isAnonymous,
        String postContent) {

        return create(
            blockerId,
            blockeeId,
            BlockScope.POST,
            postId,
            isAnonymous,
            postContent
        );
    }

    /**
     * 컨텐츠 스냅샷 길이 제한 (성능상 이유)
     */
    private static String truncateContentSnapshot(String content) {
        if (content == null) {
            return null;
        }

        final int MAX_SNAPSHOT_LENGTH = 100;

        if (content.length() > MAX_SNAPSHOT_LENGTH) {
            return content.substring(0, MAX_SNAPSHOT_LENGTH) + "...";
        }

        return content;
    }

    /**
     * 차단 요청 유효성 검증
     */
    private static void validateBlockRequest(Long blockerId, Long blockeeId) {
        if (blockerId == null || blockeeId == null) {
            throw new IllegalArgumentException("차단자 ID와 피차단자 ID는 필수입니다.");
        }

        if (blockerId.equals(blockeeId)) {
            throw new IllegalArgumentException("자기 자신을 차단할 수 없습니다.");
        }
    }

    @Override
    public String toString() {
        return "UserBlock{" +
            "id=" + getId() +
            ", blockerId=" + blockerId +
            ", blockeeId=" + blockeeId +
            ", scope=" + scope +
            ", scopeRefId=" + scopeRefId +
            ", active=" + active +
            '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserBlock userBlock = (UserBlock) obj;
        return getId() != null && getId().equals(userBlock.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}