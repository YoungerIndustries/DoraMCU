-- Add injury_state column to cl_characters for the injury/recovery system
ALTER TABLE cl_characters ADD COLUMN injury_state VARCHAR(20) DEFAULT 'HEALTHY';
