-- V11: Add crew members table for Captain's Log crew management

CREATE TABLE cl_crew_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    species VARCHAR(50),
    role VARCHAR(100) NOT NULL,
    primary_discipline VARCHAR(20) NOT NULL,
    secondary_discipline VARCHAR(20),
    expertise_rating INT NOT NULL DEFAULT 1,
    relationship VARCHAR(100),
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    character_id BIGINT NOT NULL,
    CONSTRAINT fk_crew_character FOREIGN KEY (character_id) REFERENCES cl_characters(id)
);

CREATE INDEX idx_cl_crew_character ON cl_crew_members(character_id);
