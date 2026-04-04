-- SECTORS
INSERT INTO sector (sector_name, description) VALUES
('IT',         'Information Technology'),
('BANKING',    'Banking and Financial Services'),
('PHARMA',     'Pharmaceuticals and Healthcare'),
('FMCG',       'Fast Moving Consumer Goods'),
('AUTO',       'Automobile and Auto Components'),
('ENERGY',     'Oil, Gas and Renewable Energy'),
('REALESTATE', 'Real Estate and Construction'),
('METALS',     'Metals and Mining')
ON CONFLICT (sector_name) DO NOTHING;

-- RISK QUESTIONS
INSERT INTO risk_question (question_text, category, weight, active_flag) VALUES
('How would you react if your investment dropped 20% in one month?',       'BEHAVIOR',   2, TRUE),
('What is your primary investment goal?',                                  'GOAL',       1, TRUE),
('How many years can you stay invested without withdrawing?',              'HORIZON',    2, TRUE),
('What percentage of monthly income do you save regularly?',               'FINANCIAL',  1, TRUE),
('Have you invested in equity mutual funds or stocks before?',             'EXPERIENCE', 1, TRUE),
('How stable is your current income source?',                              'FINANCIAL',  2, TRUE),
('What would you do if markets fall sharply for 6 continuous months?',    'BEHAVIOR',   2, TRUE),
('What is your current age group?',                                        'FINANCIAL',  1, TRUE)
ON CONFLICT DO NOTHING;

-- MUTUAL FUNDS
INSERT INTO mutual_fund (fund_name, amc_name, category, risk_level, expense_ratio, return_1y, return_3y, minimum_investment, investment_horizon, volatility_score, fund_status) VALUES
('HDFC Technology Fund',                    'HDFC AMC',             'EQUITY', 'HIGH',     0.950, 22.5, 18.3, 500.00,  'LONG',   8.5, 'ACTIVE'),
('SBI Banking & Financial Services Fund',   'SBI Funds Management', 'EQUITY', 'HIGH',     0.890, 15.2, 12.8, 500.00,  'LONG',   7.2, 'ACTIVE'),
('ICICI Prudential Pharma Healthcare Fund', 'ICICI Prudential AMC', 'EQUITY', 'MODERATE', 1.100, 18.7, 14.5, 1000.00, 'LONG',   6.8, 'ACTIVE'),
('Mirae Asset Large Cap Fund',              'Mirae Asset',          'EQUITY', 'MODERATE', 0.550, 14.3, 13.1, 1000.00, 'MEDIUM', 5.5, 'ACTIVE'),
('Axis Liquid Fund',                        'Axis AMC',             'DEBT',   'LOW',      0.180,  7.2,  6.8, 500.00,  'SHORT',  1.5, 'ACTIVE'),
('Nippon India Small Cap Fund',             'Nippon India',         'EQUITY', 'HIGH',     1.050, 31.2, 24.6, 100.00,  'LONG',   9.2, 'ACTIVE'),
('HDFC Hybrid Equity Fund',                 'HDFC AMC',             'HYBRID', 'MODERATE', 0.780, 16.8, 13.5, 500.00,  'MEDIUM', 5.8, 'ACTIVE'),
('SBI Magnum Gilt Fund',                    'SBI Funds Management', 'DEBT',   'LOW',      0.450,  8.1,  7.3, 5000.00, 'MEDIUM', 2.1, 'ACTIVE')
ON CONFLICT DO NOTHING;

-- FUND SECTOR ALLOCATIONS
INSERT INTO fund_sector_allocation (fund_id, sector_id, allocation_percentage) VALUES
(1,(SELECT sector_id FROM sector WHERE sector_name='IT'),       65.0),
(1,(SELECT sector_id FROM sector WHERE sector_name='FMCG'),     15.0),
(1,(SELECT sector_id FROM sector WHERE sector_name='AUTO'),     10.0),
(1,(SELECT sector_id FROM sector WHERE sector_name='METALS'),   10.0),
(2,(SELECT sector_id FROM sector WHERE sector_name='BANKING'),  75.0),
(2,(SELECT sector_id FROM sector WHERE sector_name='IT'),       15.0),
(2,(SELECT sector_id FROM sector WHERE sector_name='REALESTATE'),10.0),
(3,(SELECT sector_id FROM sector WHERE sector_name='PHARMA'),   70.0),
(3,(SELECT sector_id FROM sector WHERE sector_name='FMCG'),     20.0),
(3,(SELECT sector_id FROM sector WHERE sector_name='BANKING'),  10.0),
(4,(SELECT sector_id FROM sector WHERE sector_name='IT'),       25.0),
(4,(SELECT sector_id FROM sector WHERE sector_name='BANKING'),  30.0),
(4,(SELECT sector_id FROM sector WHERE sector_name='FMCG'),     20.0),
(4,(SELECT sector_id FROM sector WHERE sector_name='AUTO'),     15.0),
(4,(SELECT sector_id FROM sector WHERE sector_name='PHARMA'),   10.0),
(7,(SELECT sector_id FROM sector WHERE sector_name='BANKING'),  35.0),
(7,(SELECT sector_id FROM sector WHERE sector_name='IT'),       25.0),
(7,(SELECT sector_id FROM sector WHERE sector_name='PHARMA'),   20.0),
(7,(SELECT sector_id FROM sector WHERE sector_name='ENERGY'),   20.0)
ON CONFLICT (fund_id, sector_id) DO NOTHING;

-- SAMPLE MARKET EVENT
INSERT INTO market_event (event_title, event_description, impact_type, credibility_score, impact_duration, data_quality_flag, source_url, event_date, expiry_date) VALUES
('US Chip Export Ban Extended',
 'US extends semiconductor export restrictions affecting IT hardware imports.',
 'NEGATIVE', 9.0, 'MEDIUM_TERM', 'VERIFIED',
 'https://reuters.com/technology/us-chip-ban',
 NOW(), NOW() + INTERVAL '30 days')
ON CONFLICT DO NOTHING;

INSERT INTO market_event_sector (event_id, sector_id, impact_severity) VALUES
(1,(SELECT sector_id FROM sector WHERE sector_name='IT'),     8.5),
(1,(SELECT sector_id FROM sector WHERE sector_name='METALS'), 4.0)
ON CONFLICT (event_id, sector_id) DO NOTHING;