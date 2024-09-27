package net.causw.domain.model.enums.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NoticeType {
    POST("게시물 알림"),
    COMMENT("댓글 알림");

    private final String type;
}
