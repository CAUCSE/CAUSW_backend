package net.causw.app.main.domain.community.post.service.v2;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.service.v2.implementation.FileWriter;
import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.board.service.implementation.BoardReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.v2.dto.PostCreateCommand;
import net.causw.app.main.domain.community.post.service.v2.dto.PostCreateResult;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostWriter;
import net.causw.app.main.domain.community.post.service.v2.util.PostMapper;
import net.causw.app.main.domain.community.post.service.v2.util.PostValidator;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
	private final PostWriter postWriter;
	private final BoardReader boardReader;
	private final FileWriter fileWriter;
	private final BoardConfigReader boardConfigReader;

	@Transactional
	public PostCreateResult create(PostCreateCommand command) {
		User writer = command.writer();

		Board board = boardReader.getById(command.boardId());
		BoardConfig boardConfig = boardConfigReader.getByBoardId(command.boardId());
		List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(command.boardId());

		PostValidator.validateCreate(writer, board, boardConfig, boardAdminIds);

		List<UuidFile> images = fileWriter.uploadAndSaveList(command.images(), FilePath.POST);
		Post post = PostMapper.fromCreateCommand(command, writer, board, images);

		Post savedPost = postWriter.save(post);
		return PostMapper.toCreateResult(post, images.stream().map(UuidFile::getFileUrl).toList());
	}
}
