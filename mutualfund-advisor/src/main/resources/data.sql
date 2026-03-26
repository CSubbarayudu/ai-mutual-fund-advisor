-- 1. Insert Demo User
INSERT INTO users (full_name, email, mobile, role, status, created_at, updated_at)
VALUES ('Subbu', 'subbu@demo.com', '9876543210', 'USER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2. Insert Demo Investor Profile
INSERT INTO investor_profile (user_id, age, annual_income, occupation, investment_goal, investment_horizon, liquidity_preference, investment_experience, created_at, updated_at)
VALUES (1, 24, 800000.00, 'Software Engineer', 'Wealth Creation', 'Long Term (5+ Years)', 'Low', 'Beginner', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 3. Insert Risk Questions
INSERT INTO risk_question (question_text, category, weight, active_flag, created_at) VALUES
('How would you react if your portfolio drops by 20% in a month?', 'RISK_TOLERANCE', 10, TRUE, CURRENT_TIMESTAMP),
('What is the primary purpose of this investment?', 'GOAL', 5, TRUE, CURRENT_TIMESTAMP),
('How soon do you expect to need this money?', 'HORIZON', 8, TRUE, CURRENT_TIMESTAMP);

-- 4. Insert Mutual Funds (Master Data)
INSERT INTO mutual_fund (fund_name, amc_name, category, risk_level, expense_ratio, return_1y, return_3y, minimum_investment, investment_horizon, fund_status, created_at, updated_at) VALUES
('Nippon India Growth Fund', 'Nippon', 'Equity - Mid Cap', 'High', 1.15, 35.5, 22.4, 1000.00, 'Long Term', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HDFC Balanced Advantage Fund', 'HDFC', 'Hybrid', 'Moderate', 0.95, 18.2, 14.5, 500.00, 'Medium Term', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SBI Liquid Fund', 'SBI', 'Debt - Liquid', 'Low', 0.25, 7.1, 5.8, 500.00, 'Short Term', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);