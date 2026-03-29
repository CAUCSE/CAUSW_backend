package net.causw.app.main.domain.notification.notification.entity;

import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_notification")
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(name = "title")
    private String title;

    @Column(name = "body")
    private String body;

    @Column(name = "notice_type")
    @Enumerated(EnumType.STRING)
    private NoticeType noticeType;

    @Column(name = "target_id")
    private String targetId;

    @Column(name = "target_parent_id")
    private String targetParentId;


	/**
	 * 알림 생성 메서드
	 * @param user 알림을 받을 유저
	 * @param title 알림 제목
	 * @param body 알림 내용
	 * @param noticeType 알림 유형
	 * @param targetId 알림의 대상이 되는 엔티티의 ID (예: 게시물 ID, 댓글 ID 등)
	 * @param targetParentId 알림의 대상이 되는 엔티티의 상위 엔티티 ID (예: 게시물이 속한 게시판 ID 등)
	 * @return 알림 Notification 객체
	 */
	public static Notification of(
			User user,
			String title,
			String body,
			NoticeType noticeType,
			String targetId,
			String targetParentId) {
		return Notification.builder()
				.user(user)
				.title(title)
				.body(body)
				.noticeType(noticeType)
				.targetId(targetId)
				.targetParentId(targetParentId)
				.build();
	}
}
