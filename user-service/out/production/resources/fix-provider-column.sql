-- User 테이블의 provider 컬럼 길이 수정
-- MySQL 콘솔에서 실행하세요

USE covercloud_user;

-- provider 컬럼을 VARCHAR(10)으로 변경 (KAKAO, NAVER, TEST 모두 수용)
ALTER TABLE user MODIFY COLUMN provider VARCHAR(10) NOT NULL;

-- 확인 쿼리
DESC user;
SELECT * FROM user;

