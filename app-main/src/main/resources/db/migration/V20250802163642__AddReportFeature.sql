-- Migration: AddReportFeature
ALTER TABLE tb_user
    ADD COLUMN report_count INT NOT NULL DEFAULT 0;

-- SUSPENDED 상태 추가
ALTER TABLE tb_user
    MODIFY COLUMN state ENUM('AWAIT', 'ACTIVE', 'INACTIVE', 'REJECT', 'DROP', 'SUSPENDED', 'DELETED') NOT NULL;


-- Report테이블 생성
CREATE TABLE tb_report
(
    id            VARCHAR(36) NOT NULL PRIMARY KEY,
    reporter_id   VARCHAR(36) NOT NULL,
    report_type   VARCHAR(20) NOT NULL,
    target_id     VARCHAR(36) NOT NULL,
    report_reason VARCHAR(30) NOT NULL,
    created_at    DATETIME    NOT NULL,
    updated_at    DATETIME    NOT NULL,

    CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_id) REFERENCES tb_user (id),
    CONSTRAINT unique_user_content_report UNIQUE (reporter_id, report_type, target_id)
);

-- 조회 성능을 위한 Index 생성
CREATE INDEX idx_report_target_id ON tb_report(target_id);
CREATE INDEX idx_report_type ON tb_report(report_type);
CREATE INDEX idx_report_created_at ON tb_report(created_at);
CREATE INDEX idx_user_report_count ON tb_user(report_count);