-- Migration: ModifyUserRoleField

alter table user_roles
    modify role enum ('ADMIN', 'PRESIDENT', 'VICE_PRESIDENT', 'COUNCIL', 'LEADER_1',
    'LEADER_2', 'LEADER_3', 'LEADER_4', 'LEADER_CIRCLE', 'LEADER_ALUMNI',
    'COMMON', 'NONE', 'PROFESSOR', 'ALUMNI_MANAGER') not null;