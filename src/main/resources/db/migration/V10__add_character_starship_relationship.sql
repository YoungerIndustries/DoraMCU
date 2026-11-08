-- V10: Add starship assignment relationship to cl_characters
-- Links characters to their assigned starship via a foreign key.

ALTER TABLE cl_characters ADD COLUMN starship_id BIGINT;
ALTER TABLE cl_characters ADD CONSTRAINT fk_cl_character_starship FOREIGN KEY (starship_id) REFERENCES cl_starships(id);
