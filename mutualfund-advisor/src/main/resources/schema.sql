-- GROUP A: CORE IDENTITY
CREATE TABLE IF NOT EXISTS users (
    user_id     BIGSERIAL PRIMARY KEY,
    full_name   VARCHAR(150) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    mobile      VARCHAR(15),
    role        VARCHAR(30)  NOT NULL DEFAULT 'INVESTOR',
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS investor_profile (
    investor_id            BIGSERIAL PRIMARY KEY,
    user_id                BIGINT      NOT NULL REFERENCES users(user_id),
    age                    INT         NOT NULL,
    annual_income          DECIMAL(15,2),
    occupation             VARCHAR(100),
    investment_goal        VARCHAR(50),
    investment_horizon     VARCHAR(30),
    liquidity_preference   VARCHAR(30),
    investment_experience  VARCHAR(30),
    created_at             TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- GROUP A: RISK ENGINE
CREATE TABLE IF NOT EXISTS risk_question (
    question_id   BIGSERIAL PRIMARY KEY,
    question_text TEXT        NOT NULL UNIQUE,
    category      VARCHAR(50),
    weight        INT         NOT NULL DEFAULT 1,
    active_flag   BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS risk_assessment (
    assessment_id BIGSERIAL PRIMARY KEY,
    investor_id   BIGINT      NOT NULL REFERENCES investor_profile(investor_id),
    total_score   INT         NOT NULL,
    risk_level    VARCHAR(20) NOT NULL,
    assessed_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    remarks       VARCHAR(255),
    model_version VARCHAR(20) NOT NULL DEFAULT 'v1'
);

CREATE TABLE IF NOT EXISTS risk_assessment_answer (
    answer_id       BIGSERIAL PRIMARY KEY,
    assessment_id   BIGINT      NOT NULL REFERENCES risk_assessment(assessment_id),
    question_id     BIGINT      NOT NULL REFERENCES risk_question(question_id),
    selected_option VARCHAR(255),
    option_score    INT         NOT NULL,
    answered_at     TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- GROUP B: FUND INTELLIGENCE
CREATE TABLE IF NOT EXISTS sector (
    sector_id   BIGSERIAL PRIMARY KEY,
    sector_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS mutual_fund (
    fund_id            BIGSERIAL PRIMARY KEY,
    fund_name          VARCHAR(200) NOT NULL UNIQUE,
    amc_name           VARCHAR(150) NOT NULL,
    category           VARCHAR(80),
    risk_level         VARCHAR(20)  NOT NULL,
    expense_ratio      DECIMAL(5,3),
    return_1y          DECIMAL(6,2),
    return_3y          DECIMAL(6,2),
    minimum_investment DECIMAL(12,2),
    investment_horizon VARCHAR(30),
    volatility_score   DECIMAL(4,2) DEFAULT 5.0,
    fund_status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS fund_sector_allocation (
    allocation_id         BIGSERIAL PRIMARY KEY,
    fund_id               BIGINT       NOT NULL REFERENCES mutual_fund(fund_id),
    sector_id             BIGINT       NOT NULL REFERENCES sector(sector_id),
    allocation_percentage DECIMAL(5,2) NOT NULL,
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE(fund_id, sector_id)
);

CREATE TABLE IF NOT EXISTS investor_holding (
    holding_id     BIGSERIAL PRIMARY KEY,
    investor_id    BIGINT      NOT NULL REFERENCES investor_profile(investor_id),
    fund_id        BIGINT      NOT NULL REFERENCES mutual_fund(fund_id),
    holding_status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
    UNIQUE(investor_id, fund_id)
);

-- GROUP C: MARKET EVENTS
CREATE TABLE IF NOT EXISTS market_event (
    event_id          BIGSERIAL PRIMARY KEY,
    event_title       VARCHAR(255) NOT NULL UNIQUE,
    event_description TEXT,
    impact_type       VARCHAR(20)  NOT NULL,
    credibility_score DECIMAL(3,1) DEFAULT 8.0,
    impact_duration   VARCHAR(20)  DEFAULT 'MEDIUM_TERM',
    data_quality_flag VARCHAR(20)  DEFAULT 'VERIFIED',
    source_url        VARCHAR(500),
    event_date        TIMESTAMP    NOT NULL,
    expiry_date       TIMESTAMP,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS market_event_sector (
    id              BIGSERIAL PRIMARY KEY,
    event_id        BIGINT       NOT NULL REFERENCES market_event(event_id),
    sector_id       BIGINT       NOT NULL REFERENCES sector(sector_id),
    impact_severity DECIMAL(4,2) NOT NULL,
    UNIQUE(event_id, sector_id)
);

-- GROUP D: INTELLIGENCE OUTPUT
CREATE TABLE IF NOT EXISTS recommendation (
    recommendation_id     BIGSERIAL PRIMARY KEY,
    investor_id           BIGINT       NOT NULL REFERENCES investor_profile(investor_id),
    fund_id               BIGINT       NOT NULL REFERENCES mutual_fund(fund_id),
    base_match_score      DECIMAL(5,2) NOT NULL,
    market_adjusted_score DECIMAL(5,2) NOT NULL,
    confidence_score      DECIMAL(5,2),
    recommendation_reason TEXT,
    explanation_data      JSONB,
    recommendation_status VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    model_version         VARCHAR(20)  NOT NULL DEFAULT 'v1',
    generated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS user_alert (
    alert_id           BIGSERIAL PRIMARY KEY,
    user_id            BIGINT      NOT NULL REFERENCES users(user_id),
    recommendation_id  BIGINT      NOT NULL REFERENCES recommendation(recommendation_id),
    event_id           BIGINT      NOT NULL REFERENCES market_event(event_id),
    alert_type         VARCHAR(50) NOT NULL,
    alert_message      TEXT        NOT NULL,
    severity           VARCHAR(20) NOT NULL,
    is_read            BOOLEAN     NOT NULL DEFAULT FALSE,
    last_alert_sent_at TIMESTAMP,
    created_at         TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- GROUP E: AI / RAG LAYER
CREATE TABLE IF NOT EXISTS chat_history (
    chat_id       BIGSERIAL PRIMARY KEY,
    user_id       BIGINT      NOT NULL REFERENCES users(user_id),
    investor_id   BIGINT      REFERENCES investor_profile(investor_id),
    question      TEXT        NOT NULL,
    answer        TEXT,
    response_type VARCHAR(50),
    model_name    VARCHAR(100),
    asked_at      TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS fund_document (
    document_id     BIGSERIAL PRIMARY KEY,
    fund_id         BIGINT      NOT NULL REFERENCES mutual_fund(fund_id),
    doc_type        VARCHAR(50),
    content_summary TEXT
);

CREATE TABLE IF NOT EXISTS fund_document_chunk (
    chunk_id         BIGSERIAL PRIMARY KEY,
    document_id      BIGINT NOT NULL REFERENCES fund_document(document_id),
    chunk_text       TEXT   NOT NULL,
    embedding_vector TEXT
);