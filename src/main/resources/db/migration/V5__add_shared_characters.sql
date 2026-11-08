-- V5: Add shared character database for cross-system character travel
-- Allows characters to travel between DoraMCU (Traveller) and Captain's Log systems.
-- Tables: shared_characters, shared_character_abilities, shared_character_skills

-- Main shared character table - the universal character identity
CREATE TABLE IF NOT EXISTS shared_characters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    pronouns VARCHAR(50),
    description TEXT,
    -- Universal attributes (common to both systems)
    dexterity INT NOT NULL DEFAULT 0,
    technical INT NOT NULL DEFAULT 0,
    strength INT NOT NULL DEFAULT 0,
    intelligence INT NOT NULL DEFAULT 0,
    wisdom INT NOT NULL DEFAULT 0,
    charisma INT NOT NULL DEFAULT 0,
    constitution INT NOT NULL DEFAULT 0,
    -- System-specific references
    origin_system VARCHAR(20) NOT NULL,
    cl_character_id BIGINT,
    traveller_character_id INT,
    -- Character metadata
    age INT,
    race VARCHAR(100),
    notes TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE INDEX idx_shared_char_name ON shared_characters(name);
CREATE INDEX idx_shared_char_origin ON shared_characters(origin_system);
CREATE INDEX idx_shared_char_cl_ref ON shared_characters(cl_character_id);
CREATE INDEX idx_shared_char_traveller_ref ON shared_characters(traveller_character_id);

-- Skills table - universal skill storage for both systems
CREATE TABLE IF NOT EXISTS shared_character_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    skill_level INT NOT NULL DEFAULT 1,
    system_source VARCHAR(20),
    description TEXT,
    CONSTRAINT fk_skill_character FOREIGN KEY (character_id) REFERENCES shared_characters(id) ON DELETE CASCADE
);

CREATE INDEX idx_shared_skill_character ON shared_character_skills(character_id);

-- Character travel log - tracks when a character travels between systems
CREATE TABLE IF NOT EXISTS shared_character_travel_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL,
    from_system VARCHAR(20) NOT NULL,
    to_system VARCHAR(20) NOT NULL,
    travel_notes TEXT,
    traveled_at DATETIME NOT NULL,
    CONSTRAINT fk_travel_character FOREIGN KEY (character_id) REFERENCES shared_characters(id) ON DELETE CASCADE
);

CREATE INDEX idx_travel_log_character ON shared_character_travel_log(character_id);
