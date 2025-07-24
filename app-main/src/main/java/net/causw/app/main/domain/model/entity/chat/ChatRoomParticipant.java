package net.causw.app.main.domain.model.entity.chat;

import java.time.LocalDateTime;
import java.util.Optional;

import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.UserProfileImage;
import net.causw.app.main.domain.model.enums.chat.ParticipantRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
	name = "tb_chat_room_participant",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_chatroom_user",
			columnNames = {"chat_room_id", "user_id"}
		)
	},
	indexes = {
		@Index(name = "idx_user_id", columnList = "user_id"),
		@Index(name = "idx_chat_room_id", columnList = "chat_room_id")
	}
)
public class ChatRoomParticipant extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_room_id", nullable = false)
	private ChatRoom chatRoom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	@Builder.Default
	private ParticipantRole role = ParticipantRole.MEMBER;

	@Column(name = "last_read_at")
	private LocalDateTime lastReadAt;

	@Column(name = "is_active", nullable = false)
	@Builder.Default
	private Boolean isActive = true;

	@Column(name = "pinned_at")
	private LocalDateTime pinnedAt;

	public static ChatRoomParticipant of(User user, ParticipantRole role) {
		return ChatRoomParticipant.builder()
			.user(user)
			.role(role)
			.build();
	}

	public void updateLastReadAt() {
		this.lastReadAt = LocalDateTime.now();
	}

	public void deactivate() {
		this.isActive = false;
	}

	public void pin() {
		this.pinnedAt = LocalDateTime.now();
	}

	public void unpin() {
		this.pinnedAt = null;
	}

	public boolean isPinned() {
		return pinnedAt != null;
	}

	protected void setChatRoom(ChatRoom chatRoom) {
		this.chatRoom = chatRoom;
	}

	public String getUserProfileImageUrl() {
		return Optional.ofNullable(user)
				.map(User::getUserProfileImage)
				.map(UserProfileImage::getUuidFile)
				.map(UuidFile::getFileUrl)
				.orElse(null);
	}
} 