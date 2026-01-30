-- Migration: CreateScheduleTable
CREATE TABLE `tb_schedule` (
                               `id` varchar(255) NOT NULL,
                               `created_at` datetime(6) DEFAULT NULL,
                               `updated_at` datetime(6) DEFAULT NULL,
                               `title` varchar(255) NOT NULL,
                               `type` enum('ACADEMIC','DEPARTMENT','CCSSAA','STUDENT_COUNCIL','COMPETITION','HOLIDAY') NOT NULL,
                               `start` datetime(6) NOT NULL,
                               `end` datetime(6) NOT NULL,
                               `creator` varchar(255) DEFAULT NULL,
                               PRIMARY KEY (`id`),
                               KEY `idx_schedule_creator` (`creator`),
                               KEY `idx_schedule_type` (`type`),
                               KEY `idx_schedule_start_end` (`start`, `end`),
                               CONSTRAINT `fk_schedule_creator` FOREIGN KEY (`creator`) REFERENCES `tb_user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
