CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100),
    email VARCHAR(100),
    mobile VARCHAR(20),
    role VARCHAR(50),
    status VARCHAR(20),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE investor_profile (
    investor_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    age INT,
    annual_income DECIMAL(12,2),
    occupation VARCHAR(100),
    investment_goal VARCHAR(100),
    investment_horizon VARCHAR(50),
    liquidity_preference VARCHAR(50),
    investment_experience VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE risk_question (
    question_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_text VARCHAR(255),
    category VARCHAR(50),
    weight INT,
    active_flag BOOLEAN,
    created_at TIMESTAMP
);

CREATE TABLE risk_assessment (
    assessment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    investor_id BIGINT,
    total_score INT,
    risk_level VARCHAR(20),
    assessed_at TIMESTAMP,
    remarks VARCHAR(255),
    model_version VARCHAR(50),
    FOREIGN KEY (investor_id) REFERENCES investor_profile(investor_id)
);

CREATE TABLE risk_assessment_answer (
    answer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assessment_id BIGINT,
    question_id BIGINT,
    selected_option VARCHAR(100),
    option_score INT,
    answered_at TIMESTAMP,
    FOREIGN KEY (assessment_id) REFERENCES risk_assessment(assessment_id),
    FOREIGN KEY (question_id) REFERENCES risk_question(question_id)
);

CREATE TABLE mutual_fund (
    fund_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fund_name VARCHAR(150),
    amc_name VARCHAR(100),
    category VARCHAR(50),
    risk_level VARCHAR(20),
    expense_ratio DECIMAL(5,2),
    return_1y DECIMAL(6,2),
    return_3y DECIMAL(6,2),
    minimum_investment DECIMAL(10,2),
    investment_horizon VARCHAR(50),
    fund_status VARCHAR(20),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE recommendation (
    recommendation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    investor_id BIGINT,
    fund_id BIGINT,
    match_score DECIMAL(5,2),
    recommendation_reason TEXT,
    recommendation_status VARCHAR(20),
    model_version VARCHAR(50),
    generated_at TIMESTAMP,
    FOREIGN KEY (investor_id) REFERENCES investor_profile(investor_id),
    FOREIGN KEY (fund_id) REFERENCES mutual_fund(fund_id)
);

CREATE TABLE chat_history (
    chat_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    investor_id BIGINT,
    question TEXT,
    answer TEXT,
    response_type VARCHAR(50),
    model_name VARCHAR(50),
    asked_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (investor_id) REFERENCES investor_profile(investor_id)
);

CREATE TABLE fund_document (
    document_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fund_id BIGINT,
    doc_type VARCHAR(50),
    content_summary TEXT,
    FOREIGN KEY (fund_id) REFERENCES mutual_fund(fund_id)
);

CREATE TABLE fund_document_chunk (
    chunk_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT,
    chunk_text TEXT,
    embedding_vector TEXT,
    FOREIGN KEY (document_id) REFERENCES fund_document(document_id)
);