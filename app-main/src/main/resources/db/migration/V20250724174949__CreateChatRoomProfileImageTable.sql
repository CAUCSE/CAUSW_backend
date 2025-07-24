-- Migration: CreateChatRoomProfileImageTable

CREATE TABLE tb_chat_room_profile_uuid_file
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_room_id VARCHAR(36) NOT NULL,
    uuid_file_id VARCHAR(36) NOT NULL UNIQUE,

    CONSTRAINT fk_chat_room FOREIGN KEY (chat_room_id)
        REFERENCES tb_chat_room (id),
    CONSTRAINT fk_uuid_file FOREIGN KEY (uuid_file_id)
        REFERENCES tb_uuid_file (id),

    INDEX        idx_chat_room_profile_chat_room_id (chat_room_id),
    INDEX        idx_chat_room_profile_uuid_file_id (uuid_file_id)
);