-- MySQL dump 10.13  Distrib 8.0.42, for macos15.2 (arm64)
--
-- Host: db-caucse-prod.cxmgm6i6yjq2.ap-northeast-2.rds.amazonaws.com    Database: db_caucse_prod_v2
-- ------------------------------------------------------
-- Server version	8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `BATCH_JOB_EXECUTION`
--

DROP TABLE IF EXISTS `BATCH_JOB_EXECUTION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `BATCH_JOB_EXECUTION` (
                                       `JOB_EXECUTION_ID` bigint NOT NULL,
                                       `VERSION` bigint DEFAULT NULL,
                                       `JOB_INSTANCE_ID` bigint NOT NULL,
                                       `CREATE_TIME` datetime(6) NOT NULL,
                                       `START_TIME` datetime(6) DEFAULT NULL,
                                       `END_TIME` datetime(6) DEFAULT NULL,
                                       `STATUS` varchar(10) DEFAULT NULL,
                                       `EXIT_CODE` varchar(2500) DEFAULT NULL,
                                       `EXIT_MESSAGE` varchar(2500) DEFAULT NULL,
                                       `LAST_UPDATED` datetime(6) DEFAULT NULL,
                                       PRIMARY KEY (`JOB_EXECUTION_ID`),
                                       KEY `JOB_INST_EXEC_FK` (`JOB_INSTANCE_ID`),
                                       CONSTRAINT `JOB_INST_EXEC_FK` FOREIGN KEY (`JOB_INSTANCE_ID`) REFERENCES `BATCH_JOB_INSTANCE` (`JOB_INSTANCE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `BATCH_JOB_EXECUTION_CONTEXT`
--

