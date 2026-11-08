-- V9: Add combat encounter table for Captain's Log combat system
-- Supports physical and starship combat with hit tracking and round-based resolution.

CREATE TABLE cl_combats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mission_id BIGINT NOT NULL,
    character_id BIGINT NOT NULL,
    opponent_name VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    opponent_scale VARCHAR(15),
    character_hits INT NOT NULL,
    character_max_hits INT NOT NULL,
    opponent_hits INT NOT NULL,
    opponent_max_hits INT NOT NULL,
    current_round INT NOT NULL DEFAULT 0,
    status VARCHAR(15) NOT NULL,
    character_attribute VARCHAR(20),
    character_discipline VARCHAR(20),
    ship_system VARCHAR(20),
    ship_department VARCHAR(20),
    description TEXT,
    result_summary TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP NULL,
    CONSTRAINT fk_cl_combat_mission FOREIGN KEY (mission_id) REFERENCES cl_missions(id),
    CONSTRAINT fk_cl_combat_character FOREIGN KEY (character_id) REFERENCES cl_characters(id),
    INDEX idx_cl_combat_mission (mission_id)
);
