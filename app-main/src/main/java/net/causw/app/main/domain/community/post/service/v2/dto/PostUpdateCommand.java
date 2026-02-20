package net.causw.app.main.domain.community.post.service.v2.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.user.account.entity.user.User;

public record PostUpdateCommand(
	String postId,
	String content,
	Boolean isAnonymous,
	User updater,
	List<MultipartFile> images) {
}