DROP TABLE IF EXISTS `BATCH_JOB_EXECUTION_CONTEXT`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `BATCH_JOB_EXECUTION_CONTEXT` (
                                               `JOB_EXECUTION_ID` bigint NOT NULL,
                                               `SHORT_CONTEXT` varchar(2500) NOT NULL,
                                               `SERIALIZED_CONTEXT` text,
                                               PRIMARY KEY (`JOB_EXECUTION_ID`),
                                               CONSTRAINT `JOB_EXEC_CTX_FK` FOREIGN KEY (`JOB_EXECUTION_ID`) REFERENCES `BATCH_JOB_EXECUTION` (`JOB_EXECUTION_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `BATCH_JOB_EXECUTION_PARAMS`
--

DROP TABLE IF EXISTS `BATCH_JOB_EXECUTION_PARAMS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `BATCH_JOB_EXECUTION_PARAMS` (
                                              `JOB_EXECUTION_ID` bigint NOT NULL,
                                              `PARAMETER_NAME` varchar(100) NOT NULL,
                                              `PARAMETER_TYPE` varchar(100) NOT NULL,
                                              `PARAMETER_VALUE` varchar(2500) DEFAULT NULL,
                                              `IDENTIFYING` char(1) NOT NULL,
                                              KEY `JOB_EXEC_PARAMS_FK` (`JOB_EXECUTION_ID`),
                                              CONSTRAINT `JOB_EXEC_PARAMS_FK` FOREIGN KEY (`JOB_EXECUTION_ID`) REFERENCES `BATCH_JOB_EXECUTION` (`JOB_EXECUTION_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `BATCH_JOB_EXECUTION_SEQ`
--

DROP TABLE IF EXISTS `BATCH_JOB_EXECUTION_SEQ`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `BATCH_JOB_EXECUTION_SEQ` (
                                           `ID` bigint NOT NULL,
                                           `UNIQUE_KEY` char(1) NOT NULL,
                                           UNIQUE KEY `UNIQUE_KEY_UN` (`UNIQUE_KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `BATCH_JOB_INSTANCE`
--

DROP TABLE IF EXISTS `BATCH_JOB_INSTANCE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `BATCH_JOB_INSTANCE` (
                                      `JOB_INSTANCE_ID` bigint NOT NULL,
                                      `VERSION` bigint DEFAULT NULL,
                                      `JOB_NAME` varchar(100) NOT NULL,
                                      `JOB_KEY` varchar(32) NOT NULL,
                                      PRIMARY KEY (`JOB_INSTANCE_ID`),
                                      UNIQUE KEY `JOB_INST_UN` (`JOB_NAME`,`JOB_KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `BATCH_JOB_SEQ`
--

DROP TABLE IF EXISTS `BATCH_JOB_SEQ`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `BATCH_JOB_SEQ` (
                                 `ID` bigint NOT NULL,
                                 `UNIQUE_KEY` char(1) NOT NULL,
                                 UNIQUE KEY `UNIQUE_KEY_UN` (`UNIQUE_KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `BATCH_STEP_EXECUTION`
--

DROP TABLE IF EXISTS `BATCH_STEP_EXECUTION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `BATCH_STEP_EXECUTION` (
                                        `STEP_EXECUTION_ID` bigint NOT NULL,
                                        `VERSION` bigint NOT NULL,
                                        `STEP_NAME` varchar(100) NOT NULL,
                                        `JOB_EXECUTION_ID` bigint NOT NULL,
                                        `CREATE_TIME` datetime(6) NOT NULL,
                                        `START_TIME` datetime(6) DEFAULT NULL,
                                        `END_TIME` datetime(6) DEFAULT NULL,
                                        `STATUS` varchar(10) DEFAULT NULL,
                                        `COMMIT_COUNT` bigint DEFAULT NULL,
                                        `READ_COUNT` bigint DEFAULT NULL,
                                        `FILTER_COUNT` bigint DEFAULT NULL,
                                        `WRITE_COUNT` bigint DEFAULT NULL,
                                        `READ_SKIP_COUNT` bigint DEFAULT NULL,
                                        `WRITE_SKIP_COUNT` bigint DEFAULT NULL,
                                        `PROCESS_SKIP_COUNT` bigint DEFAULT NULL,
                                        `ROLLBACK_COUNT` bigint DEFAULT NULL,
                                        `EXIT_CODE` varchar(2500) DEFAULT NULL,
                                        `EXIT_MESSAGE` varchar(2500) DEFAULT NULL,
                                        `LAST_UPDATED` datetime(6) DEFAULT NULL,
                                        PRIMARY KEY (`STEP_EXECUTION_ID`),
                                        KEY `JOB_EXEC_STEP_FK` (`JOB_EXECUTION_ID`),
                                        CONSTRAINT `JOB_EXEC_STEP_FK` FOREIGN KEY (`JOB_EXECUTION_ID`) REFERENCES `BATCH_JOB_EXECUTION` (`JOB_EXECUTION_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `BATCH_STEP_EXECUTION_CONTEXT`
--

DROP TABLE IF EXISTS `BATCH_STEP_EXECUTION_CONTEXT`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `BATCH_STEP_EXECUTION_CONTEXT` (
                                                `STEP_EXECUTION_ID` bigint NOT NULL,
                                                `SHORT_CONTEXT` varchar(2500) NOT NULL,
                                                `SERIALIZED_CONTEXT` text,
                                                PRIMARY KEY (`STEP_EXECUTION_ID`),
                                                CONSTRAINT `STEP_EXEC_CTX_FK` FOREIGN KEY (`STEP_EXECUTION_ID`) REFERENCES `BATCH_STEP_EXECUTION` (`STEP_EXECUTION_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `BATCH_STEP_EXECUTION_SEQ`
--

DROP TABLE IF EXISTS `BATCH_STEP_EXECUTION_SEQ`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `BATCH_STEP_EXECUTION_SEQ` (
                                            `ID` bigint NOT NULL,
                                            `UNIQUE_KEY` char(1) NOT NULL,
                                            UNIQUE KEY `UNIQUE_KEY_UN` (`UNIQUE_KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ceremony`
--

DROP TABLE IF EXISTS `ceremony`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ceremony` (
                            `id` varchar(255) NOT NULL,
                            `created_at` datetime(6) DEFAULT NULL,
                            `updated_at` datetime(6) DEFAULT NULL,
                            `ceremony_category` enum('MARRIAGE','FUNERAL','ETC') NOT NULL,
                            `ceremony_state` enum('ACCEPT','REJECT','AWAIT','CLOSE') NOT NULL,
                            `description` varchar(255) NOT NULL,
                            `end_date` date NOT NULL,
                            `start_date` date NOT NULL,
                            `user_id` varchar(255) NOT NULL,
                            `note` varchar(255) DEFAULT NULL,
                            PRIMARY KEY (`id`),
                            KEY `FK68hi5kpp95tb3qu6fcfxq7fa7` (`user_id`),
                            CONSTRAINT `FK68hi5kpp95tb3qu6fcfxq7fa7` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
                                         `installed_rank` int NOT NULL,
                                         `version` varchar(50) DEFAULT NULL,
                                         `description` varchar(200) NOT NULL,
                                         `type` varchar(20) NOT NULL,
                                         `script` varchar(1000) NOT NULL,
                                         `checksum` int DEFAULT NULL,
                                         `installed_by` varchar(100) NOT NULL,
                                         `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         `execution_time` int NOT NULL,
                                         `success` tinyint(1) NOT NULL,
                                         PRIMARY KEY (`installed_rank`),
                                         KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_board`
--

DROP TABLE IF EXISTS `tb_board`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_board` (
                            `id` varchar(255) NOT NULL,
                            `created_at` datetime(6) DEFAULT NULL,
                            `updated_at` datetime(6) DEFAULT NULL,
                            `category` varchar(255) NOT NULL,
                            `create_role_list` varchar(255) NOT NULL,
                            `description` varchar(255) DEFAULT NULL,
                            `is_deleted` bit(1) NOT NULL DEFAULT b'0',
                            `name` varchar(255) NOT NULL,
                            `circle_id` varchar(255) DEFAULT NULL,
                            `is_default` bit(1) NOT NULL DEFAULT b'0',
                            `is_default_notice` bit(1) NOT NULL DEFAULT b'0',
                            `is_anonymous_allowed` bit(1) NOT NULL DEFAULT b'0',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `UK_79ufcd1db5uudn8hf4tg00s5t` (`name`),
                            KEY `FKf56nd4y1y3jqyec9a19gl4i43` (`circle_id`),
                            CONSTRAINT `FKf56nd4y1y3jqyec9a19gl4i43` FOREIGN KEY (`circle_id`) REFERENCES `tb_circle` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_board_apply`
--

DROP TABLE IF EXISTS `tb_board_apply`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_board_apply` (
                                  `is_annonymous_allowed` bit(1) NOT NULL DEFAULT b'0',
                                  `created_at` datetime(6) DEFAULT NULL,
                                  `updated_at` datetime(6) DEFAULT NULL,
                                  `board_name` varchar(255) NOT NULL,
                                  `category` varchar(255) NOT NULL,
                                  `create_role_list` varchar(255) NOT NULL,
                                  `description` varchar(255) DEFAULT NULL,
                                  `id` varchar(255) NOT NULL,
                                  `user_id` varchar(255) NOT NULL,
                                  `accept_status` enum('AWAIT','ACCEPTED','REJECT') NOT NULL,
                                  `circle_id` varchar(255) DEFAULT NULL,
                                  PRIMARY KEY (`id`),
                                  KEY `FKde6dcpov54bln9k8pm0eo02wj` (`user_id`),
                                  KEY `FK77llxkuuxhgumf8kjvl5yi9xx` (`circle_id`),
                                  CONSTRAINT `FK77llxkuuxhgumf8kjvl5yi9xx` FOREIGN KEY (`circle_id`) REFERENCES `tb_circle` (`id`),
                                  CONSTRAINT `FKde6dcpov54bln9k8pm0eo02wj` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_calendar`
--

DROP TABLE IF EXISTS `tb_calendar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_calendar` (
                               `month` int NOT NULL,
                               `year` int NOT NULL,
                               `created_at` datetime(6) DEFAULT NULL,
                               `updated_at` datetime(6) DEFAULT NULL,
                               `id` varchar(255) NOT NULL,
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_calendar_attach_image_uuid_file`
--

DROP TABLE IF EXISTS `tb_calendar_attach_image_uuid_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_calendar_attach_image_uuid_file` (
                                                      `created_at` datetime(6) DEFAULT NULL,
                                                      `updated_at` datetime(6) DEFAULT NULL,
                                                      `calendar_id` varchar(255) NOT NULL,
                                                      `id` varchar(255) NOT NULL,
                                                      `uuid_file_id` varchar(255) NOT NULL,
                                                      PRIMARY KEY (`id`),
                                                      UNIQUE KEY `UK_ovufeprpl55xmb2xrw2i8itku` (`calendar_id`),
                                                      UNIQUE KEY `UK_s2koor1hv4d06sf3ui0r5rlte` (`uuid_file_id`),
                                                      KEY `idx_calendar_attach_image_calendar_id` (`calendar_id`),
                                                      KEY `idx_calendar_attach_image_uuid_file_id` (`uuid_file_id`),
                                                      CONSTRAINT `FKdw5exxxgluthyq6ip8jj3vwea` FOREIGN KEY (`calendar_id`) REFERENCES `tb_calendar` (`id`),
                                                      CONSTRAINT `FKmfj3gxbrsl39bow3q5hgmlxhd` FOREIGN KEY (`uuid_file_id`) REFERENCES `tb_uuid_file` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_ceremony_attach_image_uuid_file`
--

DROP TABLE IF EXISTS `tb_ceremony_attach_image_uuid_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_ceremony_attach_image_uuid_file` (
                                                      `id` varchar(255) NOT NULL,
                                                      `created_at` datetime(6) DEFAULT NULL,
                                                      `updated_at` datetime(6) DEFAULT NULL,
                                                      `ceremony_id` varchar(255) NOT NULL,
                                                      `uuid_file_id` varchar(255) NOT NULL,
                                                      PRIMARY KEY (`id`),
                                                      UNIQUE KEY `UK_iqegoxxctvtkjkrm2ge33j26d` (`uuid_file_id`),
                                                      KEY `idx_ceremony_attach_image_ceremony_id` (`ceremony_id`),
                                                      KEY `idx_ceremony_attach_image_uuid_file_id` (`uuid_file_id`),
                                                      CONSTRAINT `FKd9t63v2tqpoa16pe6u1tlspub` FOREIGN KEY (`uuid_file_id`) REFERENCES `tb_uuid_file` (`id`),
                                                      CONSTRAINT `FKpqnnjrm67d4rnlds18tutw542` FOREIGN KEY (`ceremony_id`) REFERENCES `ceremony` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_ceremony_push_notification`
--

DROP TABLE IF EXISTS `tb_ceremony_push_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_ceremony_push_notification` (
                                                 `id` varchar(255) NOT NULL,
                                                 `created_at` datetime(6) DEFAULT NULL,
                                                 `updated_at` datetime(6) DEFAULT NULL,
                                                 `is_push_active` bit(1) NOT NULL,
                                                 `user_id` varchar(255) NOT NULL,
                                                 `is_notification_active` bit(1) NOT NULL,
                                                 `is_set_all` bit(1) NOT NULL,
                                                 PRIMARY KEY (`id`),
                                                 UNIQUE KEY `UK_dc7o4ac8ym4ar2q5t4ld863py` (`user_id`),
                                                 CONSTRAINT `FKndjbnnmuuyj2v8myo498jci87` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_ceremony_subscribe_year`
--

DROP TABLE IF EXISTS `tb_ceremony_subscribe_year`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_ceremony_subscribe_year` (
                                              `notification_id` varchar(255) NOT NULL,
                                              `admission_year` int DEFAULT NULL,
                                              KEY `FK36lyx1t3etyvq6gloftmklrt3` (`notification_id`),
                                              CONSTRAINT `FK36lyx1t3etyvq6gloftmklrt3` FOREIGN KEY (`notification_id`) REFERENCES `tb_ceremony_push_notification` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_child_comment`
--

DROP TABLE IF EXISTS `tb_child_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_child_comment` (
                                    `is_anonymous` bit(1) NOT NULL DEFAULT b'0',
                                    `is_deleted` bit(1) DEFAULT b'0',
                                    `created_at` datetime(6) DEFAULT NULL,
                                    `updated_at` datetime(6) DEFAULT NULL,
                                    `content` varchar(255) NOT NULL,
                                    `id` varchar(255) NOT NULL,
                                    `parent_comment_id` varchar(255) NOT NULL,
                                    `tag_user_name` varchar(255) DEFAULT NULL,
                                    `user_id` varchar(255) NOT NULL,
                                    PRIMARY KEY (`id`),
                                    KEY `FKss4sg42rfphnmx0vdx1w98y4k` (`parent_comment_id`),
                                    KEY `FKj32wiafix4hn1gg1u5t8h5n56` (`user_id`),
                                    CONSTRAINT `FKj32wiafix4hn1gg1u5t8h5n56` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`),
                                    CONSTRAINT `FKss4sg42rfphnmx0vdx1w98y4k` FOREIGN KEY (`parent_comment_id`) REFERENCES `tb_comment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_circle`
--

DROP TABLE IF EXISTS `tb_circle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_circle` (
                             `id` varchar(255) NOT NULL,
                             `created_at` datetime(6) DEFAULT NULL,
                             `updated_at` datetime(6) DEFAULT NULL,
                             `description` varchar(255) DEFAULT NULL,
                             `is_deleted` bit(1) DEFAULT b'0',
                             `name` varchar(255) NOT NULL,
                             `leader_id` varchar(255) DEFAULT NULL,
                             `circle_tax` int DEFAULT NULL,
                             `is_recruit` bit(1) DEFAULT NULL,
                             `recruit_end_date` datetime(6) DEFAULT NULL,
                             `recruit_members` int DEFAULT NULL,
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `UK_faag85xipb292m553obdly74p` (`leader_id`),
                             CONSTRAINT `FKsyy3e5thj8ighbjoa6owk3a37` FOREIGN KEY (`leader_id`) REFERENCES `tb_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_circle_main_image_uuid_file`
--

DROP TABLE IF EXISTS `tb_circle_main_image_uuid_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_circle_main_image_uuid_file` (
                                                  `created_at` datetime(6) DEFAULT NULL,
                                                  `updated_at` datetime(6) DEFAULT NULL,
                                                  `circle_id` varchar(255) NOT NULL,
                                                  `id` varchar(255) NOT NULL,
                                                  `uuid_file_id` varchar(255) NOT NULL,
                                                  PRIMARY KEY (`id`),
                                                  UNIQUE KEY `UK_qcir89lvmcchytkp3wrrb8a2j` (`circle_id`),
                                                  UNIQUE KEY `UK_jkn26qkltriq62ncsjmhxw4b4` (`uuid_file_id`),
                                                  KEY `idx_circle_main_image_circle_id` (`circle_id`),
                                                  KEY `idx_circle_main_image_uuid_file_id` (`uuid_file_id`),
                                                  CONSTRAINT `FK1oeptre6x44vl1t6xntwa21ml` FOREIGN KEY (`circle_id`) REFERENCES `tb_circle` (`id`),
                                                  CONSTRAINT `FKt1pk1d2ndfpb72k6klp8mip9j` FOREIGN KEY (`uuid_file_id`) REFERENCES `tb_uuid_file` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_circle_member`
--

DROP TABLE IF EXISTS `tb_circle_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_circle_member` (
                                    `created_at` datetime(6) DEFAULT NULL,
                                    `updated_at` datetime(6) DEFAULT NULL,
                                    `circle_id` varchar(255) NOT NULL,
                                    `form_id` varchar(255) DEFAULT NULL,
                                    `id` varchar(255) NOT NULL,
                                    `reply_id` varchar(255) DEFAULT NULL,
                                    `user_circle_id` varchar(255) DEFAULT NULL,
                                    `user_id` varchar(255) NOT NULL,
                                    `status` enum('AWAIT','MEMBER','LEAVE','DROP','REJECT') NOT NULL,
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `UK_d2iivsfp5em6yjwl34r0ra2b2` (`form_id`),
                                    UNIQUE KEY `UK_44wac575y755n0h69u0jt59ns` (`reply_id`),
                                    KEY `FKawtp1h56vah8jiddihqpgx5rd` (`circle_id`),
                                    KEY `FKdij2o1r9y352dl3vhkqt79sww` (`user_id`),
                                    KEY `FKrt9v3qda9jgpjlnbwstaxwp0k` (`user_circle_id`),
                                    CONSTRAINT `FKawtp1h56vah8jiddihqpgx5rd` FOREIGN KEY (`circle_id`) REFERENCES `tb_circle` (`id`),
                                    CONSTRAINT `FKdij2o1r9y352dl3vhkqt79sww` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`),
                                    CONSTRAINT `FKlswm7q7lcpvtteye02ck2vnn4` FOREIGN KEY (`form_id`) REFERENCES `tb_form` (`id`),
                                    CONSTRAINT `FKrmrumovgl9vrx9yf0cbjxg2m` FOREIGN KEY (`reply_id`) REFERENCES `tb_reply` (`id`),
                                    CONSTRAINT `FKrt9v3qda9jgpjlnbwstaxwp0k` FOREIGN KEY (`user_circle_id`) REFERENCES `tb_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_comment`
--

DROP TABLE IF EXISTS `tb_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_comment` (
                              `id` varchar(255) NOT NULL,
                              `created_at` datetime(6) DEFAULT NULL,
                              `updated_at` datetime(6) DEFAULT NULL,
                              `content` varchar(255) NOT NULL,
                              `is_deleted` bit(1) DEFAULT b'0',
                              `post_id` varchar(255) NOT NULL,
                              `user_id` varchar(255) NOT NULL,
                              `is_anonymous` bit(1) NOT NULL DEFAULT b'0',
                              PRIMARY KEY (`id`),
                              KEY `FKebak8c8m45519djplq0wanuj3` (`post_id`),
                              KEY `FK45c1cuqlljd60ihc9j0962ekq` (`user_id`),
                              CONSTRAINT `FK45c1cuqlljd60ihc9j0962ekq` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`),
                              CONSTRAINT `FKebak8c8m45519djplq0wanuj3` FOREIGN KEY (`post_id`) REFERENCES `tb_post` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_council_fee_fake_user`
--

DROP TABLE IF EXISTS `tb_council_fee_fake_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_council_fee_fake_user` (
                                            `admission_year` int NOT NULL,
                                            `current_completed_semester` int DEFAULT NULL,
                                            `graduation_year` int DEFAULT NULL,
                                            `created_at` datetime(6) DEFAULT NULL,
                                            `updated_at` datetime(6) DEFAULT NULL,
                                            `id` varchar(255) NOT NULL,
                                            `major` varchar(255) NOT NULL,
                                            `name` varchar(255) NOT NULL,
                                            `phone_number` varchar(255) NOT NULL,
                                            `student_id` varchar(255) NOT NULL,
                                            `academic_status` enum('ENROLLED','LEAVE_OF_ABSENCE','GRADUATED','DROPPED_OUT','PROBATION','PROFESSOR','UNDETERMINED') NOT NULL,
                                            `graduation_type` enum('FEBRUARY','AUGUST') DEFAULT NULL,
                                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_crawled_file_link`
--

DROP TABLE IF EXISTS `tb_crawled_file_link`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_crawled_file_link` (
                                        `created_at` datetime(6) DEFAULT NULL,
                                        `updated_at` datetime(6) DEFAULT NULL,
                                        `crawled_notice_id` varchar(255) DEFAULT NULL,
                                        `file_link` varchar(255) NOT NULL,
                                        `file_name` varchar(255) NOT NULL,
                                        `id` varchar(255) NOT NULL,
                                        PRIMARY KEY (`id`),
                                        KEY `FK3gwbq5o1qaosqlaimu5ci73by` (`crawled_notice_id`),
                                        CONSTRAINT `FK3gwbq5o1qaosqlaimu5ci73by` FOREIGN KEY (`crawled_notice_id`) REFERENCES `tb_crawled_notice` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_crawled_notice`
--

DROP TABLE IF EXISTS `tb_crawled_notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_crawled_notice` (
                                     `announce_date` date NOT NULL,
                                     `created_at` datetime(6) DEFAULT NULL,
                                     `updated_at` datetime(6) DEFAULT NULL,
                                     `author` varchar(255) NOT NULL,
                                     `content` text NOT NULL,
                                     `id` varchar(255) NOT NULL,
                                     `image_link` varchar(255) DEFAULT NULL,
                                     `link` varchar(255) NOT NULL,
                                     `title` varchar(255) NOT NULL,
                                     `type` varchar(255) NOT NULL,
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `UK_5jsgsfwrca8bepsku2i3rjosl` (`link`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_event`
--

DROP TABLE IF EXISTS `tb_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_event` (
                            `is_deleted` bit(1) DEFAULT b'0',
                            `created_at` datetime(6) DEFAULT NULL,
                            `updated_at` datetime(6) DEFAULT NULL,
                            `id` varchar(255) NOT NULL,
                            `url` varchar(255) NOT NULL,
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_event_attach_image_uuid_file`
--

DROP TABLE IF EXISTS `tb_event_attach_image_uuid_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_event_attach_image_uuid_file` (
                                                   `created_at` datetime(6) DEFAULT NULL,
                                                   `updated_at` datetime(6) DEFAULT NULL,
                                                   `event_id` varchar(255) NOT NULL,
                                                   `id` varchar(255) NOT NULL,
                                                   `uuid_file_id` varchar(255) NOT NULL,
                                                   PRIMARY KEY (`id`),
                                                   UNIQUE KEY `UK_7a78e300gosbeltnmmwu0h0me` (`event_id`),
                                                   UNIQUE KEY `UK_3uqg1t4dny9705he4cybg1wlq` (`uuid_file_id`),
                                                   KEY `idx_event_attach_image_event_id` (`event_id`),
                                                   KEY `idx_event_attach_image_uuid_file_id` (`uuid_file_id`),
                                                   CONSTRAINT `FKestsb8gvkr95ddt65luku96n6` FOREIGN KEY (`event_id`) REFERENCES `tb_event` (`id`),
                                                   CONSTRAINT `FKploeaqbyn6lhkll4pge14us7d` FOREIGN KEY (`uuid_file_id`) REFERENCES `tb_uuid_file` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_favorite_board`
--

DROP TABLE IF EXISTS `tb_favorite_board`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_favorite_board` (
                                     `created_at` datetime(6) DEFAULT NULL,
                                     `updated_at` datetime(6) DEFAULT NULL,
                                     `board_id` varchar(255) NOT NULL,
                                     `id` varchar(255) NOT NULL,
                                     `user_id` varchar(255) NOT NULL,
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `UK_h1hd4vvc3s6wuleh5p8bbw95b` (`board_id`),
                                     UNIQUE KEY `UK_93xmc4i6k8aupicijs54boo7k` (`user_id`),
                                     CONSTRAINT `FK779ypp0lsdtctv2hovwi9whxj` FOREIGN KEY (`board_id`) REFERENCES `tb_board` (`id`),
                                     CONSTRAINT `FKti6ydu5ij1enqjndfsa3eavqu` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_favorite_post`
--

DROP TABLE IF EXISTS `tb_favorite_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_favorite_post` (
                                    `is_deleted` bit(1) DEFAULT b'0',
                                    `created_at` datetime(6) DEFAULT NULL,
                                    `updated_at` datetime(6) DEFAULT NULL,
                                    `id` varchar(255) NOT NULL,
                                    `post_id` varchar(255) DEFAULT NULL,
                                    `user_id` varchar(255) DEFAULT NULL,
                                    PRIMARY KEY (`id`),
                                    KEY `FKqavkdq0v03v53woarf9k8jb3e` (`post_id`),
                                    KEY `FKq8b00gpymgb124dqmlt5vpf39` (`user_id`),
                                    CONSTRAINT `FKq8b00gpymgb124dqmlt5vpf39` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`),
                                    CONSTRAINT `FKqavkdq0v03v53woarf9k8jb3e` FOREIGN KEY (`post_id`) REFERENCES `tb_post` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_flag`
--

DROP TABLE IF EXISTS `tb_flag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_flag` (
                           `id` varchar(255) NOT NULL,
                           `updated_at` datetime(6) DEFAULT NULL,
                           `created_at` datetime(6) DEFAULT NULL,
                           `tb_key` varchar(255) NOT NULL,
                           `value` bit(1) DEFAULT b'0',
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `UK_9mxdy90sn6d8r6ri8dfudruhr` (`tb_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_form`
--

DROP TABLE IF EXISTS `tb_form`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_form` (
                           `is_allowed_enrolled` bit(1) NOT NULL,
                           `is_allowed_graduation` bit(1) NOT NULL,
                           `is_allowed_leave_of_absence` bit(1) NOT NULL,
                           `is_closed` bit(1) NOT NULL,
                           `is_deleted` bit(1) NOT NULL,
                           `is_need_council_fee_paid` bit(1) NOT NULL,
                           `created_at` datetime(6) DEFAULT NULL,
                           `updated_at` datetime(6) DEFAULT NULL,
                           `circle_id` varchar(255) DEFAULT NULL,
                           `enrolled_registered_semester` varchar(255) DEFAULT NULL,
                           `id` varchar(255) NOT NULL,
                           `leave_of_absence_registered_semester` varchar(255) DEFAULT NULL,
                           `title` varchar(255) NOT NULL,
                           `form_type` enum('POST_FORM','CIRCLE_APPLICATION_FORM') NOT NULL,
                           PRIMARY KEY (`id`),
                           KEY `circle_id_index` (`circle_id`),
                           CONSTRAINT `FKsihc207xm6hj4jd97lvyixgvg` FOREIGN KEY (`circle_id`) REFERENCES `tb_circle` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_form_question`
--

DROP TABLE IF EXISTS `tb_form_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_form_question` (
                                    `is_multiple` bit(1) NOT NULL,
                                    `number` int NOT NULL,
                                    `created_at` datetime(6) DEFAULT NULL,
                                    `updated_at` datetime(6) DEFAULT NULL,
                                    `form_id` varchar(255) NOT NULL,
                                    `id` varchar(255) NOT NULL,
                                    `question_text` varchar(255) NOT NULL,
                                    `question_type` enum('SUBJECTIVE','OBJECTIVE') NOT NULL,
                                    PRIMARY KEY (`id`),
                                    KEY `form_id_index` (`form_id`),
                                    CONSTRAINT `FKka1hcmtgi37phx060r96uuniq` FOREIGN KEY (`form_id`) REFERENCES `tb_form` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_form_question_option`
--

DROP TABLE IF EXISTS `tb_form_question_option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_form_question_option` (
                                           `number` int NOT NULL,
                                           `created_at` datetime(6) DEFAULT NULL,
                                           `updated_at` datetime(6) DEFAULT NULL,
                                           `form_question_id` varchar(255) NOT NULL,
                                           `id` varchar(255) NOT NULL,
                                           `option_text` varchar(255) NOT NULL,
                                           PRIMARY KEY (`id`),
                                           KEY `form_question_id_index` (`form_question_id`),
                                           CONSTRAINT `FKkv1jic6yn7rrr2mrlluilwj9n` FOREIGN KEY (`form_question_id`) REFERENCES `tb_form_question` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_inquiry`
--

DROP TABLE IF EXISTS `tb_inquiry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_inquiry` (
                              `is_deleted` bit(1) DEFAULT b'0',
                              `created_at` datetime(6) DEFAULT NULL,
                              `updated_at` datetime(6) DEFAULT NULL,
                              `content` text NOT NULL,
                              `id` varchar(255) NOT NULL,
                              `title` varchar(255) NOT NULL,
                              `user_id` varchar(255) NOT NULL,
                              PRIMARY KEY (`id`),
                              KEY `FKlkre2tncjdw7t8mq5x9onalme` (`user_id`),
                              CONSTRAINT `FKlkre2tncjdw7t8mq5x9onalme` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_latest_crawl`
--

DROP TABLE IF EXISTS `tb_latest_crawl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_latest_crawl` (
                                   `created_at` datetime(6) DEFAULT NULL,
                                   `updated_at` datetime(6) DEFAULT NULL,
                                   `id` varchar(255) NOT NULL,
                                   `latest_url` varchar(255) NOT NULL,
                                   `crawl_category` enum('CAU_SW_NOTICE','CAU_PORTAL_NOTICE') NOT NULL,
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_like_child_comment`
--

DROP TABLE IF EXISTS `tb_like_child_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_like_child_comment` (
                                         `created_at` datetime(6) DEFAULT NULL,
                                         `updated_at` datetime(6) DEFAULT NULL,
                                         `child_comment_id` varchar(255) DEFAULT NULL,
                                         `id` varchar(255) NOT NULL,
                                         `user_id` varchar(255) DEFAULT NULL,
                                         PRIMARY KEY (`id`),
                                         KEY `FKpcfrmcqr6j5p96w4o6ekg5v84` (`child_comment_id`),
                                         KEY `FK917vru5uo96cg0cppqolnm218` (`user_id`),
                                         CONSTRAINT `FK917vru5uo96cg0cppqolnm218` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`),
                                         CONSTRAINT `FKpcfrmcqr6j5p96w4o6ekg5v84` FOREIGN KEY (`child_comment_id`) REFERENCES `tb_child_comment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_like_comment`
--

DROP TABLE IF EXISTS `tb_like_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_like_comment` (
                                   `created_at` datetime(6) DEFAULT NULL,
                                   `updated_at` datetime(6) DEFAULT NULL,
                                   `comment_id` varchar(255) DEFAULT NULL,
                                   `id` varchar(255) NOT NULL,
                                   `user_id` varchar(255) DEFAULT NULL,
                                   PRIMARY KEY (`id`),
                                   KEY `FKf0cu0ei1kpmat8xvl7rwm1urv` (`comment_id`),
                                   KEY `FK32g6rm8jtugo3ve18bss452ek` (`user_id`),
                                   CONSTRAINT `FK32g6rm8jtugo3ve18bss452ek` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`),
                                   CONSTRAINT `FKf0cu0ei1kpmat8xvl7rwm1urv` FOREIGN KEY (`comment_id`) REFERENCES `tb_comment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_like_post`
--

DROP TABLE IF EXISTS `tb_like_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_like_post` (
                                `created_at` datetime(6) DEFAULT NULL,
                                `updated_at` datetime(6) DEFAULT NULL,
                                `id` varchar(255) NOT NULL,
                                `post_id` varchar(255) DEFAULT NULL,
                                `user_id` varchar(255) DEFAULT NULL,
                                PRIMARY KEY (`id`),
                                KEY `FK73pv6iorxfcdvctu0mpg206x8` (`post_id`),
                                KEY `FK713sod6n69tcbtxpeq1pqpds5` (`user_id`),
                                CONSTRAINT `FK713sod6n69tcbtxpeq1pqpds5` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`),
                                CONSTRAINT `FK73pv6iorxfcdvctu0mpg206x8` FOREIGN KEY (`post_id`) REFERENCES `tb_post` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_locker`
--

DROP TABLE IF EXISTS `tb_locker`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_locker` (
                             `id` varchar(255) NOT NULL,
                             `created_at` datetime(6) DEFAULT NULL,
                             `updated_at` datetime(6) DEFAULT NULL,
                             `expire_date` datetime(6) DEFAULT NULL,
                             `is_active` bit(1) DEFAULT b'1',
                             `locker_number` bigint NOT NULL,
                             `location_id` varchar(255) NOT NULL,
                             `user_id` varchar(255) DEFAULT NULL,
                             PRIMARY KEY (`id`),
                             KEY `FKdkeceafnif5f6kji4f0kmie6n` (`location_id`),
                             KEY `FKfprmp8bd5hasx3nh5h9xuaijt` (`user_id`),
                             CONSTRAINT `FKdkeceafnif5f6kji4f0kmie6n` FOREIGN KEY (`location_id`) REFERENCES `tb_locker_location` (`id`),
                             CONSTRAINT `FKfprmp8bd5hasx3nh5h9xuaijt` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_locker_location`
--

DROP TABLE IF EXISTS `tb_locker_location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_locker_location` (
                                      `created_at` datetime(6) DEFAULT NULL,
                                      `updated_at` datetime(6) DEFAULT NULL,
                                      `id` varchar(255) NOT NULL,
                                      `name` enum('SECOND','THIRD','FOURTH') NOT NULL,
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `UK_5n4i9ncqv3c3ns9tunyxq1vww` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_locker_log`
--

DROP TABLE IF EXISTS `tb_locker_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_locker_log` (
                                 `id` varchar(255) NOT NULL,
                                 `created_at` datetime(6) DEFAULT NULL,
                                 `updated_at` datetime(6) DEFAULT NULL,
                                 `action` enum('ENABLE','DISABLE','REGISTER','RETURN','EXTEND') NOT NULL,
                                 `locker_location_name` varchar(255) DEFAULT NULL,
                                 `locker_number` bigint NOT NULL,
                                 `message` varchar(255) DEFAULT NULL,
                                 `user_email` varchar(255) DEFAULT NULL,
                                 `user_name` varchar(255) DEFAULT NULL,
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_notification`
--

DROP TABLE IF EXISTS `tb_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_notification` (
                                   `is_global` bit(1) DEFAULT b'0',
                                   `created_at` datetime(6) DEFAULT NULL,
                                   `updated_at` datetime(6) DEFAULT NULL,
                                   `content` varchar(255) DEFAULT NULL,
                                   `id` varchar(255) NOT NULL,
                                   `user_id` varchar(255) DEFAULT NULL,
                                   `notice_type` enum('POST','COMMENT') DEFAULT NULL,
                                   `body` varchar(255) DEFAULT NULL,
                                   `title` varchar(255) DEFAULT NULL,
                                   PRIMARY KEY (`id`),
                                   KEY `FK9ihj3k9lv33u6qd10wq18f482` (`user_id`),
                                   CONSTRAINT `FK9ihj3k9lv33u6qd10wq18f482` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_post`
--

DROP TABLE IF EXISTS `tb_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_post` (
                           `id` varchar(255) NOT NULL,
                           `created_at` datetime(6) DEFAULT NULL,
                           `updated_at` datetime(6) DEFAULT NULL,
                           `content` text NOT NULL,
                           `is_deleted` bit(1) DEFAULT b'0',
                           `title` varchar(255) NOT NULL,
                           `is_anonymous` bit(1) NOT NULL DEFAULT b'0',
                           `is_question` bit(1) NOT NULL DEFAULT b'0',
                           `board_id` varchar(255) NOT NULL,
                           `user_id` varchar(255) NOT NULL,
                           `form_id` varchar(255) DEFAULT NULL,
                           `vote_id` varchar(255) DEFAULT NULL,
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `UK_enof2bygn1xgrpr4mwie0w2j` (`form_id`),
                           UNIQUE KEY `UK_l5mhtqwocn13ic8vaiqor2rv1` (`vote_id`),
                           KEY `board_id_index` (`board_id`),
                           KEY `user_id_index` (`user_id`),
                           KEY `form_id_index` (`form_id`),
                           CONSTRAINT `FK6x1w92hs1xh6y8o5vyql9sau4` FOREIGN KEY (`vote_id`) REFERENCES `tb_vote` (`id`),
                           CONSTRAINT `FKbb41srhc79p2dk7ok6b8w7p3s` FOREIGN KEY (`form_id`) REFERENCES `tb_form` (`id`),
                           CONSTRAINT `FKhx7a7k3pf66vpddqg5pr12anw` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`),
                           CONSTRAINT `FKsn6tvkjtynqrfxsooaojns5uu` FOREIGN KEY (`board_id`) REFERENCES `tb_board` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_post_attach_image_uuid_file`
--

DROP TABLE IF EXISTS `tb_post_attach_image_uuid_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_post_attach_image_uuid_file` (
                                                  `created_at` datetime(6) DEFAULT NULL,
                                                  `updated_at` datetime(6) DEFAULT NULL,
                                                  `id` varchar(255) NOT NULL,
                                                  `post_id` varchar(255) NOT NULL,
                                                  `uuid_file_id` varchar(255) NOT NULL,
                                                  PRIMARY KEY (`id`),
                                                  UNIQUE KEY `UK_dec6rw0gbb32hu2bgvoygrhql` (`uuid_file_id`),
                                                  KEY `idx_post_attach_image_post_id` (`post_id`),
                                                  KEY `idx_post_attach_image_uuid_file_id` (`uuid_file_id`),
                                                  CONSTRAINT `FK791svsqhdgfcl2f5y1fp98fgn` FOREIGN KEY (`uuid_file_id`) REFERENCES `tb_uuid_file` (`id`),
                                                  CONSTRAINT `FKj0wonhfu05hwn6p38any0mycy` FOREIGN KEY (`post_id`) REFERENCES `tb_post` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_reply`
--

DROP TABLE IF EXISTS `tb_reply`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_reply` (
                            `created_at` datetime(6) DEFAULT NULL,
                            `updated_at` datetime(6) DEFAULT NULL,
                            `form_id` varchar(255) NOT NULL,
                            `id` varchar(255) NOT NULL,
                            `user_id` varchar(255) NOT NULL,
                            PRIMARY KEY (`id`),
                            KEY `FKbo9i5g84h1g2l027f5hw3jlxx` (`form_id`),
                            KEY `FKkjv6in4jhfr484po2uqetymy9` (`user_id`),
                            CONSTRAINT `FKbo9i5g84h1g2l027f5hw3jlxx` FOREIGN KEY (`form_id`) REFERENCES `tb_form` (`id`),
                            CONSTRAINT `FKkjv6in4jhfr484po2uqetymy9` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_reply_question`
--

DROP TABLE IF EXISTS `tb_reply_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_reply_question` (
                                     `created_at` datetime(6) DEFAULT NULL,
                                     `updated_at` datetime(6) DEFAULT NULL,
                                     `form_question_id` varchar(255) NOT NULL,
                                     `id` varchar(255) NOT NULL,
                                     `reply_id` varchar(255) NOT NULL,
                                     `selected_option_list` varchar(255) DEFAULT NULL,
                                     `question_answer` text,
                                     PRIMARY KEY (`id`),
                                     KEY `reply_id_index` (`reply_id`),
                                     KEY `form_question_id_index` (`form_question_id`),
                                     CONSTRAINT `FKaaw5kmewbcglmurc2d15ulu4l` FOREIGN KEY (`reply_id`) REFERENCES `tb_reply` (`id`),
                                     CONSTRAINT `FKngdilacqipy027g6fgup9fw6r` FOREIGN KEY (`form_question_id`) REFERENCES `tb_form_question` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_semester`
--

DROP TABLE IF EXISTS `tb_semester`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_semester` (
                               `end_date` date NOT NULL,
                               `is_current` bit(1) NOT NULL,
                               `semester_type` tinyint NOT NULL,
                               `semester_year` int NOT NULL,
                               `start_date` date NOT NULL,
                               `created_at` datetime(6) DEFAULT NULL,
                               `updated_at` datetime(6) DEFAULT NULL,
                               `id` varchar(255) NOT NULL,
                               `update_user_id` varchar(255) NOT NULL,
                               PRIMARY KEY (`id`),
                               KEY `FK1f7qdbcou7kki27na1yhgn4a5` (`update_user_id`),
                               CONSTRAINT `FK1f7qdbcou7kki27na1yhgn4a5` FOREIGN KEY (`update_user_id`) REFERENCES `tb_user` (`id`),
                               CONSTRAINT `tb_semester_chk_1` CHECK ((`semester_type` between 0 and 3))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_text_field`
--

DROP TABLE IF EXISTS `tb_text_field`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_text_field` (
                                 `id` varchar(255) NOT NULL,
                                 `created_at` datetime(6) DEFAULT NULL,
                                 `updated_at` datetime(6) DEFAULT NULL,
                                 `tb_key` varchar(255) NOT NULL,
                                 `value` varchar(255) NOT NULL,
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `UK_1jym6b281dx706drcf9gbc8iw` (`tb_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_user`
--

DROP TABLE IF EXISTS `tb_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_user` (
                           `id` varchar(255) NOT NULL,
                           `created_at` datetime(6) DEFAULT NULL,
                           `updated_at` datetime(6) DEFAULT NULL,
                           `admission_year` int NOT NULL,
                           `email` varchar(255) NOT NULL,
                           `name` varchar(255) NOT NULL,
                           `password` varchar(255) NOT NULL,
                           `state` enum('AWAIT','ACTIVE','INACTIVE','REJECT','DROP','DELETED') NOT NULL,
                           `student_id` varchar(255) DEFAULT NULL,
                           `current_completed_semester` int DEFAULT NULL,
                           `graduation_year` int DEFAULT NULL,
                           `is_v2` bit(1) NOT NULL,
                           `academic_status_note` varchar(255) DEFAULT NULL,
                           `major` varchar(255) DEFAULT NULL,
                           `nickname` varchar(255) DEFAULT NULL,
                           `phone_number` varchar(255) DEFAULT NULL,
                           `rejection_or_drop_reason` varchar(255) DEFAULT NULL,
                           `academic_status` enum('ENROLLED','LEAVE_OF_ABSENCE','GRADUATED','DROPPED_OUT','PROBATION','PROFESSOR','UNDETERMINED') NOT NULL,
                           `graduation_type` enum('FEBRUARY','AUGUST') DEFAULT NULL,
                           `fcm_token` varchar(255) DEFAULT NULL,
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `UK_4vih17mube9j7cqyjlfbcrk4m` (`email`),
                           UNIQUE KEY `UK_ig0bbysxr6nnpxo4qn2btdcc8` (`nickname`),
                           UNIQUE KEY `UK_qi5yr54j76lu2meatpwefocym` (`phone_number`),
                           UNIQUE KEY `UK_djjmuep18k7xs81lgqgutfhjd` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_user_academic_record_application`
--

DROP TABLE IF EXISTS `tb_user_academic_record_application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_user_academic_record_application` (
                                                       `target_completed_semester` int DEFAULT NULL,
                                                       `created_at` datetime(6) DEFAULT NULL,
                                                       `updated_at` datetime(6) DEFAULT NULL,
                                                       `id` varchar(255) NOT NULL,
                                                       `note` varchar(255) DEFAULT NULL,
                                                       `reject_message` varchar(255) DEFAULT NULL,
                                                       `user_id` varchar(255) NOT NULL,
                                                       `academic_record_request_status` enum('ACCEPT','REJECT','AWAIT','CLOSE') NOT NULL,
                                                       `target_academic_status` enum('ENROLLED','LEAVE_OF_ABSENCE','GRADUATED','DROPPED_OUT','PROBATION','PROFESSOR','UNDETERMINED') NOT NULL,
                                                       PRIMARY KEY (`id`),
                                                       KEY `user_id_index` (`user_id`),
                                                       CONSTRAINT `FK6o42vtjrql4spa3qheorq57e7` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_user_academic_record_application_attach_image_uuid_file`
--

DROP TABLE IF EXISTS `tb_user_academic_record_application_attach_image_uuid_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_user_academic_record_application_attach_image_uuid_file` (
                                                                              `created_at` datetime(6) DEFAULT NULL,
                                                                              `updated_at` datetime(6) DEFAULT NULL,
                                                                              `id` varchar(255) NOT NULL,
                                                                              `user_academic_record_application_id` varchar(255) NOT NULL,
                                                                              `uuid_file_id` varchar(255) NOT NULL,
                                                                              PRIMARY KEY (`id`),
                                                                              UNIQUE KEY `UK_m5oic5hwkfi8a67wp49s3qdn0` (`uuid_file_id`),
                                                                              KEY `idx_user_academic_record_application_attach_image_application_id` (`user_academic_record_application_id`),
                                                                              KEY `idx_user_academic_record_application_attach_image_uuid_file_id` (`uuid_file_id`),
                                                                              CONSTRAINT `FKepr9k9enk156gcxua0nvv8ldg` FOREIGN KEY (`user_academic_record_application_id`) REFERENCES `tb_user_academic_record_application` (`id`),
                                                                              CONSTRAINT `FKs0kth7tg0drbg2a74rhdy67ck` FOREIGN KEY (`uuid_file_id`) REFERENCES `tb_uuid_file` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_user_academic_record_log`
--

DROP TABLE IF EXISTS `tb_user_academic_record_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_user_academic_record_log` (
                                               `graduation_year` int DEFAULT NULL,
                                               `created_at` datetime(6) DEFAULT NULL,
                                               `updated_at` datetime(6) DEFAULT NULL,
                                               `controlled_user_email` varchar(255) NOT NULL,
                                               `controlled_user_name` varchar(255) NOT NULL,
                                               `controlled_user_student_id` varchar(255) NOT NULL,
                                               `id` varchar(255) NOT NULL,
                                               `note` varchar(255) DEFAULT NULL,
                                               `reject_message` varchar(255) DEFAULT NULL,
                                               `target_user_email` varchar(255) NOT NULL,
                                               `target_user_name` varchar(255) NOT NULL,
                                               `target_user_student_id` varchar(255) NOT NULL,
                                               `graduation_type` enum('FEBRUARY','AUGUST') DEFAULT NULL,
                                               `prior_academic_record_application_id` enum('ENROLLED','LEAVE_OF_ABSENCE','GRADUATED','DROPPED_OUT','PROBATION','PROFESSOR','UNDETERMINED') NOT NULL,
                                               `target_academic_record_request_status` enum('ACCEPT','REJECT','AWAIT','CLOSE') DEFAULT NULL,
                                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_user_academic_record_log_attach_image`
--

DROP TABLE IF EXISTS `tb_user_academic_record_log_attach_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_user_academic_record_log_attach_image` (
                                                            `created_at` datetime(6) DEFAULT NULL,
                                                            `updated_at` datetime(6) DEFAULT NULL,
                                                            `id` varchar(255) NOT NULL,
                                                            `user_academic_record_log_id` varchar(255) NOT NULL,
                                                            `uuid_file_id` varchar(255) NOT NULL,
                                                            PRIMARY KEY (`id`),
                                                            KEY `idx_user_academic_record_log_attach_image_log_id` (`user_academic_record_log_id`),
                                                            KEY `idx_user_academic_record_log_attach_image_uuid_file_id` (`uuid_file_id`),
                                                            CONSTRAINT `FKiiknx3qc1s4sc16fokab160nj` FOREIGN KEY (`user_academic_record_log_id`) REFERENCES `tb_user_academic_record_log` (`id`),
                                                            CONSTRAINT `FKn6qscetye4wllnxsjwx2ault1` FOREIGN KEY (`uuid_file_id`) REFERENCES `tb_uuid_file` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_user_admission`
--

DROP TABLE IF EXISTS `tb_user_admission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_user_admission` (
                                     `created_at` datetime(6) DEFAULT NULL,
                                     `updated_at` datetime(6) DEFAULT NULL,
                                     `description` varchar(255) DEFAULT NULL,
                                     `id` varchar(255) NOT NULL,
                                     `user_id` varchar(255) NOT NULL,
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `UK_5c0j7cyx9b1awc3y5v8m8t25e` (`user_id`),
                                     CONSTRAINT `FKdkcgsxmu3ph4cddd7gydgfx3p` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_user_admission_attach_image_uuid_file`
--

DROP TABLE IF EXISTS `tb_user_admission_attach_image_uuid_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_user_admission_attach_image_uuid_file` (
                                                            `created_at` datetime(6) DEFAULT NULL,
                                                            `updated_at` datetime(6) DEFAULT NULL,
                                                            `id` varchar(255) NOT NULL,
                                                            `user_admission_id` varchar(255) NOT NULL,
                                                            `uuid_file_id` varchar(255) NOT NULL,
                                                            PRIMARY KEY (`id`),
                                                            UNIQUE KEY `UK_8yfr48oo34p9gkbl0ro62dxcx` (`uuid_file_id`),
                                                            KEY `idx_user_admission_attach_image__admission_id` (`user_admission_id`),
                                                            KEY `idx_user_admission_attach_image_uuid_file_id` (`uuid_file_id`),
                                                            CONSTRAINT `FK8kr6tasvi8kyw0m7vlj1oab9i` FOREIGN KEY (`uuid_file_id`) REFERENCES `tb_uuid_file` (`id`),
                                                            CONSTRAINT `FKnemwteegkkpge96155ubd3ss8` FOREIGN KEY (`user_admission_id`) REFERENCES `tb_user_admission` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_user_admission_log`
--

DROP TABLE IF EXISTS `tb_user_admission_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_user_admission_log` (
                                         `id` varchar(255) NOT NULL,
                                         `created_at` datetime(6) DEFAULT NULL,
                                         `updated_at` datetime(6) DEFAULT NULL,
                                         `action` enum('ACCEPT','REJECT') NOT NULL,
                                         `admin_user_email` varchar(255) NOT NULL,
                                         `admin_user_name` varchar(255) NOT NULL,
                                         `description` varchar(255) DEFAULT NULL,
                                         `user_email` varchar(255) NOT NULL,
                                         `user_name` varchar(255) NOT NULL,
                                         `reject_reason` varchar(255) DEFAULT NULL,
                                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_user_admission_log_attach_image_uuid_file`
--

DROP TABLE IF EXISTS `tb_user_admission_log_attach_image_uuid_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_user_admission_log_attach_image_uuid_file` (
                                                                `created_at` datetime(6) DEFAULT NULL,
                                                                `updated_at` datetime(6) DEFAULT NULL,
                                                                `id` varchar(255) NOT NULL,
                                                                `user_admission_log_id` varchar(255) NOT NULL,
                                                                `uuid_file_id` varchar(255) NOT NULL,
                                                                PRIMARY KEY (`id`),
                                                                UNIQUE KEY `UK_q92p8k87vpukt4uxceu54t98l` (`uuid_file_id`),
                                                                KEY `idx_user_admission_log_attach_image_log_id` (`user_admission_log_id`),
                                                                KEY `idx_user_admission_log_attach_image_uuid_file_id` (`uuid_file_id`),
                                                                CONSTRAINT `FK31g16rte8nvuns6lcd8dtquig` FOREIGN KEY (`uuid_file_id`) REFERENCES `tb_uuid_file` (`id`),
                                                                CONSTRAINT `FKg8j9u8vgj2vswq0hltfhvbsm9` FOREIGN KEY (`user_admission_log_id`) REFERENCES `tb_user_admission_log` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_user_board_subscribe`
--

DROP TABLE IF EXISTS `tb_user_board_subscribe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_user_board_subscribe` (
                                           `is_subscribed` bit(1) DEFAULT NULL,
                                           `created_at` datetime(6) DEFAULT NULL,
                                           `updated_at` datetime(6) DEFAULT NULL,
                                           `board_id` varchar(255) DEFAULT NULL,
                                           `id` varchar(255) NOT NULL,
                                           `user_id` varchar(255) DEFAULT NULL,
                                           PRIMARY KEY (`id`),
                                           KEY `FKm3w9ve1mqdvu5yya43lgktqka` (`board_id`),
                                           KEY `FKps9i1rpnulrnluj7m6fl212x` (`user_id`),
                                           CONSTRAINT `FKm3w9ve1mqdvu5yya43lgktqka` FOREIGN KEY (`board_id`) REFERENCES `tb_board` (`id`),
                                           CONSTRAINT `FKps9i1rpnulrnluj7m6fl212x` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_user_council_fee`
--

DROP TABLE IF EXISTS `tb_user_council_fee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_user_council_fee` (
                                       `is_joined_service` bit(1) NOT NULL,
                                       `is_paid` int NOT NULL,
                                       `is_refunded` bit(1) NOT NULL,
                                       `num_of_paid_semester` int NOT NULL,
                                       `refunded_at` int DEFAULT NULL,
                                       `created_at` datetime(6) DEFAULT NULL,
                                       `updated_at` datetime(6) DEFAULT NULL,
                                       `council_fee_fake_user_id` varchar(255) DEFAULT NULL,
                                       `id` varchar(255) NOT NULL,
                                       `user_id` varchar(255) DEFAULT NULL,
                                       `paid_at` int NOT NULL,
                                       PRIMARY KEY (`id`),
                                       UNIQUE KEY `UK_gnokg5xtbxsb61hft4a2g8eng` (`council_fee_fake_user_id`),
                                       UNIQUE KEY `UK_4hbj83xu0pmbq16nmj3q9kk3a` (`user_id`),
                                       CONSTRAINT `FK54eg4kykxfyqe461th83o012d` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`),
                                       CONSTRAINT `FKa2vx6xctboiyveok54n93qfr7` FOREIGN KEY (`council_fee_fake_user_id`) REFERENCES `tb_council_fee_fake_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_user_council_fee_log`
--

DROP TABLE IF EXISTS `tb_user_council_fee_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_user_council_fee_log` (
                                           `admission_year` int NOT NULL,
                                           `current_completed_semester` int NOT NULL,
                                           `graduation_year` int DEFAULT NULL,
                                           `joined_at` date DEFAULT NULL,
                                           `num_of_paid_semester` bit(1) NOT NULL,
                                           `paid_at` int NOT NULL,
                                           `target_is_joined_service` bit(1) NOT NULL,
                                           `target_is_refunded` bit(1) NOT NULL,
                                           `target_num_of_paid_semester` int NOT NULL,
                                           `target_paid_at` int NOT NULL,
                                           `target_refunded_at` int DEFAULT NULL,
                                           `time_of_semester_year` int NOT NULL,
                                           `created_at` datetime(6) DEFAULT NULL,
                                           `updated_at` datetime(6) DEFAULT NULL,
                                           `controlled_user_email` varchar(255) NOT NULL,
                                           `controlled_user_name` varchar(255) NOT NULL,
                                           `controlled_user_student_id` varchar(255) NOT NULL,
                                           `email` varchar(255) DEFAULT NULL,
                                           `id` varchar(255) NOT NULL,
                                           `major` varchar(255) NOT NULL,
                                           `phone_number` varchar(255) NOT NULL,
                                           `student_id` varchar(255) NOT NULL,
                                           `user_name` varchar(255) NOT NULL,
                                           `academic_status` enum('ENROLLED','LEAVE_OF_ABSENCE','GRADUATED','DROPPED_OUT','PROBATION','PROFESSOR','UNDETERMINED') NOT NULL,
                                           `graduation_type` enum('FEBRUARY','AUGUST') DEFAULT NULL,
                                           `time_of_semester_type` enum('FIRST','SECOND','SUMMER','WINTER') NOT NULL,
                                           `update_type` enum('CREATE','UPDATE','DELETE') NOT NULL,
                                           `council_fee_log_type` enum('CREATE','UPDATE','DELETE') NOT NULL,
                                           `is_applied_this_semester` bit(1) NOT NULL,
                                           `rest_of_semester` int NOT NULL,
                                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_user_profile_uuid_file`
--

DROP TABLE IF EXISTS `tb_user_profile_uuid_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_user_profile_uuid_file` (
                                             `created_at` datetime(6) DEFAULT NULL,
                                             `updated_at` datetime(6) DEFAULT NULL,
                                             `id` varchar(255) NOT NULL,
                                             `user_id` varchar(255) NOT NULL,
                                             `uuid_file_id` varchar(255) NOT NULL,
                                             PRIMARY KEY (`id`),
                                             UNIQUE KEY `UK_pp99y4eo5m8f7k0o6lecshsps` (`user_id`),
                                             UNIQUE KEY `UK_ioi0qbw6h8fw1vqkssyxt8x7u` (`uuid_file_id`),
                                             KEY `idx_user_profile_user_id` (`user_id`),
                                             KEY `idx_user_profile_uuid_file_id` (`uuid_file_id`),
                                             CONSTRAINT `FK6p29j8ic8e13vonmldawkoq67` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`),
                                             CONSTRAINT `FKkciuncg6fgjygqctb69iyh0nt` FOREIGN KEY (`uuid_file_id`) REFERENCES `tb_uuid_file` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_uuid_file`
--

DROP TABLE IF EXISTS `tb_uuid_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_uuid_file` (
                                `is_used` bit(1) NOT NULL DEFAULT b'1',
                                `created_at` datetime(6) DEFAULT NULL,
                                `updated_at` datetime(6) DEFAULT NULL,
                                `extension` varchar(255) NOT NULL,
                                `file_key` varchar(255) NOT NULL,
                                `id` varchar(255) NOT NULL,
                                `raw_file_name` varchar(255) NOT NULL,
                                `uuid` varchar(255) NOT NULL,
                                `file_path` enum('USER_PROFILE','USER_ADMISSION','USER_ACADEMIC_RECORD_APPLICATION','CIRCLE_PROFILE','POST','CALENDAR','EVENT','ETC') NOT NULL,
                                `file_url` text NOT NULL,
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `UK_ok1ekub9dpdyhsx9vexnkn1sn` (`file_key`),
                                UNIQUE KEY `UK_o61trieqgmab71n786b9aic93` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_vote`
--

DROP TABLE IF EXISTS `tb_vote`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_vote` (
                           `allow_anonymous` bit(1) NOT NULL,
                           `allow_multiple` bit(1) NOT NULL,
                           `is_end` bit(1) NOT NULL,
                           `created_at` datetime(6) DEFAULT NULL,
                           `updated_at` datetime(6) DEFAULT NULL,
                           `id` varchar(255) NOT NULL,
                           `title` varchar(255) DEFAULT NULL,
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_vote_option`
--

DROP TABLE IF EXISTS `tb_vote_option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_vote_option` (
                                  `created_at` datetime(6) DEFAULT NULL,
                                  `updated_at` datetime(6) DEFAULT NULL,
                                  `id` varchar(255) NOT NULL,
                                  `option_name` varchar(255) DEFAULT NULL,
                                  `vote_id` varchar(255) NOT NULL,
                                  PRIMARY KEY (`id`),
                                  KEY `FKnni9i6g97ug9egsfw6n01titi` (`vote_id`),
                                  CONSTRAINT `FKnni9i6g97ug9egsfw6n01titi` FOREIGN KEY (`vote_id`) REFERENCES `tb_vote` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_vote_record`
--

DROP TABLE IF EXISTS `tb_vote_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_vote_record` (
                                  `created_at` datetime(6) DEFAULT NULL,
                                  `updated_at` datetime(6) DEFAULT NULL,
                                  `id` varchar(255) NOT NULL,
                                  `user_id` varchar(255) DEFAULT NULL,
                                  `vote_option_id` varchar(255) DEFAULT NULL,
                                  PRIMARY KEY (`id`),
                                  KEY `FKj1rlo6wtl6qibjq8an1qj0ucl` (`user_id`),
                                  KEY `FK6nwktkmc5plj4grvt2999fxcg` (`vote_option_id`),
                                  CONSTRAINT `FK6nwktkmc5plj4grvt2999fxcg` FOREIGN KEY (`vote_option_id`) REFERENCES `tb_vote_option` (`id`),
                                  CONSTRAINT `FKj1rlo6wtl6qibjq8an1qj0ucl` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_roles` (
                              `user_id` varchar(255) NOT NULL,
                              `role` enum('ADMIN','PRESIDENT','VICE_PRESIDENT','COUNCIL','LEADER_1','LEADER_2','LEADER_3','LEADER_4','LEADER_CIRCLE','LEADER_ALUMNI','COMMON','NONE','PROFESSOR') NOT NULL,
                              PRIMARY KEY (`user_id`,`role`),
                              CONSTRAINT `FKlqb868dhpatxi3e1m1nu3ukr5` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-08-17  1:44:57
