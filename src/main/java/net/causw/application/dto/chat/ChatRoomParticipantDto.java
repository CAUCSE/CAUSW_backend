package net.causw.application.dto.chat;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class ChatRoomParticipantDto {

    @Getter
    @Setter
    @Builder(access = AccessLevel.PRIVATE)
    public static class ParticipantResponse {
        private String userId;
        private String name;
        private String profileImageUrl;
        private LocalDateTime updatedAt;

        public static ParticipantResponse of(String userId, String name, String profileImageUrl, LocalDateTime updatedAt) {
            return ParticipantResponse.builder()
                    .userId(userId)
                    .name(name)
                    .profileImageUrl(profileImageUrl)
                    .updatedAt(updatedAt)
                    .build();
        }
    }
}
