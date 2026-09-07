-- AI학과 개설(2021) 이전 입학생 중 department 미설정 유저 백필
-- DepartmentResolver.departmentPeriods 와 동일한 연도 구간을 적용한다.
UPDATE tb_user
SET department = CASE
    WHEN admission_year BETWEEN 2018 AND 2020 THEN 'SCHOOL_OF_SW'
    WHEN admission_year BETWEEN 2003 AND 2017 THEN 'SCHOOL_OF_CSE'
    WHEN admission_year BETWEEN 1993 AND 2002 THEN 'DEPT_OF_CSE'
    WHEN admission_year BETWEEN 1972 AND 1992 THEN 'DEPT_OF_CS'
END
WHERE department IS NULL
  AND admission_year IS NOT NULL
  AND admission_year < 2021;