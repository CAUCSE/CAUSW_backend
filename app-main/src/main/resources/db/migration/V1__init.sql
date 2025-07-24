-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Table `BATCH_JOB_INSTANCE`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `BATCH_JOB_INSTANCE` (
                                                    `JOB_INSTANCE_ID` BIGINT NOT NULL,
                                                    `VERSION` BIGINT NULL DEFAULT NULL,
                                                    `JOB_NAME` VARCHAR(100) NOT NULL,
    `JOB_KEY` VARCHAR(32) NOT NULL,
    PRIMARY KEY (`JOB_INSTANCE_ID`),
    UNIQUE INDEX `JOB_INST_UN` (`JOB_NAME` ASC, `JOB_KEY` ASC) VISIBLE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `BATCH_JOB_EXECUTION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `BATCH_JOB_EXECUTION` (
                                                     `JOB_EXECUTION_ID` BIGINT NOT NULL,
                                                     `VERSION` BIGINT NULL DEFAULT NULL,
                                                     `JOB_INSTANCE_ID` BIGINT NOT NULL,
                                                     `CREATE_TIME` DATETIME(6) NOT NULL,
    `START_TIME` DATETIME(6) NULL DEFAULT NULL,
    `END_TIME` DATETIME(6) NULL DEFAULT NULL,
    `STATUS` VARCHAR(10) NULL DEFAULT NULL,
    `EXIT_CODE` VARCHAR(2500) NULL DEFAULT NULL,
    `EXIT_MESSAGE` VARCHAR(2500) NULL DEFAULT NULL,
    `LAST_UPDATED` DATETIME(6) NULL DEFAULT NULL,
    PRIMARY KEY (`JOB_EXECUTION_ID`),
    INDEX `JOB_INST_EXEC_FK` (`JOB_INSTANCE_ID` ASC) VISIBLE,
    CONSTRAINT `JOB_INST_EXEC_FK`
    FOREIGN KEY (`JOB_INSTANCE_ID`)
    REFERENCES `BATCH_JOB_INSTANCE` (`JOB_INSTANCE_ID`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `BATCH_JOB_EXECUTION_CONTEXT`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `BATCH_JOB_EXECUTION_CONTEXT` (
                                                             `JOB_EXECUTION_ID` BIGINT NOT NULL,
                                                             `SHORT_CONTEXT` VARCHAR(2500) NOT NULL,
    `SERIALIZED_CONTEXT` TEXT NULL DEFAULT NULL,
    PRIMARY KEY (`JOB_EXECUTION_ID`),
    CONSTRAINT `JOB_EXEC_CTX_FK`
    FOREIGN KEY (`JOB_EXECUTION_ID`)
    REFERENCES `BATCH_JOB_EXECUTION` (`JOB_EXECUTION_ID`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `BATCH_JOB_EXECUTION_PARAMS`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `BATCH_JOB_EXECUTION_PARAMS` (
                                                            `JOB_EXECUTION_ID` BIGINT NOT NULL,
                                                            `PARAMETER_NAME` VARCHAR(100) NOT NULL,
    `PARAMETER_TYPE` VARCHAR(100) NOT NULL,
    `PARAMETER_VALUE` VARCHAR(2500) NULL DEFAULT NULL,
    `IDENTIFYING` CHAR(1) NOT NULL,
    INDEX `JOB_EXEC_PARAMS_FK` (`JOB_EXECUTION_ID` ASC) VISIBLE,
    CONSTRAINT `JOB_EXEC_PARAMS_FK`
    FOREIGN KEY (`JOB_EXECUTION_ID`)
    REFERENCES `BATCH_JOB_EXECUTION` (`JOB_EXECUTION_ID`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `BATCH_JOB_EXECUTION_SEQ`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `BATCH_JOB_EXECUTION_SEQ` (
                                                         `ID` BIGINT NOT NULL,
                                                         `UNIQUE_KEY` CHAR(1) NOT NULL,
    UNIQUE INDEX `UNIQUE_KEY_UN` (`UNIQUE_KEY` ASC))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `BATCH_JOB_SEQ`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `BATCH_JOB_SEQ` (
                                               `ID` BIGINT NOT NULL,
                                               `UNIQUE_KEY` CHAR(1) NOT NULL,
    UNIQUE INDEX `UNIQUE_KEY_UN` (`UNIQUE_KEY` ASC))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `BATCH_STEP_EXECUTION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `BATCH_STEP_EXECUTION` (
                                                      `STEP_EXECUTION_ID` BIGINT NOT NULL,
                                                      `VERSION` BIGINT NOT NULL,
                                                      `STEP_NAME` VARCHAR(100) NOT NULL,
    `JOB_EXECUTION_ID` BIGINT NOT NULL,
    `CREATE_TIME` DATETIME(6) NOT NULL,
    `START_TIME` DATETIME(6) NULL DEFAULT NULL,
    `END_TIME` DATETIME(6) NULL DEFAULT NULL,
    `STATUS` VARCHAR(10) NULL DEFAULT NULL,
    `COMMIT_COUNT` BIGINT NULL DEFAULT NULL,
    `READ_COUNT` BIGINT NULL DEFAULT NULL,
    `FILTER_COUNT` BIGINT NULL DEFAULT NULL,
    `WRITE_COUNT` BIGINT NULL DEFAULT NULL,
    `READ_SKIP_COUNT` BIGINT NULL DEFAULT NULL,
    `WRITE_SKIP_COUNT` BIGINT NULL DEFAULT NULL,
    `PROCESS_SKIP_COUNT` BIGINT NULL DEFAULT NULL,
    `ROLLBACK_COUNT` BIGINT NULL DEFAULT NULL,
    `EXIT_CODE` VARCHAR(2500) NULL DEFAULT NULL,
    `EXIT_MESSAGE` VARCHAR(2500) NULL DEFAULT NULL,
    `LAST_UPDATED` DATETIME(6) NULL DEFAULT NULL,
    PRIMARY KEY (`STEP_EXECUTION_ID`),
    INDEX `JOB_EXEC_STEP_FK` (`JOB_EXECUTION_ID` ASC) VISIBLE,
    CONSTRAINT `JOB_EXEC_STEP_FK`
    FOREIGN KEY (`JOB_EXECUTION_ID`)
    REFERENCES `BATCH_JOB_EXECUTION` (`JOB_EXECUTION_ID`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `BATCH_STEP_EXECUTION_CONTEXT`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `BATCH_STEP_EXECUTION_CONTEXT` (
                                                              `STEP_EXECUTION_ID` BIGINT NOT NULL,
                                                              `SHORT_CONTEXT` VARCHAR(2500) NOT NULL,
    `SERIALIZED_CONTEXT` TEXT NULL DEFAULT NULL,
    PRIMARY KEY (`STEP_EXECUTION_ID`),
    CONSTRAINT `STEP_EXEC_CTX_FK`
    FOREIGN KEY (`STEP_EXECUTION_ID`)
    REFERENCES `BATCH_STEP_EXECUTION` (`STEP_EXECUTION_ID`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `BATCH_STEP_EXECUTION_SEQ`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `BATCH_STEP_EXECUTION_SEQ` (
                                                          `ID` BIGINT NOT NULL,
                                                          `UNIQUE_KEY` CHAR(1) NOT NULL,
    UNIQUE INDEX `UNIQUE_KEY_UN` (`UNIQUE_KEY` ASC))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_locker_location`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_locker_location` (
                                                    `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `name` ENUM('SECOND', 'THIRD', 'FOURTH') NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_5n4i9ncqv3c3ns9tunyxq1vww` (`name` ASC) VISIBLE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_locker`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_locker` (
                                           `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `expire_date` DATETIME(6) NULL DEFAULT NULL,
    `is_active` BIT(1) NULL DEFAULT b'1',
    `locker_number` BIGINT NOT NULL,
    `location_id` VARCHAR(255) NOT NULL,
    `user_id` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_kfx69bjr0mcgnwsluuohuvhpb` (`user_id` ASC) VISIBLE,
    INDEX `FKdkeceafnif5f6kji4f0kmie6n` (`location_id` ASC) VISIBLE,
    CONSTRAINT `FKdkeceafnif5f6kji4f0kmie6n`
    FOREIGN KEY (`location_id`)
    REFERENCES `tb_locker_location` (`id`),
    CONSTRAINT `FKfprmp8bd5hasx3nh5h9xuaijt`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_user`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_user` (
                                         `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `academic_status` ENUM('ENROLLED', 'LEAVE_OF_ABSENCE', 'GRADUATED', 'DROPPED_OUT', 'SUSPEND', 'EXPEL', 'PROFESSOR', 'UNDETERMINED') NOT NULL,
    `academic_status_note` VARCHAR(255) NULL DEFAULT NULL,
    `admission_year` INT NOT NULL,
    `current_completed_semester` INT NULL DEFAULT NULL,
    `email` VARCHAR(255) NOT NULL,
    `graduation_type` ENUM('FEBRUARY', 'AUGUST') NULL DEFAULT NULL,
    `graduation_year` INT NULL DEFAULT NULL,
    `is_v2` BIT(1) NOT NULL,
    `major` VARCHAR(255) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `nickname` VARCHAR(255) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `phone_number` VARCHAR(255) NOT NULL,
    `rejection_or_drop_reason` VARCHAR(255) NULL DEFAULT NULL,
    `state` ENUM('AWAIT', 'ACTIVE', 'INACTIVE', 'REJECT', 'DROP', 'DELETED') NOT NULL,
    `student_id` VARCHAR(255) NOT NULL,
    `locker_id` VARCHAR(255) NULL DEFAULT NULL,
    `fcm_token` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_4vih17mube9j7cqyjlfbcrk4m` (`email` ASC) VISIBLE,
    UNIQUE INDEX `UK_ig0bbysxr6nnpxo4qn2btdcc8` (`nickname` ASC) VISIBLE,
    UNIQUE INDEX `UK_qi5yr54j76lu2meatpwefocym` (`phone_number` ASC) VISIBLE,
    UNIQUE INDEX `UK_djjmuep18k7xs81lgqgutfhjd` (`student_id` ASC) VISIBLE,
    UNIQUE INDEX `UK_6md40q5ok3w1xs4ugdds0kcx2` (`locker_id` ASC) VISIBLE,
    CONSTRAINT `FK6df4nyawh2hwx7pxbl9qxep2a`
    FOREIGN KEY (`locker_id`)
    REFERENCES `tb_locker` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `ceremony`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ceremony` (
                                          `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `ceremony_category` ENUM('MARRIAGE', 'FUNERAL', 'ETC') NOT NULL,
    `ceremony_state` ENUM('ACCEPT', 'REJECT', 'AWAIT', 'CLOSE') NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    `end_date` DATE NOT NULL,
    `note` VARCHAR(255) NULL DEFAULT NULL,
    `start_date` DATE NOT NULL,
    `user_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `FK68hi5kpp95tb3qu6fcfxq7fa7` (`user_id` ASC) VISIBLE,
    CONSTRAINT `FK68hi5kpp95tb3qu6fcfxq7fa7`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_circle`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_circle` (
                                           `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `circle_tax` INT NULL DEFAULT NULL,
    `description` VARCHAR(255) NULL DEFAULT NULL,
    `is_deleted` BIT(1) NULL DEFAULT b'0',
    `is_recruit` BIT(1) NULL DEFAULT NULL,
    `name` VARCHAR(255) NOT NULL,
    `recruit_end_date` DATETIME(6) NULL DEFAULT NULL,
    `recruit_members` INT NULL DEFAULT NULL,
    `leader_id` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_faag85xipb292m553obdly74p` (`leader_id` ASC) VISIBLE,
    CONSTRAINT `FKsyy3e5thj8ighbjoa6owk3a37`
    FOREIGN KEY (`leader_id`)
    REFERENCES `tb_user` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_board`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_board` (
                                          `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `category` VARCHAR(255) NOT NULL,
    `create_role_list` VARCHAR(255) NOT NULL,
    `description` VARCHAR(255) NULL DEFAULT NULL,
    `is_default` BIT(1) NOT NULL DEFAULT b'0',
    `is_default_notice` BIT(1) NOT NULL DEFAULT b'0',
    `is_deleted` BIT(1) NOT NULL DEFAULT b'0',
    `is_anonymous_allowed` BIT(1) NOT NULL DEFAULT b'0',
    `name` VARCHAR(255) NOT NULL,
    `circle_id` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_79ufcd1db5uudn8hf4tg00s5t` (`name` ASC) VISIBLE,
    INDEX `FKf56nd4y1y3jqyec9a19gl4i43` (`circle_id` ASC) VISIBLE,
    CONSTRAINT `FKf56nd4y1y3jqyec9a19gl4i43`
    FOREIGN KEY (`circle_id`)
    REFERENCES `tb_circle` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_board_apply`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_board_apply` (
                                                `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `accept_status` ENUM('AWAIT', 'ACCEPTED', 'REJECT') NOT NULL,
    `board_name` VARCHAR(255) NOT NULL,
    `category` VARCHAR(255) NOT NULL,
    `create_role_list` VARCHAR(255) NOT NULL,
    `description` VARCHAR(255) NULL DEFAULT NULL,
    `is_annonymous_allowed` BIT(1) NOT NULL DEFAULT b'0',
    `user_id` VARCHAR(255) NOT NULL,
    `circle_id` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `FKde6dcpov54bln9k8pm0eo02wj` (`user_id` ASC) VISIBLE,
    INDEX `FK77llxkuuxhgumf8kjvl5yi9xx` (`circle_id` ASC) VISIBLE,
    CONSTRAINT `FK77llxkuuxhgumf8kjvl5yi9xx`
    FOREIGN KEY (`circle_id`)
    REFERENCES `tb_circle` (`id`),
    CONSTRAINT `FKde6dcpov54bln9k8pm0eo02wj`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_calendar`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_calendar` (
                                             `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `month` INT NOT NULL,
    `year` INT NOT NULL,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_uuid_file`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_uuid_file` (
                                              `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `extension` VARCHAR(255) NOT NULL,
    `file_key` VARCHAR(255) NOT NULL,
    `file_path` ENUM('USER_PROFILE', 'USER_ADMISSION', 'USER_ACADEMIC_RECORD_APPLICATION', 'CIRCLE_PROFILE', 'POST', 'CALENDAR', 'EVENT', 'ETC') NOT NULL,
    `file_url` TEXT NOT NULL,
    `is_used` BIT(1) NOT NULL DEFAULT b'1',
    `raw_file_name` VARCHAR(255) NOT NULL,
    `uuid` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_ok1ekub9dpdyhsx9vexnkn1sn` (`file_key` ASC) VISIBLE,
    UNIQUE INDEX `UK_o61trieqgmab71n786b9aic93` (`uuid` ASC) VISIBLE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_calendar_attach_image_uuid_file`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_calendar_attach_image_uuid_file` (
                                                                    `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `calendar_id` VARCHAR(255) NOT NULL,
    `uuid_file_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_ovufeprpl55xmb2xrw2i8itku` (`calendar_id` ASC) VISIBLE,
    UNIQUE INDEX `UK_s2koor1hv4d06sf3ui0r5rlte` (`uuid_file_id` ASC) VISIBLE,
    INDEX `idx_calendar_attach_image_calendar_id` (`calendar_id` ASC) VISIBLE,
    INDEX `idx_calendar_attach_image_uuid_file_id` (`uuid_file_id` ASC) VISIBLE,
    CONSTRAINT `FKdw5exxxgluthyq6ip8jj3vwea`
    FOREIGN KEY (`calendar_id`)
    REFERENCES `tb_calendar` (`id`),
    CONSTRAINT `FKmfj3gxbrsl39bow3q5hgmlxhd`
    FOREIGN KEY (`uuid_file_id`)
    REFERENCES `tb_uuid_file` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_ceremony`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_ceremony` (
                                             `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `ceremony_category` ENUM('MARRIAGE', 'FUNERAL', 'ETC') NOT NULL,
    `ceremony_state` ENUM('ACCEPT', 'REJECT', 'AWAIT', 'CLOSE') NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    `end_date` DATE NOT NULL,
    `note` VARCHAR(255) NULL DEFAULT NULL,
    `start_date` DATE NOT NULL,
    `user_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `FK1r9gjgpwdqf6rxp0i9l8k3qgo` (`user_id` ASC) VISIBLE,
    CONSTRAINT `FK1r9gjgpwdqf6rxp0i9l8k3qgo`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_ceremony_attach_image_uuid_file`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_ceremony_attach_image_uuid_file` (
                                                                    `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `ceremony_id` VARCHAR(255) NOT NULL,
    `uuid_file_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_iqegoxxctvtkjkrm2ge33j26d` (`uuid_file_id` ASC) VISIBLE,
    INDEX `idx_ceremony_attach_image_ceremony_id` (`ceremony_id` ASC) VISIBLE,
    INDEX `idx_ceremony_attach_image_uuid_file_id` (`uuid_file_id` ASC) VISIBLE,
    CONSTRAINT `FKd9t63v2tqpoa16pe6u1tlspub`
    FOREIGN KEY (`uuid_file_id`)
    REFERENCES `tb_uuid_file` (`id`),
    CONSTRAINT `FKh6htdl05nwrom8kat1s79u265`
    FOREIGN KEY (`ceremony_id`)
    REFERENCES `tb_ceremony` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_ceremony_push_notification`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_ceremony_push_notification` (
                                                               `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `is_notification_active` BIT(1) NOT NULL,
    `is_set_all` BIT(1) NOT NULL,
    `user_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_dc7o4ac8ym4ar2q5t4ld863py` (`user_id` ASC) VISIBLE,
    CONSTRAINT `FKndjbnnmuuyj2v8myo498jci87`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_ceremony_subscribe_year`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_ceremony_subscribe_year` (
                                                            `notification_id` VARCHAR(255) NOT NULL,
    `admission_year` INT NULL DEFAULT NULL,
    INDEX `FK36lyx1t3etyvq6gloftmklrt3` (`notification_id` ASC) VISIBLE,
    CONSTRAINT `FK36lyx1t3etyvq6gloftmklrt3`
    FOREIGN KEY (`notification_id`)
    REFERENCES `tb_ceremony_push_notification` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_vote`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_vote` (
                                         `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `allow_anonymous` BIT(1) NOT NULL,
    `allow_multiple` BIT(1) NOT NULL,
    `is_end` BIT(1) NOT NULL,
    `title` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_form`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_form` (
                                         `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `enrolled_registered_semester` VARCHAR(255) NULL DEFAULT NULL,
    `leave_of_absence_registered_semester` VARCHAR(255) NULL DEFAULT NULL,
    `form_type` ENUM('POST_FORM', 'CIRCLE_APPLICATION_FORM') NOT NULL,
    `is_allowed_enrolled` BIT(1) NOT NULL,
    `is_allowed_graduation` BIT(1) NOT NULL,
    `is_allowed_leave_of_absence` BIT(1) NOT NULL,
    `is_closed` BIT(1) NOT NULL,
    `is_deleted` BIT(1) NOT NULL,
    `is_need_council_fee_paid` BIT(1) NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `circle_id` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `circle_id_index` (`circle_id` ASC) VISIBLE,
    CONSTRAINT `FKsihc207xm6hj4jd97lvyixgvg`
    FOREIGN KEY (`circle_id`)
    REFERENCES `tb_circle` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_post`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_post` (
                                         `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `content` TEXT NOT NULL,
    `is_anonymous` BIT(1) NOT NULL DEFAULT b'0',
    `is_deleted` BIT(1) NULL DEFAULT b'0',
    `is_question` BIT(1) NOT NULL DEFAULT b'0',
    `title` VARCHAR(255) NOT NULL,
    `board_id` VARCHAR(255) NOT NULL,
    `form_id` VARCHAR(255) NULL DEFAULT NULL,
    `vote_id` VARCHAR(255) NULL DEFAULT NULL,
    `user_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_enof2bygn1xgrpr4mwie0w2j` (`form_id` ASC) VISIBLE,
    UNIQUE INDEX `UK_l5mhtqwocn13ic8vaiqor2rv1` (`vote_id` ASC) VISIBLE,
    INDEX `board_id_index` (`board_id` ASC) VISIBLE,
    INDEX `user_id_index` (`user_id` ASC) VISIBLE,
    INDEX `form_id_index` (`form_id` ASC) VISIBLE,
    CONSTRAINT `FK6x1w92hs1xh6y8o5vyql9sau4`
    FOREIGN KEY (`vote_id`)
    REFERENCES `tb_vote` (`id`),
    CONSTRAINT `FKbb41srhc79p2dk7ok6b8w7p3s`
    FOREIGN KEY (`form_id`)
    REFERENCES `tb_form` (`id`),
    CONSTRAINT `FKhx7a7k3pf66vpddqg5pr12anw`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`),
    CONSTRAINT `FKsn6tvkjtynqrfxsooaojns5uu`
    FOREIGN KEY (`board_id`)
    REFERENCES `tb_board` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_comment`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_comment` (
                                            `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `content` VARCHAR(255) NOT NULL,
    `is_anonymous` BIT(1) NOT NULL DEFAULT b'0',
    `is_deleted` BIT(1) NULL DEFAULT b'0',
    `post_id` VARCHAR(255) NOT NULL,
    `user_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `FKebak8c8m45519djplq0wanuj3` (`post_id` ASC) VISIBLE,
    INDEX `FK45c1cuqlljd60ihc9j0962ekq` (`user_id` ASC) VISIBLE,
    CONSTRAINT `FK45c1cuqlljd60ihc9j0962ekq`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`),
    CONSTRAINT `FKebak8c8m45519djplq0wanuj3`
    FOREIGN KEY (`post_id`)
    REFERENCES `tb_post` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_child_comment`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_child_comment` (
                                                  `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `content` VARCHAR(255) NOT NULL,
    `is_anonymous` BIT(1) NOT NULL DEFAULT b'0',
    `is_deleted` BIT(1) NULL DEFAULT b'0',
    `tag_user_name` VARCHAR(255) NULL DEFAULT NULL,
    `parent_comment_id` VARCHAR(255) NOT NULL,
    `user_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `FKss4sg42rfphnmx0vdx1w98y4k` (`parent_comment_id` ASC) VISIBLE,
    INDEX `FKj32wiafix4hn1gg1u5t8h5n56` (`user_id` ASC) VISIBLE,
    CONSTRAINT `FKj32wiafix4hn1gg1u5t8h5n56`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`),
    CONSTRAINT `FKss4sg42rfphnmx0vdx1w98y4k`
    FOREIGN KEY (`parent_comment_id`)
    REFERENCES `tb_comment` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_circle_main_image_uuid_file`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_circle_main_image_uuid_file` (
                                                                `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `circle_id` VARCHAR(255) NOT NULL,
    `uuid_file_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_qcir89lvmcchytkp3wrrb8a2j` (`circle_id` ASC) VISIBLE,
    UNIQUE INDEX `UK_jkn26qkltriq62ncsjmhxw4b4` (`uuid_file_id` ASC) VISIBLE,
    INDEX `idx_circle_main_image_circle_id` (`circle_id` ASC) VISIBLE,
    INDEX `idx_circle_main_image_uuid_file_id` (`uuid_file_id` ASC) VISIBLE,
    CONSTRAINT `FK1oeptre6x44vl1t6xntwa21ml`
    FOREIGN KEY (`circle_id`)
    REFERENCES `tb_circle` (`id`),
    CONSTRAINT `FKt1pk1d2ndfpb72k6klp8mip9j`
    FOREIGN KEY (`uuid_file_id`)
    REFERENCES `tb_uuid_file` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_reply`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_reply` (
                                          `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `form_id` VARCHAR(255) NOT NULL,
    `user_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `FKbo9i5g84h1g2l027f5hw3jlxx` (`form_id` ASC) VISIBLE,
    INDEX `FKkjv6in4jhfr484po2uqetymy9` (`user_id` ASC) VISIBLE,
    CONSTRAINT `FKbo9i5g84h1g2l027f5hw3jlxx`
    FOREIGN KEY (`form_id`)
    REFERENCES `tb_form` (`id`),
    CONSTRAINT `FKkjv6in4jhfr484po2uqetymy9`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_circle_member`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_circle_member` (
                                                  `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `status` ENUM('AWAIT', 'MEMBER', 'LEAVE', 'DROP', 'REJECT') NOT NULL,
    `form_id` VARCHAR(255) NULL DEFAULT NULL,
    `reply_id` VARCHAR(255) NULL DEFAULT NULL,
    `circle_id` VARCHAR(255) NOT NULL,
    `user_id` VARCHAR(255) NOT NULL,
    `user_circle_id` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_d2iivsfp5em6yjwl34r0ra2b2` (`form_id` ASC) VISIBLE,
    UNIQUE INDEX `UK_44wac575y755n0h69u0jt59ns` (`reply_id` ASC) VISIBLE,
    INDEX `FKawtp1h56vah8jiddihqpgx5rd` (`circle_id` ASC) VISIBLE,
    INDEX `FKdij2o1r9y352dl3vhkqt79sww` (`user_id` ASC) VISIBLE,
    INDEX `FKrt9v3qda9jgpjlnbwstaxwp0k` (`user_circle_id` ASC) VISIBLE,
    CONSTRAINT `FKawtp1h56vah8jiddihqpgx5rd`
    FOREIGN KEY (`circle_id`)
    REFERENCES `tb_circle` (`id`),
    CONSTRAINT `FKdij2o1r9y352dl3vhkqt79sww`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`),
    CONSTRAINT `FKlswm7q7lcpvtteye02ck2vnn4`
    FOREIGN KEY (`form_id`)
    REFERENCES `tb_form` (`id`),
    CONSTRAINT `FKrmrumovgl9vrx9yf0cbjxg2m`
    FOREIGN KEY (`reply_id`)
    REFERENCES `tb_reply` (`id`),
    CONSTRAINT `FKrt9v3qda9jgpjlnbwstaxwp0k`
    FOREIGN KEY (`user_circle_id`)
    REFERENCES `tb_user` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_council_fee_fake_user`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_council_fee_fake_user` (
                                                          `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `academic_status` ENUM('ENROLLED', 'LEAVE_OF_ABSENCE', 'GRADUATED', 'DROPPED_OUT', 'SUSPEND', 'EXPEL', 'PROFESSOR', 'UNDETERMINED') NOT NULL,
    `admission_year` INT NOT NULL,
    `current_completed_semester` INT NULL DEFAULT NULL,
    `graduation_type` ENUM('FEBRUARY', 'AUGUST') NULL DEFAULT NULL,
    `graduation_year` INT NULL DEFAULT NULL,
    `major` VARCHAR(255) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `phone_number` VARCHAR(255) NOT NULL,
    `student_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_crawled_notice`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_crawled_notice` (
                                                   `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `announce_date` DATE NOT NULL,
    `author` VARCHAR(255) NOT NULL,
    `content` TEXT NOT NULL,
    `image_link` VARCHAR(255) NULL DEFAULT NULL,
    `link` VARCHAR(255) NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `type` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_5jsgsfwrca8bepsku2i3rjosl` (`link` ASC) VISIBLE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_crawled_file_link`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_crawled_file_link` (
                                                      `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `file_link` VARCHAR(255) NOT NULL,
    `file_name` VARCHAR(255) NOT NULL,
    `crawled_notice_id` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `FK3gwbq5o1qaosqlaimu5ci73by` (`crawled_notice_id` ASC) VISIBLE,
    CONSTRAINT `FK3gwbq5o1qaosqlaimu5ci73by`
    FOREIGN KEY (`crawled_notice_id`)
    REFERENCES `tb_crawled_notice` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_event`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_event` (
                                          `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `is_deleted` BIT(1) NULL DEFAULT b'0',
    `url` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_event_attach_image_uuid_file`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_event_attach_image_uuid_file` (
                                                                 `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `event_id` VARCHAR(255) NOT NULL,
    `uuid_file_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_7a78e300gosbeltnmmwu0h0me` (`event_id` ASC) VISIBLE,
    UNIQUE INDEX `UK_3uqg1t4dny9705he4cybg1wlq` (`uuid_file_id` ASC) VISIBLE,
    INDEX `idx_event_attach_image_event_id` (`event_id` ASC) VISIBLE,
    INDEX `idx_event_attach_image_uuid_file_id` (`uuid_file_id` ASC) VISIBLE,
    CONSTRAINT `FKestsb8gvkr95ddt65luku96n6`
    FOREIGN KEY (`event_id`)
    REFERENCES `tb_event` (`id`),
    CONSTRAINT `FKploeaqbyn6lhkll4pge14us7d`
    FOREIGN KEY (`uuid_file_id`)
    REFERENCES `tb_uuid_file` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_favorite_board`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_favorite_board` (
                                                   `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `board_id` VARCHAR(255) NOT NULL,
    `user_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_h1hd4vvc3s6wuleh5p8bbw95b` (`board_id` ASC) VISIBLE,
    UNIQUE INDEX `UK_93xmc4i6k8aupicijs54boo7k` (`user_id` ASC) VISIBLE,
    CONSTRAINT `FK779ypp0lsdtctv2hovwi9whxj`
    FOREIGN KEY (`board_id`)
    REFERENCES `tb_board` (`id`),
    CONSTRAINT `FKti6ydu5ij1enqjndfsa3eavqu`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_favorite_post`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_favorite_post` (
                                                  `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `is_deleted` BIT(1) NULL DEFAULT b'0',
    `post_id` VARCHAR(255) NULL DEFAULT NULL,
    `user_id` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `FKqavkdq0v03v53woarf9k8jb3e` (`post_id` ASC) VISIBLE,
    INDEX `FKq8b00gpymgb124dqmlt5vpf39` (`user_id` ASC) VISIBLE,
    CONSTRAINT `FKq8b00gpymgb124dqmlt5vpf39`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`),
    CONSTRAINT `FKqavkdq0v03v53woarf9k8jb3e`
    FOREIGN KEY (`post_id`)
    REFERENCES `tb_post` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_flag`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_flag` (
                                         `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `tb_key` VARCHAR(255) NOT NULL,
    `value` BIT(1) NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_9mxdy90sn6d8r6ri8dfudruhr` (`tb_key` ASC) VISIBLE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_form_question`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_form_question` (
                                                  `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `is_multiple` BIT(1) NOT NULL,
    `number` INT NOT NULL,
    `question_text` VARCHAR(255) NOT NULL,
    `question_type` ENUM('SUBJECTIVE', 'OBJECTIVE') NOT NULL,
    `form_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `form_id_index` (`form_id` ASC) VISIBLE,
    CONSTRAINT `FKka1hcmtgi37phx060r96uuniq`
    FOREIGN KEY (`form_id`)
    REFERENCES `tb_form` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_form_question_option`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_form_question_option` (
                                                         `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `number` INT NOT NULL,
    `option_text` VARCHAR(255) NOT NULL,
    `form_question_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `form_question_id_index` (`form_question_id` ASC) VISIBLE,
    CONSTRAINT `FKkv1jic6yn7rrr2mrlluilwj9n`
    FOREIGN KEY (`form_question_id`)
    REFERENCES `tb_form_question` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_inquiry`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_inquiry` (
                                            `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `content` TEXT NOT NULL,
    `is_deleted` BIT(1) NULL DEFAULT b'0',
    `title` VARCHAR(255) NOT NULL,
    `user_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `FKlkre2tncjdw7t8mq5x9onalme` (`user_id` ASC) VISIBLE,
    CONSTRAINT `FKlkre2tncjdw7t8mq5x9onalme`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_latest_crawl`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_latest_crawl` (
                                                 `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `crawl_category` ENUM('CAU_SW_NOTICE', 'CAU_PORTAL_NOTICE') NOT NULL,
    `latest_url` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_like_child_comment`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_like_child_comment` (
                                                       `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `child_comment_id` VARCHAR(255) NULL DEFAULT NULL,
    `user_id` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `FKpcfrmcqr6j5p96w4o6ekg5v84` (`child_comment_id` ASC) VISIBLE,
    INDEX `FK917vru5uo96cg0cppqolnm218` (`user_id` ASC) VISIBLE,
    CONSTRAINT `FK917vru5uo96cg0cppqolnm218`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`),
    CONSTRAINT `FKpcfrmcqr6j5p96w4o6ekg5v84`
    FOREIGN KEY (`child_comment_id`)
    REFERENCES `tb_child_comment` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_like_comment`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_like_comment` (
                                                 `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `comment_id` VARCHAR(255) NULL DEFAULT NULL,
    `user_id` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `FKf0cu0ei1kpmat8xvl7rwm1urv` (`comment_id` ASC) VISIBLE,
    INDEX `FK32g6rm8jtugo3ve18bss452ek` (`user_id` ASC) VISIBLE,
    CONSTRAINT `FK32g6rm8jtugo3ve18bss452ek`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`),
    CONSTRAINT `FKf0cu0ei1kpmat8xvl7rwm1urv`
    FOREIGN KEY (`comment_id`)
    REFERENCES `tb_comment` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_like_post`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_like_post` (
                                              `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `post_id` VARCHAR(255) NULL DEFAULT NULL,
    `user_id` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `FK73pv6iorxfcdvctu0mpg206x8` (`post_id` ASC) VISIBLE,
    INDEX `FK713sod6n69tcbtxpeq1pqpds5` (`user_id` ASC) VISIBLE,
    CONSTRAINT `FK713sod6n69tcbtxpeq1pqpds5`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`),
    CONSTRAINT `FK73pv6iorxfcdvctu0mpg206x8`
    FOREIGN KEY (`post_id`)
    REFERENCES `tb_post` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_locker_log`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_locker_log` (
                                               `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `action` ENUM('ENABLE', 'DISABLE', 'REGISTER', 'RETURN', 'EXTEND') NOT NULL,
    `locker_location_name` VARCHAR(255) NULL DEFAULT NULL,
    `locker_number` BIGINT NOT NULL,
    `message` VARCHAR(255) NULL DEFAULT NULL,
    `user_email` VARCHAR(255) NULL DEFAULT NULL,
    `user_name` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_notification`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_notification` (
                                                 `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `content` VARCHAR(255) NULL DEFAULT NULL,
    `is_global` BIT(1) NULL DEFAULT b'0',
    `notice_type` ENUM('POST', 'COMMENT', 'CEREMONY', 'BOARD') NULL DEFAULT NULL,
    `user_id` VARCHAR(255) NULL DEFAULT NULL,
    `body` VARCHAR(255) NULL DEFAULT NULL,
    `title` VARCHAR(255) NULL DEFAULT NULL,
    `target_id` VARCHAR(255) NULL DEFAULT NULL,
    `target_parent_id` VARCHAR(255) NULL DEFAULT NULL, # 임시 조치 (추후 삭제)
    PRIMARY KEY (`id`),
    INDEX `FK9ihj3k9lv33u6qd10wq18f482` (`user_id` ASC) VISIBLE,
    CONSTRAINT `FK9ihj3k9lv33u6qd10wq18f482`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_notification_log`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_notification_log` (
                                                     `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `is_read` BIT(1) NOT NULL,
    `notification_id` VARCHAR(255) NOT NULL,
    `user_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `FKnm0qj8np5hpys0e9rfe4jlu3b` (`notification_id` ASC) VISIBLE,
    INDEX `FKl2q98bs3s4gdkf77nfntwbgyk` (`user_id` ASC) VISIBLE,
    CONSTRAINT `FKl2q98bs3s4gdkf77nfntwbgyk`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`),
    CONSTRAINT `FKnm0qj8np5hpys0e9rfe4jlu3b`
    FOREIGN KEY (`notification_id`)
    REFERENCES `tb_notification` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_post_attach_image_uuid_file`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_post_attach_image_uuid_file` (
                                                                `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `post_id` VARCHAR(255) NOT NULL,
    `uuid_file_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_dec6rw0gbb32hu2bgvoygrhql` (`uuid_file_id` ASC) VISIBLE,
    INDEX `idx_post_attach_image_post_id` (`post_id` ASC) VISIBLE,
    INDEX `idx_post_attach_image_uuid_file_id` (`uuid_file_id` ASC) VISIBLE,
    CONSTRAINT `FK791svsqhdgfcl2f5y1fp98fgn`
    FOREIGN KEY (`uuid_file_id`)
    REFERENCES `tb_uuid_file` (`id`),
    CONSTRAINT `FKj0wonhfu05hwn6p38any0mycy`
    FOREIGN KEY (`post_id`)
    REFERENCES `tb_post` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_reply_question`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_reply_question` (
                                                   `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `question_answer` TEXT NULL DEFAULT NULL,
    `selected_option_list` VARCHAR(255) NULL DEFAULT NULL,
    `form_question_id` VARCHAR(255) NOT NULL,
    `reply_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `reply_id_index` (`reply_id` ASC) VISIBLE,
    INDEX `form_question_id_index` (`form_question_id` ASC) VISIBLE,
    CONSTRAINT `FKaaw5kmewbcglmurc2d15ulu4l`
    FOREIGN KEY (`reply_id`)
    REFERENCES `tb_reply` (`id`),
    CONSTRAINT `FKngdilacqipy027g6fgup9fw6r`
    FOREIGN KEY (`form_question_id`)
    REFERENCES `tb_form_question` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_semester`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_semester` (
                                             `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `end_date` DATE NOT NULL,
    `is_current` BIT(1) NOT NULL,
    `semester_type` TINYINT NOT NULL,
    `semester_year` INT NOT NULL,
    `start_date` DATE NOT NULL,
    `update_user_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `FK1f7qdbcou7kki27na1yhgn4a5` (`update_user_id` ASC) VISIBLE,
    CONSTRAINT `FK1f7qdbcou7kki27na1yhgn4a5`
    FOREIGN KEY (`update_user_id`)
    REFERENCES `tb_user` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_text_field`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_text_field` (
                                               `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `tb_key` VARCHAR(255) NOT NULL,
    `value` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_1jym6b281dx706drcf9gbc8iw` (`tb_key` ASC) VISIBLE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_user_academic_record_application`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_user_academic_record_application` (
                                                                     `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `academic_record_request_status` ENUM('ACCEPT', 'REJECT', 'AWAIT', 'CLOSE') NOT NULL,
    `note` VARCHAR(255) NULL DEFAULT NULL,
    `reject_message` VARCHAR(255) NULL DEFAULT NULL,
    `target_academic_status` ENUM('ENROLLED', 'LEAVE_OF_ABSENCE', 'GRADUATED', 'DROPPED_OUT', 'SUSPEND', 'EXPEL', 'PROFESSOR', 'UNDETERMINED') NOT NULL,
    `target_completed_semester` INT NULL DEFAULT NULL,
    `user_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `user_id_index` (`user_id` ASC) VISIBLE,
    CONSTRAINT `FK6o42vtjrql4spa3qheorq57e7`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_user_academic_record_application_attach_image_uuid_file`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_user_academic_record_application_attach_image_uuid_file` (
                                                                                            `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `user_academic_record_application_id` VARCHAR(255) NOT NULL,
    `uuid_file_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_m5oic5hwkfi8a67wp49s3qdn0` (`uuid_file_id` ASC) VISIBLE,
    INDEX `idx_user_academic_record_application_attach_image_application_id` (`user_academic_record_application_id` ASC) VISIBLE,
    INDEX `idx_user_academic_record_application_attach_image_uuid_file_id` (`uuid_file_id` ASC) VISIBLE,
    CONSTRAINT `FKepr9k9enk156gcxua0nvv8ldg`
    FOREIGN KEY (`user_academic_record_application_id`)
    REFERENCES `tb_user_academic_record_application` (`id`),
    CONSTRAINT `FKs0kth7tg0drbg2a74rhdy67ck`
    FOREIGN KEY (`uuid_file_id`)
    REFERENCES `tb_uuid_file` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_user_academic_record_log`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_user_academic_record_log` (
                                                             `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `controlled_user_email` VARCHAR(255) NOT NULL,
    `controlled_user_name` VARCHAR(255) NOT NULL,
    `controlled_user_student_id` VARCHAR(255) NOT NULL,
    `graduation_type` ENUM('FEBRUARY', 'AUGUST') NULL DEFAULT NULL,
    `graduation_year` INT NULL DEFAULT NULL,
    `note` VARCHAR(255) NULL DEFAULT NULL,
    `reject_message` VARCHAR(255) NULL DEFAULT NULL,
    `target_academic_record_request_status` ENUM('ACCEPT', 'REJECT', 'AWAIT', 'CLOSE') NULL DEFAULT NULL,
    `prior_academic_record_application_id` ENUM('ENROLLED', 'LEAVE_OF_ABSENCE', 'GRADUATED', 'DROPPED_OUT', 'SUSPEND', 'EXPEL', 'PROFESSOR', 'UNDETERMINED') NOT NULL,
    `target_user_email` VARCHAR(255) NOT NULL,
    `target_user_name` VARCHAR(255) NOT NULL,
    `target_user_student_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_user_academic_record_log_attach_image`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_user_academic_record_log_attach_image` (
                                                                          `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `user_academic_record_log_id` VARCHAR(255) NOT NULL,
    `uuid_file_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_user_academic_record_log_attach_image_log_id` (`user_academic_record_log_id` ASC) VISIBLE,
    INDEX `idx_user_academic_record_log_attach_image_uuid_file_id` (`uuid_file_id` ASC) VISIBLE,
    CONSTRAINT `FKiiknx3qc1s4sc16fokab160nj`
    FOREIGN KEY (`user_academic_record_log_id`)
    REFERENCES `tb_user_academic_record_log` (`id`),
    CONSTRAINT `FKn6qscetye4wllnxsjwx2ault1`
    FOREIGN KEY (`uuid_file_id`)
    REFERENCES `tb_uuid_file` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_user_admission`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_user_admission` (
                                                   `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `description` VARCHAR(255) NULL DEFAULT NULL,
    `user_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_5c0j7cyx9b1awc3y5v8m8t25e` (`user_id` ASC) VISIBLE,
    CONSTRAINT `FKdkcgsxmu3ph4cddd7gydgfx3p`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_user_admission_attach_image_uuid_file`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_user_admission_attach_image_uuid_file` (
                                                                          `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `user_admission_id` VARCHAR(255) NOT NULL,
    `uuid_file_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_8yfr48oo34p9gkbl0ro62dxcx` (`uuid_file_id` ASC) VISIBLE,
    INDEX `idx_user_admission_attach_image__admission_id` (`user_admission_id` ASC) VISIBLE,
    INDEX `idx_user_admission_attach_image_uuid_file_id` (`uuid_file_id` ASC) VISIBLE,
    CONSTRAINT `FK8kr6tasvi8kyw0m7vlj1oab9i`
    FOREIGN KEY (`uuid_file_id`)
    REFERENCES `tb_uuid_file` (`id`),
    CONSTRAINT `FKnemwteegkkpge96155ubd3ss8`
    FOREIGN KEY (`user_admission_id`)
    REFERENCES `tb_user_admission` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_user_admission_log`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_user_admission_log` (
                                                       `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `action` ENUM('ACCEPT', 'REJECT') NOT NULL,
    `admin_user_email` VARCHAR(255) NOT NULL,
    `admin_user_name` VARCHAR(255) NOT NULL,
    `description` VARCHAR(255) NULL DEFAULT NULL,
    `reject_reason` VARCHAR(255) NULL DEFAULT NULL,
    `user_email` VARCHAR(255) NOT NULL,
    `user_name` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_user_admission_log_attach_image_uuid_file`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_user_admission_log_attach_image_uuid_file` (
                                                                              `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `user_admission_log_id` VARCHAR(255) NOT NULL,
    `uuid_file_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_q92p8k87vpukt4uxceu54t98l` (`uuid_file_id` ASC) VISIBLE,
    INDEX `idx_user_admission_log_attach_image_log_id` (`user_admission_log_id` ASC) VISIBLE,
    INDEX `idx_user_admission_log_attach_image_uuid_file_id` (`uuid_file_id` ASC) VISIBLE,
    CONSTRAINT `FK31g16rte8nvuns6lcd8dtquig`
    FOREIGN KEY (`uuid_file_id`)
    REFERENCES `tb_uuid_file` (`id`),
    CONSTRAINT `FKg8j9u8vgj2vswq0hltfhvbsm9`
    FOREIGN KEY (`user_admission_log_id`)
    REFERENCES `tb_user_admission_log` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_user_board_subscribe`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_user_board_subscribe` (
                                                         `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `is_subscribed` BIT(1) NULL DEFAULT NULL,
    `board_id` VARCHAR(255) NULL DEFAULT NULL,
    `user_id` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `FKm3w9ve1mqdvu5yya43lgktqka` (`board_id` ASC) VISIBLE,
    INDEX `FKps9i1rpnulrnluj7m6fl212x` (`user_id` ASC) VISIBLE,
    CONSTRAINT `FKm3w9ve1mqdvu5yya43lgktqka`
    FOREIGN KEY (`board_id`)
    REFERENCES `tb_board` (`id`),
    CONSTRAINT `FKps9i1rpnulrnluj7m6fl212x`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_user_comment_subscribe`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_user_comment_subscribe` (
                                                           `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `is_subscribed` BIT(1) NULL DEFAULT NULL,
    `comment_id` VARCHAR(255) NULL DEFAULT NULL,
    `user_id` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `FKsmjykxko0wkguyduujxljy9p1` (`comment_id` ASC) VISIBLE,
    INDEX `FKhtvmhbqf9n7whwlkpvakadg1x` (`user_id` ASC) VISIBLE,
    CONSTRAINT `FKhtvmhbqf9n7whwlkpvakadg1x`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`),
    CONSTRAINT `FKsmjykxko0wkguyduujxljy9p1`
    FOREIGN KEY (`comment_id`)
    REFERENCES `tb_comment` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_user_council_fee`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_user_council_fee` (
                                                     `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `is_joined_service` BIT(1) NOT NULL,
    `is_refunded` BIT(1) NOT NULL,
    `num_of_paid_semester` INT NOT NULL,
    `paid_at` INT NOT NULL,
    `refunded_at` INT NULL DEFAULT NULL,
    `council_fee_fake_user_id` VARCHAR(255) NULL DEFAULT NULL,
    `user_id` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_gnokg5xtbxsb61hft4a2g8eng` (`council_fee_fake_user_id` ASC) VISIBLE,
    UNIQUE INDEX `UK_4hbj83xu0pmbq16nmj3q9kk3a` (`user_id` ASC) VISIBLE,
    CONSTRAINT `FK54eg4kykxfyqe461th83o012d`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`),
    CONSTRAINT `FKa2vx6xctboiyveok54n93qfr7`
    FOREIGN KEY (`council_fee_fake_user_id`)
    REFERENCES `tb_council_fee_fake_user` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_user_council_fee_log`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_user_council_fee_log` (
                                                         `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `academic_status` ENUM('ENROLLED', 'LEAVE_OF_ABSENCE', 'GRADUATED', 'DROPPED_OUT', 'SUSPEND', 'EXPEL', 'PROFESSOR', 'UNDETERMINED') NOT NULL,
    `admission_year` INT NOT NULL,
    `controlled_user_email` VARCHAR(255) NOT NULL,
    `controlled_user_name` VARCHAR(255) NOT NULL,
    `controlled_user_student_id` VARCHAR(255) NOT NULL,
    `council_fee_log_type` ENUM('CREATE', 'UPDATE', 'DELETE') NOT NULL,
    `current_completed_semester` INT NOT NULL,
    `email` VARCHAR(255) NULL DEFAULT NULL,
    `graduation_type` ENUM('FEBRUARY', 'AUGUST') NULL DEFAULT NULL,
    `graduation_year` INT NULL DEFAULT NULL,
    `is_applied_this_semester` BIT(1) NOT NULL,
    `joined_at` DATE NULL DEFAULT NULL,
    `major` VARCHAR(255) NOT NULL,
    `phone_number` VARCHAR(255) NOT NULL,
    `rest_of_semester` INT NOT NULL,
    `student_id` VARCHAR(255) NOT NULL,
    `target_is_joined_service` BIT(1) NOT NULL,
    `target_is_refunded` BIT(1) NOT NULL,
    `target_num_of_paid_semester` INT NOT NULL,
    `target_paid_at` INT NOT NULL,
    `target_refunded_at` INT NULL DEFAULT NULL,
    `time_of_semester_type` ENUM('FIRST', 'SECOND', 'SUMMER', 'WINTER') NOT NULL,
    `time_of_semester_year` INT NOT NULL,
    `user_name` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_user_fcm_token`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_user_fcm_token` (
                                                   `user_id` VARCHAR(255) NOT NULL,
    `fcm_token` VARCHAR(255) NULL DEFAULT NULL,
    `fcm_token_value` VARCHAR(255) NULL DEFAULT NULL,
    INDEX `FK67eb8vcoda8mx5odsy07fc1fh` (`user_id` ASC) VISIBLE,
    CONSTRAINT `FK67eb8vcoda8mx5odsy07fc1fh`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_user_post_subscribe`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_user_post_subscribe` (
                                                        `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `is_subscribed` BIT(1) NULL DEFAULT NULL,
    `post_id` VARCHAR(255) NULL DEFAULT NULL,
    `user_id` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `FK7sqkcrntbyp1x61eqmio1bp1e` (`post_id` ASC) VISIBLE,
    INDEX `FKlny56nrqcq87bftis6i91x2y7` (`user_id` ASC) VISIBLE,
    CONSTRAINT `FK7sqkcrntbyp1x61eqmio1bp1e`
    FOREIGN KEY (`post_id`)
    REFERENCES `tb_post` (`id`),
    CONSTRAINT `FKlny56nrqcq87bftis6i91x2y7`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_user_profile_uuid_file`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_user_profile_uuid_file` (
                                                           `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `user_id` VARCHAR(255) NOT NULL,
    `uuid_file_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_pp99y4eo5m8f7k0o6lecshsps` (`user_id` ASC) VISIBLE,
    UNIQUE INDEX `UK_ioi0qbw6h8fw1vqkssyxt8x7u` (`uuid_file_id` ASC) VISIBLE,
    INDEX `idx_user_profile_user_id` (`user_id` ASC) VISIBLE,
    INDEX `idx_user_profile_uuid_file_id` (`uuid_file_id` ASC) VISIBLE,
    CONSTRAINT `FK6p29j8ic8e13vonmldawkoq67`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`),
    CONSTRAINT `FKkciuncg6fgjygqctb69iyh0nt`
    FOREIGN KEY (`uuid_file_id`)
    REFERENCES `tb_uuid_file` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_vote_option`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_vote_option` (
                                                `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `option_name` VARCHAR(255) NULL DEFAULT NULL,
    `vote_id` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `FKnni9i6g97ug9egsfw6n01titi` (`vote_id` ASC) VISIBLE,
    CONSTRAINT `FKnni9i6g97ug9egsfw6n01titi`
    FOREIGN KEY (`vote_id`)
    REFERENCES `tb_vote` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `tb_vote_record`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_vote_record` (
                                                `id` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NULL DEFAULT NULL,
    `updated_at` DATETIME(6) NULL DEFAULT NULL,
    `user_id` VARCHAR(255) NULL DEFAULT NULL,
    `vote_option_id` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `FKj1rlo6wtl6qibjq8an1qj0ucl` (`user_id` ASC) VISIBLE,
    INDEX `FK6nwktkmc5plj4grvt2999fxcg` (`vote_option_id` ASC) VISIBLE,
    CONSTRAINT `FK6nwktkmc5plj4grvt2999fxcg`
    FOREIGN KEY (`vote_option_id`)
    REFERENCES `tb_vote_option` (`id`),
    CONSTRAINT `FKj1rlo6wtl6qibjq8an1qj0ucl`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `user_roles`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_roles` (
                                            `user_id` VARCHAR(255) NOT NULL,
    `role` ENUM('ADMIN', 'PRESIDENT', 'VICE_PRESIDENT', 'COUNCIL', 'LEADER_1', 'LEADER_2', 'LEADER_3', 'LEADER_4', 'LEADER_CIRCLE', 'LEADER_ALUMNI', 'COMMON', 'NONE', 'PROFESSOR') NOT NULL,
    PRIMARY KEY (`user_id`, `role`),
    CONSTRAINT `FKlqb868dhpatxi3e1m1nu3ukr5`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;