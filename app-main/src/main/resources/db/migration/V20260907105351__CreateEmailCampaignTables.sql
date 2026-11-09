-- Migration: CreateEmailCampaignTables
CREATE TABLE tb_email_campaign
(
    id                        VARCHAR(255) NOT NULL PRIMARY KEY,
    subject                   VARCHAR(255) NOT NULL,
    sanitized_html            LONGTEXT     NOT NULL,
    from_address              VARCHAR(255) NOT NULL,
    reply_to_address          VARCHAR(255) NULL,
    filter_json               JSON         NOT NULL,
    recipient_count           BIGINT       NOT NULL DEFAULT 0,
    pending_count             BIGINT       NOT NULL DEFAULT 0,
    sent_count                BIGINT       NOT NULL DEFAULT 0,
    failed_count              BIGINT       NOT NULL DEFAULT 0,
    skipped_count             BIGINT       NOT NULL DEFAULT 0,
    status                    VARCHAR(30)  NOT NULL,
    created_by_user_id        VARCHAR(255) NOT NULL,
    send_requested_by_user_id VARCHAR(255) NULL,
    send_requested_at         DATETIME(6)  NULL,
    queued_at                 DATETIME(6)  NULL,
    started_at                DATETIME(6)  NULL,
    completed_at              DATETIME(6)  NULL,
    failure_detail            TEXT         NULL,
    created_at                DATETIME(6)  NOT NULL,
    updated_at                DATETIME(6)  NOT NULL,
    INDEX idx_email_campaign_status_created_at (status, created_at)
);

CREATE TABLE tb_email_campaign_recipient
(
    id                BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    campaign_id       VARCHAR(255) NOT NULL,
    user_id           VARCHAR(255) NOT NULL,
    email_snapshot    VARCHAR(255) NOT NULL,
    status            VARCHAR(30)  NOT NULL,
    ses_message_id    VARCHAR(255) NULL,
    attempt_count     INT          NOT NULL DEFAULT 0,
    last_attempt_at   DATETIME(6)  NULL,
    next_attempt_at   DATETIME(6)  NULL,
    claimed_at        DATETIME(6)  NULL,
    sent_at           DATETIME(6)  NULL,
    last_error_code   VARCHAR(100) NULL,
    last_error_detail VARCHAR(500) NULL,
    skip_reason       VARCHAR(100) NULL,
    created_at        DATETIME(6)  NOT NULL,
    updated_at        DATETIME(6)  NOT NULL,
    CONSTRAINT fk_email_campaign_recipient_campaign
        FOREIGN KEY (campaign_id) REFERENCES tb_email_campaign (id) ON DELETE CASCADE,
    CONSTRAINT uk_email_campaign_recipient_campaign_user UNIQUE (campaign_id, user_id),
    INDEX idx_email_campaign_recipient_work (campaign_id, status, next_attempt_at),
    INDEX idx_email_campaign_recipient_claim_recovery (status, claimed_at)
);
