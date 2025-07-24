-- Migration: CreateChatRoomAndParticipantTable
-- 채팅방 테이블
CREATE TABLE tb_chat_room
(
    id         VARCHAR(36) PRIMARY KEY,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    name       VARCHAR(100),
    type       VARCHAR(20) NOT NULL
);

-- 채팅방 참여자 테이블
CREATE TABLE tb_chat_room_participant
(
    id           VARCHAR(36) PRIMARY KEY,
    created_at   DATETIME(6),
    updated_at   DATETIME(6),
    chat_room_id VARCHAR(36) NOT NULL,
    user_id      VARCHAR(36) NOT NULL,
    role         VARCHAR(20) NOT NULL,
    last_read_at DATETIME(6),
    is_active    BOOLEAN     NOT NULL DEFAULT TRUE,
    pinned_at    DATETIME(6),
    CONSTRAINT uk_chatroom_user UNIQUE (chat_room_id, user_id),
    INDEX        idx_user_id (user_id),
    INDEX        idx_chat_room_id (chat_room_id),
    FOREIGN KEY (chat_room_id) REFERENCES tb_chat_room (id),
    FOREIGN KEY (user_id) REFERENCES tb_user (id)
);