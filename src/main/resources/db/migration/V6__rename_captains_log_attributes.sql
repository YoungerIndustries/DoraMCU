-- V6: Rename Captain's Log attribute columns from D&D names to official Captain's Log names
-- Affected tables: cl_characters, cl_starships
-- Renames: dexterity→fitness, intelligence→reason, wisdom→insight, charisma→presence

-- Rename columns in cl_characters
ALTER TABLE cl_characters CHANGE COLUMN dexterity fitness INT NOT NULL DEFAULT 0;
ALTER TABLE cl_characters CHANGE COLUMN intelligence reason INT NOT NULL DEFAULT 0;
ALTER TABLE cl_characters CHANGE COLUMN wisdom insight INT NOT NULL DEFAULT 0;
ALTER TABLE cl_characters CHANGE COLUMN charisma presence INT NOT NULL DEFAULT 0;

-- Rename columns in cl_starships
ALTER TABLE cl_starships CHANGE COLUMN dexterity fitness INT NOT NULL DEFAULT 0;
ALTER TABLE cl_starships CHANGE COLUMN intelligence reason INT NOT NULL DEFAULT 0;
ALTER TABLE cl_starships CHANGE COLUMN wisdom insight INT NOT NULL DEFAULT 0;
ALTER TABLE cl_starships CHANGE COLUMN charisma presence INT NOT NULL DEFAULT 0;
