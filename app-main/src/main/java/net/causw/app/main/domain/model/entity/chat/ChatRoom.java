package net.causw.app.main.domain.model.entity.chat;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import lombok.*;
import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.ChatRoomProfileImage;
import net.causw.app.main.domain.model.enums.chat.ChatRoomType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_chat_room")
public class ChatRoom extends BaseEntity {

	@Column(name = "name", length = 100)
	private String roomName;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private ChatRoomType roomType;

	@Setter
	@OneToOne(cascade = {CascadeType.REMOVE, CascadeType.PERSIST}, mappedBy = "chatRoom")
	private ChatRoomProfileImage roomProfileImage;

	@OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private Set<ChatRoomParticipant> participants = new HashSet<>();

	public static ChatRoom of(
		String roomName,
		ChatRoomType roomType
	) {
		return ChatRoom.builder()
			.roomName(roomName)
			.roomType(roomType)
			.build();
	}

	public void addParticipant(ChatRoomParticipant participant) {
		participant.setChatRoom(this);
		this.participants.add(participant);
	}

	public String getRoomProfileImageUrl() {
		return Optional.ofNullable(roomProfileImage)
				.map(ChatRoomProfileImage::getUuidFile)
				.map(UuidFile::getFileUrl)
				.orElse(null);
	}
} 