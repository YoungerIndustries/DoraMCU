-- V7: Add 2d20 task attempt fields to cl_log_entries
-- Supports the updated Modiphius 2d20 "count successes" dice mechanics.
-- New columns: attribute, difficulty, dice_rolled, boons, threats, successes,
--               momentum_generated, threat_generated, complication, complication_description.

ALTER TABLE cl_log_entries ADD COLUMN attribute VARCHAR(20);
ALTER TABLE cl_log_entries ADD COLUMN difficulty INT;
ALTER TABLE cl_log_entries ADD COLUMN dice_rolled VARCHAR(200);
ALTER TABLE cl_log_entries ADD COLUMN boons INT;
ALTER TABLE cl_log_entries ADD COLUMN threats INT;
ALTER TABLE cl_log_entries ADD COLUMN successes INT;
ALTER TABLE cl_log_entries ADD COLUMN momentum_generated INT;
ALTER TABLE cl_log_entries ADD COLUMN threat_generated INT;
ALTER TABLE cl_log_entries ADD COLUMN complication TINYINT(1);
ALTER TABLE cl_log_entries ADD COLUMN complication_description VARCHAR(500);
