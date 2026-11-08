-- V8: Add momentum, threat, and narrative beat tracking to cl_missions
-- Supports the 2d20 Momentum/Threat resource system and the 15-beat three-act mission structure.
-- New columns: momentum, threat, current_beat.

ALTER TABLE cl_missions ADD COLUMN momentum INT NOT NULL DEFAULT 0;
ALTER TABLE cl_missions ADD COLUMN threat INT NOT NULL DEFAULT 0;
ALTER TABLE cl_missions ADD COLUMN current_beat INT NOT NULL DEFAULT 1;
