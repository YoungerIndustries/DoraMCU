-- V12: Add experience points column for career progression system
ALTER TABLE cl_characters ADD COLUMN experience_points INT NOT NULL DEFAULT 0;
