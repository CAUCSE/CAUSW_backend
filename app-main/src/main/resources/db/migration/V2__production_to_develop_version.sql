-- V2__production_to_develop_version.sql
-- Flyway migration script for production to develop version

-- 3. tb_user 테이블 수정
-- academic_status enum 값 변경 (PROBATION -> SUSPEND, EXPEL 추가)
ALTER TABLE tb_user
    MODIFY COLUMN academic_status ENUM('ENROLLED', 'LEAVE_OF_ABSENCE', 'GRADUATED', 'DROPPED_OUT', 'SUSPEND', 'EXPEL', 'PROFESSOR', 'UNDETERMINED') NOT NULL;

-- major 컬럼을 NOT NULL로 변경
ALTER TABLE tb_user
    MODIFY COLUMN major VARCHAR(255) NOT NULL;

-- nickname null인 회원 데이터 처리
UPDATE tb_user u
    JOIN (
    SELECT u1.id,
    u1.name,
    ROW_NUMBER() OVER (
    PARTITION BY u1.name
    ORDER BY u1.id
    ) AS rn,
    (
    SELECT COUNT(*)
    FROM tb_user x
    WHERE x.nickname REGEXP CONCAT('^', u1.name, '(_[0-9]+)?$')
    ) AS existing_cnt
    FROM tb_user u1
    WHERE u1.nickname IS NULL
    ) t ON u.id = t.id
    SET u.nickname =
        CASE
        WHEN t.existing_cnt + t.rn - 1 = 0 THEN t.name
        ELSE CONCAT(t.name, '_', t.existing_cnt + t.rn - 1)
END
WHERE u.nickname IS NULL;

-- phone_number null 인 회원 데이터 처리
UPDATE tb_user
    JOIN (
    SELECT id,
    CONCAT('temp-', ROW_NUMBER() OVER (ORDER BY id) - 1) AS new_phone
    FROM tb_user
    WHERE phone_number IS NULL
    ) AS temp_data ON tb_user.id = temp_data.id
    SET tb_user.phone_number = temp_data.new_phone;

-- nickname 컬럼을 NOT NULL로 변경
ALTER TABLE tb_user
    MODIFY COLUMN nickname VARCHAR(255) NOT NULL;

-- phone_number 컬럼을 NOT NULL로 변경
ALTER TABLE tb_user
    MODIFY COLUMN phone_number VARCHAR(255) NOT NULL;

-- student_id 컬럼을 NOT NULL로 변경
ALTER TABLE tb_user
    MODIFY COLUMN student_id VARCHAR(255) NOT NULL;

-- locker_id 컬럼 추가
ALTER TABLE tb_user
    ADD COLUMN locker_id VARCHAR(255) NULL;

-- unique 제약조건 추가
ALTER TABLE tb_user
    ADD CONSTRAINT UK_6md40q5ok3w1xs81lgqgutfhjd UNIQUE (student_id);

ALTER TABLE tb_user
    ADD CONSTRAINT UK_6md40q5ok3w1xs81lgqgutfhjc UNIQUE (locker_id);

-- 4. tb_locker 테이블 수정
-- user_id unique 제약조건 추가
ALTER TABLE tb_locker
    ADD CONSTRAINT UK_kfx69bjr0mcgnwsluuohuvhpb UNIQUE (user_id);

-- 5. tb_user와 tb_locker 간의 FK 관계 설정
ALTER TABLE tb_user
    ADD CONSTRAINT FK6df4nyawh2hwx7pxbl9qxep2a
        FOREIGN KEY (locker_id) REFERENCES tb_locker (id);

-- 6. tb_council_fee_fake_user 테이블 수정
-- academic_status enum 값 변경
ALTER TABLE tb_council_fee_fake_user
    MODIFY COLUMN academic_status ENUM('ENROLLED', 'LEAVE_OF_ABSENCE', 'GRADUATED', 'DROPPED_OUT', 'SUSPEND', 'EXPEL', 'PROFESSOR', 'UNDETERMINED') NOT NULL;

-- 7. ceremony 테이블을 tb_ceremony로 이름 변경
RENAME TABLE ceremony TO tb_ceremony;

-- 8. tb_ceremony_attach_image_uuid_file 테이블의 FK 제약조건 수정
ALTER TABLE tb_ceremony_attach_image_uuid_file
DROP FOREIGN KEY FKpqnnjrm67d4rnlds18tutw542;

ALTER TABLE tb_ceremony_attach_image_uuid_file
    ADD CONSTRAINT FKh6htdl05nwrom8kat1s79u265
        FOREIGN KEY (ceremony_id) REFERENCES tb_ceremony (id);

-- 9. tb_ceremony_push_notification 테이블 수정
-- is_push_active 컬럼 제거
ALTER TABLE tb_ceremony_push_notification
DROP COLUMN is_push_active;

-- 10. tb_user_council_fee 테이블 수정
-- is_paid drop
ALTER TABLE tb_user_council_fee
    DROP COLUMN is_paid;

