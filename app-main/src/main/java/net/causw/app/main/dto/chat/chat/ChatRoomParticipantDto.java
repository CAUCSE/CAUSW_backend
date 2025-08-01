package net.causw.app.main.dto.chat.chat;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class ChatRoomParticipantDto {

	@Getter
	@Setter
	@Builder(access = AccessLevel.PRIVATE)
	public static class ParticipantResponse {
		private String userId;
		private String name;
		private String profileImageUrl;
		private LocalDateTime lastReadAt;

		public static ParticipantResponse of(String userId, String name, String profileImageUrl, LocalDateTime lastReadAt) {
			return ParticipantResponse.builder()
					.userId(userId)
					.name(name)
					.profileImageUrl(profileImageUrl)
					.lastReadAt(lastReadAt)
					.build();
		}
	}
}
