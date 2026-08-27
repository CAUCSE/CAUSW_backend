package net.causw.app.main.domain.community.systemnotice.entity;

import net.causw.app.main.shared.entity.AuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_user_system_notice_read")
public class UserSystemNoticeRead extends AuditableEntity {

	@Id
	@Column(name = "user_id", nullable = false)
	private String userId;

	@Column(name = "last_read_post_id")
	private String lastReadPostId;

	public static UserSystemNoticeRead of(String userId) {
		return new UserSystemNoticeRead(userId, null);
	}
}
