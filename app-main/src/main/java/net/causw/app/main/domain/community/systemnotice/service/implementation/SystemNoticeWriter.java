package net.causw.app.main.domain.community.systemnotice.service.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.systemnotice.entity.UserSystemNoticeRead;
import net.causw.app.main.domain.community.systemnotice.repository.UserSystemNoticeReadRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class SystemNoticeWriter {

	private final UserSystemNoticeReadRepository userSystemNoticeReadRepository;

	public UserSystemNoticeRead save(UserSystemNoticeRead read) {
		return userSystemNoticeReadRepository.save(read);
	}

	public void upsertLastReadPost(String userId, String postId) {
		userSystemNoticeReadRepository.upsertLastReadPost(userId, postId);
	}
}
