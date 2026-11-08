-- =====================================================================
-- 병원 진료 예약 관리 시스템 - DDL (CREATE TABLE)
-- 작성: 정제은
-- 기준 자료: 02-2 데베프리티걸 도메인 클래스 설계서 (2026.10.14, 김민진·허유정·방민채·정제은)
--            5장 "데이터베이스(테이블) 매핑" 표(5.1 클래스-테이블 매핑, 5.2 외래키 관계) 기준
--
-- [타입 변환 기준]
-- - java.util.Date 필드는 Oracle DATE로 통일 매핑 (날짜+시간 모두 표현)
-- - boolean -> NUMBER(1) + CHECK(0,1)
-- - TEXT로 표기된 긴 텍스트 필드(bio, diagnosis, prescription) -> CLOB
-- - 모든 PK는 NUMBER(19) + 별도 SEQUENCE
-- - CHECK 제약에 이름을 붙여 위반 시 어떤 제약인지 바로 확인 가능하도록 함
-- - 취소(X)된 예약을 제외하고 의사/회원 기준 중복 예약을 막는 함수 기반 UNIQUE 인덱스 적용
-- =====================================================================


-- ---------------------------------------------------------------------
-- 0. 기존 객체 정리 (재실행 대비)
-- ---------------------------------------------------------------------
BEGIN
  FOR t IN (SELECT table_name FROM user_tables WHERE table_name IN (
    'MEMBER','DEPARTMENT','DOCTOR','DOCTOR_SCHEDULE','DOCTOR_OFF',
    'SYMPTOM_KEYWORD','RESERVATION','MEDICAL_RECORD'))
  LOOP
    EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS PURGE';
  END LOOP;
END;
/

BEGIN
  FOR s IN (SELECT sequence_name FROM user_sequences WHERE sequence_name IN (
    'SEQ_MEMBER','SEQ_DEPARTMENT','SEQ_DOCTOR','SEQ_DOCTOR_SCHEDULE','SEQ_DOCTOR_OFF',
    'SEQ_SYMPTOM_KEYWORD','SEQ_RESERVATION','SEQ_MEDICAL_RECORD'))
  LOOP
    EXECUTE IMMEDIATE 'DROP SEQUENCE ' || s.sequence_name;
  END LOOP;
END;
/


-- ---------------------------------------------------------------------
-- 1. MEMBER  (Member.java abstract + Admin 상속, MemberRole/MemberStatus enum)
--    단일 테이블 상속: Admin 전용 테이블 없음, role 컬럼(M/A)으로 구분
-- ---------------------------------------------------------------------
CREATE TABLE MEMBER (
    MEMBER_ID    NUMBER(19)        NOT NULL,
    USER_ID      VARCHAR2(50)      NOT NULL,
    PASSWORD     VARCHAR2(255)     NOT NULL,
    NAME         VARCHAR2(50 CHAR) NOT NULL,
    BIRTH        DATE,
    PHONE        VARCHAR2(20),
    EMAIL        VARCHAR2(100),
    ROLE         CHAR(1)      NOT NULL,
    STATUS       CHAR(1)      NOT NULL,
    CREATED_AT   DATE              DEFAULT SYSDATE NOT NULL,
    CONSTRAINT PK_MEMBER PRIMARY KEY (MEMBER_ID),
    CONSTRAINT UQ_MEMBER_USER_ID UNIQUE (USER_ID),
    CONSTRAINT CK_MEMBER_ROLE   CHECK (ROLE IN ('M','A')),
    CONSTRAINT CK_MEMBER_STATUS CHECK (STATUS IN ('A','W'))
);

CREATE SEQUENCE SEQ_MEMBER START WITH 1 INCREMENT BY 1 NOCACHE;


-- ---------------------------------------------------------------------
-- 2. DEPARTMENT  (Department.java)
-- ---------------------------------------------------------------------
CREATE TABLE DEPARTMENT (
    DEPT_ID      NUMBER(19)        NOT NULL,
    DEPT_NAME    VARCHAR2(50 CHAR) NOT NULL,
    CONSTRAINT PK_DEPARTMENT PRIMARY KEY (DEPT_ID),
    CONSTRAINT UQ_DEPARTMENT_NAME UNIQUE (DEPT_NAME)
);

CREATE SEQUENCE SEQ_DEPARTMENT START WITH 1 INCREMENT BY 1 NOCACHE;


