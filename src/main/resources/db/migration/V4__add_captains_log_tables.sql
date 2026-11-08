-- V4: Add Captain's Log Solo RPG tables
-- Supports the Captain's Log: Solo Roleplaying Game (Star Trek Adventures / Modiphius 2d20 system).
-- Tables: cl_characters, cl_focuses, cl_values, cl_starships, cl_ship_talents, cl_missions, cl_log_entries

-- Characters with background, attributes, disciplines, and career tracking
CREATE TABLE IF NOT EXISTS cl_characters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    pronouns VARCHAR(50),
    trait1 VARCHAR(100),
    trait2 VARCHAR(100),
    trait3 VARCHAR(100),
    rank VARCHAR(20) NOT NULL,
    assignment VARCHAR(20) NOT NULL,
    environment VARCHAR(20) NOT NULL,
    outlook VARCHAR(20) NOT NULL,
    education VARCHAR(20) NOT NULL,
    dexterity INT NOT NULL DEFAULT 0,
    technical INT NOT NULL DEFAULT 0,
    strength INT NOT NULL DEFAULT 0,
    intelligence INT NOT NULL DEFAULT 0,
    wisdom INT NOT NULL DEFAULT 0,
    charisma INT NOT NULL DEFAULT 0,
    constitution INT NOT NULL DEFAULT 0,
    command INT NOT NULL DEFAULT 0,
    leadership INT NOT NULL DEFAULT 0,
    science INT NOT NULL DEFAULT 0,
    security INT NOT NULL DEFAULT 0,
    medicine INT NOT NULL DEFAULT 0,
    operations INT NOT NULL DEFAULT 0,
    equipment TEXT,
    career_events INT NOT NULL DEFAULT 0,
    missions_completed INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_cl_character_name ON cl_characters(name);

-- Focuses (specialties) within disciplines, linked to a character
CREATE TABLE IF NOT EXISTS cl_focuses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL,
    discipline VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT fk_focus_character FOREIGN KEY (character_id) REFERENCES cl_characters(id) ON DELETE CASCADE
);

CREATE INDEX idx_cl_focus_character ON cl_focuses(character_id);

-- Character values (moral compass), linked to a character
CREATE TABLE IF NOT EXISTS cl_values (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL,
    description VARCHAR(200) NOT NULL,
    used TINYINT(1) NOT NULL DEFAULT 0,
    challenged TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_value_character FOREIGN KEY (character_id) REFERENCES cl_characters(id) ON DELETE CASCADE
);

CREATE INDEX idx_cl_value_character ON cl_values(character_id);

-- Starships or stations with systems, stress trackers, departments, and attributes
CREATE TABLE IF NOT EXISTS cl_starships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    registry VARCHAR(50),
    trait1 VARCHAR(100),
    trait2 VARCHAR(100),
    trait3 VARCHAR(100),
    ship_class VARCHAR(50) NOT NULL,
    scale VARCHAR(20) NOT NULL,
    is_station TINYINT(1) NOT NULL DEFAULT 0,
    -- Starship systems (2d20 ratings)
    comms INT NOT NULL DEFAULT 0,
    computers INT NOT NULL DEFAULT 0,
    engines INT NOT NULL DEFAULT 0,
    sensors INT NOT NULL DEFAULT 0,
    structure INT NOT NULL DEFAULT 0,
    weapons INT NOT NULL DEFAULT 0,
    -- System stress trackers
    comms_stress INT NOT NULL DEFAULT 0,
    computers_stress INT NOT NULL DEFAULT 0,
    engines_stress INT NOT NULL DEFAULT 0,
    sensors_stress INT NOT NULL DEFAULT 0,
    structure_stress INT NOT NULL DEFAULT 0,
    weapons_stress INT NOT NULL DEFAULT 0,
    -- Departments
    command INT NOT NULL DEFAULT 0,
    science INT NOT NULL DEFAULT 0,
    security INT NOT NULL DEFAULT 0,
    engineering INT NOT NULL DEFAULT 0,
    medical INT NOT NULL DEFAULT 0,
    operations INT NOT NULL DEFAULT 0,
    -- Starship attributes
    dexterity INT NOT NULL DEFAULT 0,
    technical INT NOT NULL DEFAULT 0,
    strength INT NOT NULL DEFAULT 0,
    intelligence INT NOT NULL DEFAULT 0,
    wisdom INT NOT NULL DEFAULT 0,
    charisma INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_cl_starship_name ON cl_starships(name);

-- Ship talents (special abilities), linked to a starship
CREATE TABLE IF NOT EXISTS cl_ship_talents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    starship_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    CONSTRAINT fk_ship_talent_starship FOREIGN KEY (starship_id) REFERENCES cl_starships(id) ON DELETE CASCADE
);

CREATE INDEX idx_cl_ship_talent_starship ON cl_ship_talents(starship_id);

-- Missions following the 3-act structure, linked to a character
CREATE TABLE IF NOT EXISTS cl_missions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL,
    mission_number INT NOT NULL,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(20),
    status VARCHAR(20) NOT NULL,
    current_act INT NOT NULL DEFAULT 1,
    act1_log TEXT,
    act2_log TEXT,
    act3_log TEXT,
    summary TEXT,
    development TEXT,
    values_used VARCHAR(500),
    values_challenged VARCHAR(500),
    career_event TINYINT(1) NOT NULL DEFAULT 0,
    career_event_description TEXT,
    created_at DATETIME NOT NULL,
    completed_at DATETIME,
    CONSTRAINT fk_mission_character FOREIGN KEY (character_id) REFERENCES cl_characters(id) ON DELETE CASCADE
);

CREATE INDEX idx_cl_mission_character ON cl_missions(character_id);

-- Individual log entries within a mission (narrative, oracle queries, skill checks)
CREATE TABLE IF NOT EXISTS cl_log_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mission_id BIGINT NOT NULL,
    act INT NOT NULL,
    type VARCHAR(20) NOT NULL,
    text TEXT NOT NULL,
    discipline VARCHAR(20),
    target_number INT,
    roll_result INT,
    success TINYINT(1),
    oracle_question VARCHAR(500),
    oracle_answer VARCHAR(10),
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_log_entry_mission FOREIGN KEY (mission_id) REFERENCES cl_missions(id) ON DELETE CASCADE
);

CREATE INDEX idx_cl_log_entry_mission ON cl_log_entries(mission_id);
