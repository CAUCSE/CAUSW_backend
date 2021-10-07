package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Board;
import net.causw.adapter.persistence.BoardRepository;
import net.causw.adapter.persistence.Circle;
import net.causw.adapter.persistence.User;
import net.causw.application.spi.BoardPort;
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.model.CircleDomainModel;
import net.causw.domain.model.UserDomainModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class BoardPortImpl implements BoardPort {
    private final BoardRepository boardRepository;

    public BoardPortImpl(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Override
    public Optional<BoardDomainModel> findById(String id) {
        return this.boardRepository.findById(id).map(this::entityToDomainModel);
    }

    @Override
    public List<BoardDomainModel> findAll() {
        return this.boardRepository.findByCircle_IdIsNullAndIsDeletedIsFalse()
                .stream()
                .map(this::entityToDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<BoardDomainModel> findByCircleId(String circleId) {
        return this.boardRepository.findByCircle_IdAndIsDeletedIsFalse(circleId)
                .stream()
                .map(this::entityToDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public BoardDomainModel create(BoardDomainModel boardDomainModel) {
        return this.entityToDomainModel(this.boardRepository.save(Board.from(boardDomainModel)));
    }

    @Override
    public Optional<BoardDomainModel> update(String id, BoardDomainModel boardDomainModel) {
        return this.boardRepository.findById(id).map(
                srcBoard -> {
                    srcBoard.setName(boardDomainModel.getName());
                    srcBoard.setDescription(boardDomainModel.getDescription());
                    srcBoard.setCreateRoles(String.join(",", boardDomainModel.getCreateRoleList()));

                    return this.entityToDomainModel(this.boardRepository.save(srcBoard));
                }
        );
    }

    @Override
    public Optional<BoardDomainModel> delete(String id) {
        return this.boardRepository.findById(id).map(
                srcBoard -> {
                    srcBoard.setIsDeleted(true);

                    return this.entityToDomainModel(this.boardRepository.save(srcBoard));
                }
        );
    }

    private BoardDomainModel entityToDomainModel(Board board) {
        CircleDomainModel circleDomainModel = null;
        if (board.getCircle() != null) {
            circleDomainModel = this.entityToDomainModel(board.getCircle());
        }

        return BoardDomainModel.of(
                board.getId(),
                board.getName(),
                board.getDescription(),
                new ArrayList<>(Arrays.asList(board.getCreateRoles().split(","))),
                board.getCategory(),
                board.getIsDeleted(),
                circleDomainModel
        );
    }

    private CircleDomainModel entityToDomainModel(Circle circle) {
        return CircleDomainModel.of(
                circle.getId(),
                circle.getName(),
                circle.getMainImage(),
                circle.getDescription(),
                circle.getIsDeleted(),
                this.entityToDomainModel(circle.getLeader())
        );
    }

    private UserDomainModel entityToDomainModel(User user) {
        return UserDomainModel.of(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPassword(),
                user.getStudentId(),
                user.getAdmissionYear(),
                user.getRole(),
                user.getProfileImage(),
                user.getState()
        );
    }
}