-- ---------------------------------------------------------------------
-- 3. DOCTOR  (Doctor.java)
-- ---------------------------------------------------------------------
CREATE TABLE DOCTOR (
    DOCTOR_ID    NUMBER(19)         NOT NULL,
    NAME         VARCHAR2(50 CHAR)  NOT NULL,
    SPECIALTY    VARCHAR2(100 CHAR),
    BIO          CLOB,
    DEPT_ID      NUMBER(19)         NOT NULL,
    CONSTRAINT PK_DOCTOR PRIMARY KEY (DOCTOR_ID),
    CONSTRAINT FK_DOCTOR_DEPT FOREIGN KEY (DEPT_ID) REFERENCES DEPARTMENT (DEPT_ID)
);

CREATE INDEX IDX_DOCTOR_DEPT ON DOCTOR (DEPT_ID);

CREATE SEQUENCE SEQ_DOCTOR START WITH 1 INCREMENT BY 1 NOCACHE;


-- ---------------------------------------------------------------------
-- 4. DOCTOR_SCHEDULE  (DoctorSchedule.java)
--    dayOfWeek/startTime/endTime은 String("HH:mm") - CHECK로 형식 강제
-- ---------------------------------------------------------------------
CREATE TABLE DOCTOR_SCHEDULE (
    SCHEDULE_ID  NUMBER(19)    NOT NULL,
    DOCTOR_ID    NUMBER(19)    NOT NULL,
    DAY_OF_WEEK  CHAR(1)       NOT NULL,
    START_TIME   VARCHAR2(5),
    END_TIME     VARCHAR2(5),
    DAY_OFF      NUMBER(1)     DEFAULT 0 NOT NULL,
    CONSTRAINT PK_DOCTOR_SCHEDULE PRIMARY KEY (SCHEDULE_ID),
    CONSTRAINT FK_SCH_DOCTOR FOREIGN KEY (DOCTOR_ID) REFERENCES DOCTOR (DOCTOR_ID),
    CONSTRAINT CK_SCH_DAY CHECK (DAY_OF_WEEK IN
        ('1','2','3','4','5','6','7')),
    CONSTRAINT CK_SCH_DAY_OFF CHECK (DAY_OFF IN (0,1)),
    CONSTRAINT CK_SCH_START_FMT CHECK (REGEXP_LIKE(START_TIME, '^([01][0-9]|2[0-3]):[0-5][0-9]$')),
    CONSTRAINT CK_SCH_END_FMT   CHECK (REGEXP_LIKE(END_TIME,   '^([01][0-9]|2[0-3]):[0-5][0-9]$')),
    CONSTRAINT CK_SCH_TIME_ORDER CHECK (DAY_OFF = 1 OR START_TIME < END_TIME),
    CONSTRAINT CK_SCH_TIME_REQUIRED CHECK (
        DAY_OFF = 1 OR (START_TIME IS NOT NULL AND END_TIME IS NOT NULL)
    )
);

CREATE INDEX IDX_SCH_DOCTOR ON DOCTOR_SCHEDULE (DOCTOR_ID);

CREATE SEQUENCE SEQ_DOCTOR_SCHEDULE START WITH 1 INCREMENT BY 1 NOCACHE;


-- ---------------------------------------------------------------------
-- 5. DOCTOR_OFF  (DoctorOff.java, OffSource enum)
-- ---------------------------------------------------------------------
CREATE TABLE DOCTOR_OFF (
    OFF_ID       NUMBER(19)         NOT NULL,
    DOCTOR_ID    NUMBER(19)         NOT NULL,
    OFF_DATE     DATE               NOT NULL,
    REASON       VARCHAR2(200 CHAR),
    SOURCE       CHAR(1)           NOT NULL,
    CONSTRAINT PK_DOCTOR_OFF PRIMARY KEY (OFF_ID),
    CONSTRAINT FK_OFF_DOCTOR FOREIGN KEY (DOCTOR_ID) REFERENCES DOCTOR (DOCTOR_ID),
    CONSTRAINT UQ_OFF_DOCTOR_DATE UNIQUE (DOCTOR_ID, OFF_DATE),
    CONSTRAINT CK_OFF_SOURCE CHECK (SOURCE IN ('M','H'))
);

CREATE SEQUENCE SEQ_DOCTOR_OFF START WITH 1 INCREMENT BY 1 NOCACHE;


-- ---------------------------------------------------------------------
-- 6. SYMPTOM_KEYWORD  (SymptomKeyword.java)
-- ---------------------------------------------------------------------
CREATE TABLE SYMPTOM_KEYWORD (
    KEYWORD_ID   NUMBER(19)        NOT NULL,
    KEYWORD      VARCHAR2(50 CHAR) NOT NULL,
    WEIGHT       NUMBER(3)         NOT NULL,
    DEPT_ID      NUMBER(19)        NOT NULL,
    CONSTRAINT PK_SYMPTOM_KEYWORD PRIMARY KEY (KEYWORD_ID),
    CONSTRAINT FK_SYM_DEPT FOREIGN KEY (DEPT_ID) REFERENCES DEPARTMENT (DEPT_ID)
);

