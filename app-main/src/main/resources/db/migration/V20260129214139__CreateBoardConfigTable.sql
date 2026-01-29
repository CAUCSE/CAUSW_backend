-- Migration: CreateBoardConfigTable
/**
  public class BoardConfig extends BaseEntity {

	private String boardId;

	@Column(name = "is_anonymous", nullable = false)
	private boolean isAnonymous;

	@Column(name = "read_scope", nullable = false)
	private BoardReadScope readScope;

	@Column(name = "write_scope", nullable = false)
	private BoardWriteScope writeScope;

	@Column(name = "is_notice", nullable = false)
	private boolean isNotice;

	@Column(name = "visibility", nullable = false)
	private BoardVisibility visibility;
}
 */
CREATE TABLE tb_board_config (
    id varchar(255) NOT NULL,
    board_id VARCHAR(255) NOT NULL,
    is_anonymous BOOLEAN NOT NULL,
    read_scope VARCHAR(50) NOT NULL,
    write_scope VARCHAR(50) NOT NULL,
    is_notice BOOLEAN NOT NULL,
    visibility VARCHAR(50) NOT NULL,
    created_at datetime(6) DEFAULT NULL,
    updated_at datetime(6) DEFAULT NULL
);
