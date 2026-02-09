package net.causw.app.main.domain.community.post.service.v2;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.PostAttachImage;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.service.v2.implementation.FileReader;
import net.causw.app.main.domain.asset.file.service.v2.implementation.FileWriter;
import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.board.service.implementation.BoardReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.v2.dto.PostCreateCommand;
import net.causw.app.main.domain.community.post.service.v2.dto.PostCreateResult;
import net.causw.app.main.domain.community.post.service.v2.dto.PostUpdateCommand;
import net.causw.app.main.domain.community.post.service.v2.dto.PostUpdateResult;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostReader;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostWriter;
import net.causw.app.main.domain.community.post.service.v2.util.PostMapper;
import net.causw.app.main.domain.community.post.service.v2.util.PostValidator;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
	private final PostReader postReader;
	private final PostWriter postWriter;
	private final BoardReader boardReader;
	private final FileWriter fileWriter;
	private final FileReader fileReader;
	private final BoardConfigReader boardConfigReader;

	@Transactional
	public PostCreateResult create(PostCreateCommand command) {
		User writer = command.writer();

		Board board = boardReader.getById(command.boardId());
		BoardConfig boardConfig = boardConfigReader.getByBoardId(command.boardId());
		List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(command.boardId());

		PostValidator.validateCreate(writer, board, boardConfig, boardAdminIds);

		List<UuidFile> images;
		if (command.images() != null && !command.images().isEmpty()) {
			images = fileWriter.uploadAndSaveList(command.images(), FilePath.POST);
		} else {
			images = new ArrayList<>();
		}
		Post post = PostMapper.fromCreateCommand(command, writer, board, images);

		Post savedPost = postWriter.save(post);
		return PostMapper.toCreateResult(savedPost, images.stream().map(UuidFile::getFileUrl).toList());
	}

	@Transactional
	public void deletePost(User deleter, String postId) {
		Post post = postReader.findById(postId);
		List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(post.getBoard().getId());
		PostValidator.validateDelete(deleter, post, boardAdminIds);
		// 소프트 삭제 처리
		post.setIsDeleted(true);
	}

	@Transactional
	public PostUpdateResult update(PostUpdateCommand command) {
		User updater = command.updater();
		Post post = postReader.findById(command.postId());
		List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(post.getBoard().getId());

		PostValidator.validateUpdate(updater, post, boardAdminIds);

		// 기존 이미지 삭제
		List<PostAttachImage> oldImages = post.getPostAttachImageList();
		if (oldImages != null && !oldImages.isEmpty()) {
			List<String> oldFileIds = oldImages.stream()
				.map(PostAttachImage::getUuidFile)
				.map(UuidFile::getId)
				.toList();
			List<UuidFile> oldFiles = fileReader.findByIds(oldFileIds);
			fileWriter.deleteList(oldFiles);
		}

		// 새 이미지 업로드 및 저장
		List<UuidFile> newImages;
		if (command.images() != null && !command.images().isEmpty()) {
			newImages = fileWriter.uploadAndSaveList(command.images(), FilePath.POST);
		} else {
			newImages = new ArrayList<>();
		}

		// PostAttachImage 리스트 생성
		List<PostAttachImage> newPostAttachImages = newImages.stream()
			.map(uuidFile -> PostAttachImage.of(post, uuidFile))
			.toList();

		// 게시글 업데이트
		Post updatedPost = postWriter.updateContentAndImages(post, command.content(), newPostAttachImages);

		return PostMapper.toUpdateResult(updatedPost, newImages.stream().map(UuidFile::getFileUrl).toList());
	}

}
