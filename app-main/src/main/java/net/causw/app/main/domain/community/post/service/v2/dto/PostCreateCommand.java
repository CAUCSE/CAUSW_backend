package net.causw.app.main.domain.community.post.service.v2.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.user.account.entity.user.User;

public record PostCreateCommand(
	String content,
	String boardId,
	Boolean isAnonymous,
	User writer,
	List<MultipartFile> images) {
}
