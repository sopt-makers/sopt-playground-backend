-- ===================================================
-- Member Question/Answer Feature Database Schema
-- ===================================================

-- 1. 질문 테이블 (member_question)
CREATE TABLE member_question (
    question_id BIGSERIAL PRIMARY KEY,
    receiver_id BIGINT NOT NULL,
    asker_id BIGINT,
    content TEXT NOT NULL CHECK (char_length(content) <= 2000),
    is_anonymous BOOLEAN NOT NULL,
    is_reported BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_member_question_receiver FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_member_question_asker FOREIGN KEY (asker_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 인덱스 생성
CREATE INDEX idx_member_question_receiver_id ON member_question(receiver_id);
CREATE INDEX idx_member_question_asker_id ON member_question(asker_id);
CREATE INDEX idx_member_question_created_at ON member_question(created_at DESC);
CREATE INDEX idx_member_question_receiver_created ON member_question(receiver_id, created_at DESC);

COMMENT ON TABLE member_question IS '회원 질문 테이블';
COMMENT ON COLUMN member_question.question_id IS '질문 ID (PK)';
COMMENT ON COLUMN member_question.receiver_id IS '질문을 받은 사용자 ID';
COMMENT ON COLUMN member_question.asker_id IS '질문 작성자 ID (익명인 경우 NULL)';
COMMENT ON COLUMN member_question.content IS '질문 내용 (최대 2,000자)';
COMMENT ON COLUMN member_question.is_anonymous IS '익명 여부';
COMMENT ON COLUMN member_question.is_reported IS '신고 여부';

-- ===================================================

-- 2. 답변 테이블 (member_answer)
CREATE TABLE member_answer (
    answer_id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL UNIQUE,
    content TEXT NOT NULL CHECK (char_length(content) <= 2000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_member_answer_question FOREIGN KEY (question_id) REFERENCES member_question(question_id) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX idx_member_answer_question_id ON member_answer(question_id);

COMMENT ON TABLE member_answer IS '질문 답변 테이블';
COMMENT ON COLUMN member_answer.answer_id IS '답변 ID (PK)';
COMMENT ON COLUMN member_answer.question_id IS '질문 ID (FK, UNIQUE)';
COMMENT ON COLUMN member_answer.content IS '답변 내용 (최대 2,000자)';

-- ===================================================

-- 3. 질문 반응 테이블 (나도 궁금해요)
CREATE TABLE question_reaction (
    reaction_id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_question_reaction_question FOREIGN KEY (question_id) REFERENCES member_question(question_id) ON DELETE CASCADE,
    CONSTRAINT fk_question_reaction_member FOREIGN KEY (member_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_question_reaction_question_member UNIQUE (question_id, member_id)
);

-- 인덱스 생성
CREATE INDEX idx_question_reaction_question_id ON question_reaction(question_id);
CREATE INDEX idx_question_reaction_member_id ON question_reaction(member_id);

COMMENT ON TABLE question_reaction IS '질문 반응 테이블 (나도 궁금해요)';
COMMENT ON COLUMN question_reaction.reaction_id IS '반응 ID (PK)';
COMMENT ON COLUMN question_reaction.question_id IS '질문 ID (FK)';
COMMENT ON COLUMN question_reaction.member_id IS '반응한 사용자 ID (FK)';

-- ===================================================

-- 4. 답변 반응 테이블 (도움돼요)
CREATE TABLE answer_reaction (
    reaction_id BIGSERIAL PRIMARY KEY,
    answer_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_answer_reaction_answer FOREIGN KEY (answer_id) REFERENCES member_answer(answer_id) ON DELETE CASCADE,
    CONSTRAINT fk_answer_reaction_member FOREIGN KEY (member_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_answer_reaction_answer_member UNIQUE (answer_id, member_id)
);

-- 인덱스 생성
CREATE INDEX idx_answer_reaction_answer_id ON answer_reaction(answer_id);
CREATE INDEX idx_answer_reaction_member_id ON answer_reaction(member_id);

COMMENT ON TABLE answer_reaction IS '답변 반응 테이블 (도움돼요)';
COMMENT ON COLUMN answer_reaction.reaction_id IS '반응 ID (PK)';
COMMENT ON COLUMN answer_reaction.answer_id IS '답변 ID (FK)';
COMMENT ON COLUMN answer_reaction.member_id IS '반응한 사용자 ID (FK)';

-- ===================================================

-- 5. 질문 신고 테이블
CREATE TABLE question_report (
    report_id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX idx_question_report_question_id ON question_report(question_id);
CREATE INDEX idx_question_report_reporter_id ON question_report(reporter_id);
CREATE INDEX idx_question_report_question_reporter ON question_report(question_id, reporter_id);

COMMENT ON TABLE question_report IS '질문 신고 테이블';
COMMENT ON COLUMN question_report.report_id IS '신고 ID (PK)';
COMMENT ON COLUMN question_report.question_id IS '신고된 질문 ID';
COMMENT ON COLUMN question_report.reporter_id IS '신고자 ID';
COMMENT ON COLUMN question_report.reason IS '신고 사유';

-- ===================================================
-- Trigger: updated_at 자동 업데이트
-- ===================================================

-- updated_at 자동 업데이트 함수
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 각 테이블에 트리거 적용
CREATE TRIGGER update_member_question_updated_at
    BEFORE UPDATE ON member_question
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_member_answer_updated_at
    BEFORE UPDATE ON member_answer
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_question_reaction_updated_at
    BEFORE UPDATE ON question_reaction
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_answer_reaction_updated_at
    BEFORE UPDATE ON answer_reaction
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ===================================================
-- Rollback SQL (필요 시 사용)
-- ===================================================

/*
-- 테이블 삭제 (역순)
DROP TABLE IF EXISTS question_report CASCADE;
DROP TABLE IF EXISTS answer_reaction CASCADE;
DROP TABLE IF EXISTS question_reaction CASCADE;
DROP TABLE IF EXISTS member_answer CASCADE;
DROP TABLE IF EXISTS member_question CASCADE;

-- 트리거 삭제
DROP TRIGGER IF EXISTS update_member_question_updated_at ON member_question;
DROP TRIGGER IF EXISTS update_member_answer_updated_at ON member_answer;
DROP TRIGGER IF EXISTS update_question_reaction_updated_at ON question_reaction;
DROP TRIGGER IF EXISTS update_answer_reaction_updated_at ON answer_reaction;

-- 함수 삭제
DROP FUNCTION IF EXISTS update_updated_at_column();
*/
