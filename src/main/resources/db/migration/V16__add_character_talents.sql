-- V16: Add character talents table for the Captain's Log Solo RPG
-- Character talents provide special abilities that modify gameplay

CREATE TABLE cl_character_talents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    mechanical_effect VARCHAR(200),
    source VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_cl_talent_character FOREIGN KEY (character_id) REFERENCES cl_characters(id)
);

CREATE INDEX idx_cl_character_talent_character ON cl_character_talents(character_id);