CREATE INDEX IDX_SYM_DEPT ON SYMPTOM_KEYWORD (DEPT_ID);

CREATE SEQUENCE SEQ_SYMPTOM_KEYWORD START WITH 1 INCREMENT BY 1 NOCACHE;


-- ---------------------------------------------------------------------
-- 7. RESERVATION  (Reservation.java, ReservationStatus enum)
--    member/doctor/doctorSchedule 참조 -> FK 3개 (모두 NOT NULL)
-- ---------------------------------------------------------------------
CREATE TABLE RESERVATION (
    RESERVATION_ID    NUMBER(19)   NOT NULL,
    MEMBER_ID         NUMBER(19)   NOT NULL,
    DOCTOR_ID         NUMBER(19)   NOT NULL,
    SCHEDULE_ID       NUMBER(19)   NOT NULL,
    RESERVATION_DATE  DATE         NOT NULL,
    STATUS            CHAR(1)      NOT NULL,
    CREATED_AT        DATE         DEFAULT SYSDATE NOT NULL,
    CANCELED_AT       DATE,
    CONSTRAINT PK_RESERVATION PRIMARY KEY (RESERVATION_ID),
    CONSTRAINT FK_RES_MEMBER FOREIGN KEY (MEMBER_ID) REFERENCES MEMBER (MEMBER_ID),
    CONSTRAINT FK_RES_DOCTOR FOREIGN KEY (DOCTOR_ID) REFERENCES DOCTOR (DOCTOR_ID),
    CONSTRAINT FK_RES_SCHEDULE FOREIGN KEY (SCHEDULE_ID) REFERENCES DOCTOR_SCHEDULE (SCHEDULE_ID),
    CONSTRAINT CK_RES_STATUS CHECK (STATUS IN ('R','C','X','N'))
);

CREATE INDEX IDX_RES_DOCTOR_DATE ON RESERVATION (DOCTOR_ID, RESERVATION_DATE);
CREATE INDEX IDX_RES_MEMBER_DATE ON RESERVATION (MEMBER_ID, RESERVATION_DATE);

-- 중복 예약 방지: 취소(X) 상태는 제외하고, 같은 의사/같은 시간대 또는
-- 같은 회원/같은 시간대의 '살아있는' 예약이 둘 이상 생기지 않도록 함.
CREATE UNIQUE INDEX UX_RESERVATION_ACTIVE ON RESERVATION (
    CASE WHEN STATUS <> 'X' THEN DOCTOR_ID END,
    CASE WHEN STATUS <> 'X' THEN RESERVATION_DATE END);

CREATE UNIQUE INDEX UX_RESERVATION_MEMBER_ACTIVE ON RESERVATION (
    CASE WHEN STATUS <> 'X' THEN MEMBER_ID END,
    CASE WHEN STATUS <> 'X' THEN RESERVATION_DATE END);

CREATE SEQUENCE SEQ_RESERVATION START WITH 1 INCREMENT BY 1 NOCACHE;


-- ---------------------------------------------------------------------
-- 8. MEDICAL_RECORD  (MedicalRecord.java)
--    reservation 1:1 -> RESERVATION_ID UNIQUE NOT NULL
-- ---------------------------------------------------------------------
CREATE TABLE MEDICAL_RECORD (
    RECORD_ID      NUMBER(19)         NOT NULL,
    RESERVATION_ID NUMBER(19)         NOT NULL,
    DIAGNOSIS      CLOB,
    PRESCRIPTION   CLOB,
    RECORD_DATE    DATE,
    CONSTRAINT PK_MEDICAL_RECORD PRIMARY KEY (RECORD_ID),
    CONSTRAINT FK_RECORD_RESERVATION FOREIGN KEY (RESERVATION_ID) REFERENCES RESERVATION (RESERVATION_ID),
    CONSTRAINT UQ_RECORD_RESERVATION UNIQUE (RESERVATION_ID)
);

CREATE SEQUENCE SEQ_MEDICAL_RECORD START WITH 1 INCREMENT BY 1 NOCACHE;


-- =====================================================================
-- 검증용 쿼리 (실행 전 참고, 평소엔 주석 처리)
-- =====================================================================
-- SELECT table_name FROM user_tables ORDER BY table_name;
-- SELECT constraint_name, constraint_type, table_name FROM user_constraints
--   WHERE table_name IN ('MEMBER','DEPARTMENT','DOCTOR','DOCTOR_SCHEDULE',
--                         'DOCTOR_OFF','SYMPTOM_KEYWORD','RESERVATION','MEDICAL_RECORD')
--   ORDER BY table_name, constraint_type;
