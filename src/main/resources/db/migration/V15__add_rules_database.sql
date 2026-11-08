-- Add rules database tables for the Captain's Log rulebook reference system
-- cl_rules_entries: stores individual rule text with metadata
-- cl_rules_tables: stores oracle/probability matrix table entries

CREATE TABLE cl_rules_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    chapter VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    page INT,
    rule_text TEXT,
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_cl_rules_category ON cl_rules_entries(category);
CREATE INDEX idx_cl_rules_chapter ON cl_rules_entries(chapter);
CREATE INDEX idx_cl_rules_title ON cl_rules_entries(title);

CREATE TABLE cl_rules_tables (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    chapter VARCHAR(100) NOT NULL,
    page INT,
    roll_value INT NOT NULL,
    result_label VARCHAR(200) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_cl_rules_table_name ON cl_rules_tables(table_name);
CREATE INDEX idx_cl_rules_table_category ON cl_rules_tables(category);
