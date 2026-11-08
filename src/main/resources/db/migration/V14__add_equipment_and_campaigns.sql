-- Add equipment table for structured equipment management
CREATE TABLE cl_equipment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(30),
    boon_on VARCHAR(200),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_cl_equipment_character FOREIGN KEY (character_id) REFERENCES cl_characters(id)
);

CREATE INDEX idx_cl_equipment_character ON cl_equipment(character_id);

-- Add campaign table for campaign/arc tracking
CREATE TABLE cl_campaigns (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    ongoing_npcs TEXT,
    key_locations TEXT,
    plot_threads TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    concluded_at TIMESTAMP NULL,
    CONSTRAINT fk_cl_campaign_character FOREIGN KEY (character_id) REFERENCES cl_characters(id)
);

CREATE INDEX idx_cl_campaign_character ON cl_campaigns(character_id);

-- Add campaign_id foreign key to missions
ALTER TABLE cl_missions ADD COLUMN campaign_id BIGINT;
ALTER TABLE cl_missions ADD CONSTRAINT fk_cl_mission_campaign FOREIGN KEY (campaign_id) REFERENCES cl_campaigns(id);
