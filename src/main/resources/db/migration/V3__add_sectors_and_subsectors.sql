-- V3: Add sectors and subsectors tables to store names persistently
-- Previously, sector and subsector names were generated from hardcoded arrays
-- every time a galaxy was loaded, so custom names were lost on restart.

CREATE TABLE IF NOT EXISTS sectors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    galaxy_id BIGINT NOT NULL,
    sector_row INT NOT NULL,
    sector_col INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    trade_type VARCHAR(50),
    CONSTRAINT fk_sector_galaxy FOREIGN KEY (galaxy_id) REFERENCES galaxies(id) ON DELETE CASCADE,
    CONSTRAINT uk_sector_galaxy_pos UNIQUE (galaxy_id, sector_row, sector_col)
);

CREATE INDEX idx_sector_galaxy ON sectors(galaxy_id);

CREATE TABLE IF NOT EXISTS subsectors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sector_id BIGINT NOT NULL,
    sub_sector_row INT NOT NULL,
    sub_sector_col INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT fk_subsector_sector FOREIGN KEY (sector_id) REFERENCES sectors(id) ON DELETE CASCADE,
    CONSTRAINT uk_subsector_sector_pos UNIQUE (sector_id, sub_sector_row, sub_sector_col)
);

CREATE INDEX idx_subsector_sector ON subsectors(sector_id);