-- 11. tb_user_council_fee_log 테이블 수정
-- academic_status enum 값 변경
ALTER TABLE tb_user_council_fee_log
    MODIFY COLUMN academic_status enum('ENROLLED', 'LEAVE_OF_ABSENCE', 'GRADUATED', 'DROPPED_OUT', 'SUSPEND', 'EXPEL', 'PROFESSOR', 'UNDETERMINED') NOT NULL;

-- 12. tb_user_academic_record_application 테이블 수정
-- target_academic_status enum 값 변경
ALTER TABLE tb_user_academic_record_application
    MODIFY COLUMN target_academic_status ENUM('ENROLLED', 'LEAVE_OF_ABSENCE', 'GRADUATED', 'DROPPED_OUT', 'SUSPEND', 'EXPEL', 'PROFESSOR', 'UNDETERMINED') NOT NULL;

-- 13. tb_user_academic_record_log 테이블 수정
-- prior_academic_record_application_id 컬럼 타입 변경
ALTER TABLE tb_user_academic_record_log
    MODIFY COLUMN prior_academic_record_application_id ENUM('ENROLLED', 'LEAVE_OF_ABSENCE', 'GRADUATED', 'DROPPED_OUT', 'SUSPEND', 'EXPEL', 'PROFESSOR', 'UNDETERMINED') NOT NULL;

-- 14. 새로운 테이블들 생성
-- tb_notification_log 테이블 생성
CREATE TABLE IF NOT EXISTS tb_notification_log (
                                                   id VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NULL DEFAULT NULL,
    updated_at DATETIME(6) NULL DEFAULT NULL,
    is_read BIT(1) NOT NULL,
    notification_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    INDEX FKnm0qj8np5hpys0e9rfe4jlu3b (notification_id ASC) VISIBLE,
    INDEX FKl2q98bs3s4gdkf77nfntwbgyk (user_id ASC) VISIBLE,
    CONSTRAINT FKl2q98bs3s4gdkf77nfntwbgyk
    FOREIGN KEY (user_id) REFERENCES tb_user (id),
    CONSTRAINT FKnm0qj8np5hpys0e9rfe4jlu3b
    FOREIGN KEY (notification_id) REFERENCES tb_notification (id)
    ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- tb_user_comment_subscribe 테이블 생성
CREATE TABLE IF NOT EXISTS tb_user_comment_subscribe (
                                                         id VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NULL DEFAULT NULL,
    updated_at DATETIME(6) NULL DEFAULT NULL,
    is_subscribed BIT(1) NULL DEFAULT NULL,
    comment_id VARCHAR(255) NULL DEFAULT NULL,
    user_id VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (id),
    INDEX FKsmjykxko0wkguyduujxljy9p1 (comment_id ASC) VISIBLE,
    INDEX FKhtvmhbqf9n7whwlkpvakadg1x (user_id ASC) VISIBLE,
    CONSTRAINT FKhtvmhbqf9n7whwlkpvakadg1x
    FOREIGN KEY (user_id) REFERENCES tb_user (id),
    CONSTRAINT FKsmjykxko0wkguyduujxljy9p1
    FOREIGN KEY (comment_id) REFERENCES tb_comment (id)
    ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- tb_user_fcm_token 테이블 생성
CREATE TABLE IF NOT EXISTS tb_user_fcm_token (
                                                 user_id VARCHAR(255) NOT NULL,
    fcm_token VARCHAR(255) NULL DEFAULT NULL,
    fcm_token_value VARCHAR(255) NULL DEFAULT NULL,
    INDEX FK67eb8vcoda8mx5odsy07fc1fh (user_id ASC) VISIBLE,
    CONSTRAINT FK67eb8vcoda8mx5odsy07fc1fh
    FOREIGN KEY (user_id) REFERENCES tb_user (id)
    ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- tb_user_post_subscribe 테이블 생성
CREATE TABLE IF NOT EXISTS tb_user_post_subscribe (
                                                      id VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NULL DEFAULT NULL,
    updated_at DATETIME(6) NULL DEFAULT NULL,
    is_subscribed BIT(1) NULL DEFAULT NULL,
    post_id VARCHAR(255) NULL DEFAULT NULL,
    user_id VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (id),
    INDEX FK7sqkcrntbyp1x61eqmio1bp1e (post_id ASC) VISIBLE,
    INDEX FKlny56nrqcq87bftis6i91x2y7 (user_id ASC) VISIBLE,
    CONSTRAINT FK7sqkcrntbyp1x61eqmio1bp1e
    FOREIGN KEY (post_id) REFERENCES tb_post (id),
    CONSTRAINT FKlny56nrqcq87bftis6i91x2y7
    FOREIGN KEY (user_id) REFERENCES tb_user (id)
    ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- 15. tb_notification 테이블에 컬럼 추가
ALTER TABLE tb_notification
    ADD COLUMN target_id VARCHAR(255) NULL DEFAULT NULL;

ALTER TABLE tb_notification
    ADD COLUMN target_parent_id VARCHAR(255) NULL DEFAULT NULL COMMENT '임시 조치 (추후 삭제)';

ALTER TABLE tb_notification
    MODIFY COLUMN notice_type ENUM('POST', 'COMMENT', 'CEREMONY', 'BOARD') NULL DEFAULT NULL;