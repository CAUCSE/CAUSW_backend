package net.causw.app.main.domain.community.systemnotice.service.dto;

import net.causw.app.main.domain.user.account.entity.user.User;

public record SystemNoticeUpdateCommand(String title, String content, User updater) {
}
