-- V2: Complete schema rebuild to accommodate all JPA entities
-- Drops all existing tables and recreates them with correct structure
-- This migration adds support for Army, Navy, Facilities components,
-- celestial bodies (moons, asteroids, gas giants, nebulas, black holes, comets),
-- space stations, and all missing columns
--
-- WARNING: This is a DESTRUCTIVE migration — it drops ALL tables before recreating them.
-- It is intended as a one-time reset during development only. DO NOT use this pattern
-- for future migrations in production. Future migrations should be additive (ALTER TABLE,
-- CREATE TABLE) to preserve existing data.

-- Drop tables in reverse dependency order
DROP TABLE IF EXISTS planet_strategic_deposits;
DROP TABLE IF EXISTS asteroids;
DROP TABLE IF EXISTS comets;
DROP TABLE IF EXISTS black_holes;
DROP TABLE IF EXISTS nebulas;
DROP TABLE IF EXISTS gas_giants;
DROP TABLE IF EXISTS asteroid_belts;
DROP TABLE IF EXISTS moons;
DROP TABLE IF EXISTS space_stations;
DROP TABLE IF EXISTS planets;
DROP TABLE IF EXISTS stars;
DROP TABLE IF EXISTS solar_systems;
DROP TABLE IF EXISTS game_sessions_persisted;
DROP TABLE IF EXISTS galaxies;
DROP TABLE IF EXISTS devices;
DROP TABLE IF EXISTS players;
DROP TABLE IF EXISTS users;

-- Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    galaxy_name VARCHAR(255) NOT NULL DEFAULT 'Milky Way'
);

-- Players table with admin flag
CREATE TABLE players (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    login_pin VARCHAR(6) NOT NULL UNIQUE,
    empire_name VARCHAR(255) NOT NULL,
    government_type VARCHAR(20) NOT NULL,
    ethos VARCHAR(20) NOT NULL,
    user_id BIGINT,
    is_admin BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_player_email UNIQUE (email),
    CONSTRAINT uk_player_login_pin UNIQUE (login_pin),
    CONSTRAINT fk_player_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_player_email ON players(email);
CREATE INDEX idx_player_login_pin ON players(login_pin);
CREATE INDEX idx_player_user_id ON players(user_id);

-- Devices table
CREATE TABLE devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    location VARCHAR(255)
);

-- Galaxies table
CREATE TABLE galaxies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    total_sectors INT NOT NULL DEFAULT 100,
    hyperlane_density DOUBLE NOT NULL DEFAULT 1.0
);

-- Solar systems table with sector/subsector coordinates
CREATE TABLE solar_systems (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    galaxy_id BIGINT NOT NULL,
    sector_row INT NOT NULL,
    sector_col INT NOT NULL,
    sub_sector_row INT NOT NULL,
    sub_sector_col INT NOT NULL,
    pos_x INT NOT NULL,
    pos_y INT NOT NULL,
    pos_z INT NOT NULL,
    CONSTRAINT fk_solar_system_galaxy FOREIGN KEY (galaxy_id) REFERENCES galaxies(id)
);

CREATE INDEX idx_solar_system_galaxy ON solar_systems(galaxy_id);
CREATE INDEX idx_solar_system_sector ON solar_systems(sector_row, sector_col);
CREATE INDEX idx_solar_system_position ON solar_systems(pos_x, pos_y, pos_z);

-- Stars table
CREATE TABLE stars (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    solar_system_id BIGINT NOT NULL,
    star_size VARCHAR(10) NOT NULL,
    star_type VARCHAR(10) NOT NULL,
    radius INT NOT NULL,
    energy_yield INT NOT NULL,
    station_slots INT NOT NULL,
    occupied_slots INT NOT NULL,
    radiation_damage DOUBLE NOT NULL,
    gravity_well_bonus DOUBLE NOT NULL,
    CONSTRAINT fk_star_solar_system FOREIGN KEY (solar_system_id) REFERENCES solar_systems(id)
);

CREATE INDEX idx_star_solar_system ON stars(solar_system_id);
CREATE INDEX idx_star_type ON stars(star_type);
CREATE INDEX idx_star_size ON stars(star_size);

-- Planets table with full resource production stats
CREATE TABLE planets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    solar_system_id BIGINT NOT NULL,
    planet_size VARCHAR(10) NOT NULL,
    mineral_quality VARCHAR(15) NOT NULL,
    radius INT NOT NULL,
    mineral_production INT NOT NULL,
    energy_production INT NOT NULL,
    food_production INT NOT NULL,
    research_production INT NOT NULL,
    influence_production INT NOT NULL,
    current_population INT NOT NULL,
    max_population INT NOT NULL,
    social_stat INT NOT NULL,
    intelligence_stat INT NOT NULL,
    diligence_stat INT NOT NULL,
    resolve_stat INT NOT NULL,
    owner_empire_id INT,
    CONSTRAINT fk_planet_solar_system FOREIGN KEY (solar_system_id) REFERENCES solar_systems(id)
);

CREATE INDEX idx_planet_solar_system ON planets(solar_system_id);
CREATE INDEX idx_planet_size ON planets(planet_size);
CREATE INDEX idx_planet_mineral_quality ON planets(mineral_quality);

-- Planet strategic deposits (element collection)
CREATE TABLE planet_strategic_deposits (
    planet_id BIGINT NOT NULL,
    resource VARCHAR(15) NOT NULL,
    CONSTRAINT fk_planet_deposits_planet FOREIGN KEY (planet_id) REFERENCES planets(id)
);

-- Space stations table
CREATE TABLE space_stations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    solar_system_id BIGINT NOT NULL,
    station_type VARCHAR(15) NOT NULL,
    hex_x INT NOT NULL,
    hex_y INT NOT NULL,
    hex_z INT NOT NULL,
    hull_points INT NOT NULL,
    current_hull INT NOT NULL,
    shield_points INT NOT NULL,
    current_shield INT NOT NULL,
    component_slots INT NOT NULL,
    occupied_slots INT NOT NULL,
    damage INT NOT NULL,
    CONSTRAINT fk_space_station_solar_system FOREIGN KEY (solar_system_id) REFERENCES solar_systems(id)
);

CREATE INDEX idx_space_station_solar_system ON space_stations(solar_system_id);

-- Moons table
CREATE TABLE moons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    solar_system_id BIGINT NOT NULL,
    parent_planet_id BIGINT,
    moon_size VARCHAR(10) NOT NULL,
    mineral_yield INT NOT NULL,
    outpost_slots INT NOT NULL,
    strategic_deposit VARCHAR(15),
    CONSTRAINT fk_moon_solar_system FOREIGN KEY (solar_system_id) REFERENCES solar_systems(id),
    CONSTRAINT fk_moon_parent_planet FOREIGN KEY (parent_planet_id) REFERENCES planets(id)
);

CREATE INDEX idx_moon_solar_system ON moons(solar_system_id);
CREATE INDEX idx_moon_parent_planet ON moons(parent_planet_id);

-- Asteroid belts table
CREATE TABLE asteroid_belts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    solar_system_id BIGINT NOT NULL,
    radius INT NOT NULL,
    mineral_yield INT NOT NULL,
    station_slots INT NOT NULL,
    movement_penalty DOUBLE NOT NULL,
    strategic_deposit VARCHAR(15),
    CONSTRAINT fk_asteroid_belt_solar_system FOREIGN KEY (solar_system_id) REFERENCES solar_systems(id)
);

CREATE INDEX idx_asteroid_belt_solar_system ON asteroid_belts(solar_system_id);

-- Gas giants table
CREATE TABLE gas_giants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    solar_system_id BIGINT NOT NULL,
    radius INT NOT NULL,
    energy_yield INT NOT NULL,
    station_slots INT NOT NULL,
    gravitational_pull DOUBLE NOT NULL,
    slingshot_bonus DOUBLE NOT NULL,
    CONSTRAINT fk_gas_giant_solar_system FOREIGN KEY (solar_system_id) REFERENCES solar_systems(id)
);

CREATE INDEX idx_gas_giant_solar_system ON gas_giants(solar_system_id);

-- Nebulas table
CREATE TABLE nebulas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    solar_system_id BIGINT NOT NULL,
    nebula_size VARCHAR(10) NOT NULL,
    radius INT NOT NULL,
    elerium_yield INT NOT NULL,
    station_slots INT NOT NULL,
    sensor_jamming DOUBLE NOT NULL,
    cloaking_bonus DOUBLE NOT NULL,
    CONSTRAINT fk_nebula_solar_system FOREIGN KEY (solar_system_id) REFERENCES solar_systems(id)
);

CREATE INDEX idx_nebula_solar_system ON nebulas(solar_system_id);

-- Black holes table
CREATE TABLE black_holes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    solar_system_id BIGINT NOT NULL,
    black_hole_size VARCHAR(10) NOT NULL,
    radius INT NOT NULL,
    anti_matter_yield INT NOT NULL,
    station_slots INT NOT NULL,
    gravitational_pull DOUBLE NOT NULL,
    destruction_chance DOUBLE NOT NULL,
    CONSTRAINT fk_black_hole_solar_system FOREIGN KEY (solar_system_id) REFERENCES solar_systems(id)
);

CREATE INDEX idx_black_hole_solar_system ON black_holes(solar_system_id);

-- Comets table
CREATE TABLE comets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    solar_system_id BIGINT NOT NULL,
    comet_size VARCHAR(10) NOT NULL,
    resource_bonus INT NOT NULL,
    duration_turns INT NOT NULL,
    remaining_turns INT NOT NULL,
    harvested BOOLEAN NOT NULL DEFAULT FALSE,
    strategic_deposit VARCHAR(15),
    CONSTRAINT fk_comet_solar_system FOREIGN KEY (solar_system_id) REFERENCES solar_systems(id)
);

CREATE INDEX idx_comet_solar_system ON comets(solar_system_id);

-- Asteroids table
CREATE TABLE asteroids (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    solar_system_id BIGINT NOT NULL,
    asteroid_size VARCHAR(15) NOT NULL,
    mineral_yield INT NOT NULL,
    outpost_slots INT NOT NULL,
    movement_penalty DOUBLE NOT NULL,
    strategic_deposit VARCHAR(15),
    CONSTRAINT fk_asteroid_solar_system FOREIGN KEY (solar_system_id) REFERENCES solar_systems(id)
);

CREATE INDEX idx_asteroid_solar_system ON asteroids(solar_system_id);
CREATE INDEX idx_asteroid_size ON asteroids(asteroid_size);

-- Persisted game sessions table
CREATE TABLE game_sessions_persisted (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    session_name VARCHAR(255) NOT NULL,
    game_state LONGTEXT,
    player_ids VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_activity_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_game_session_last_activity ON game_sessions_persisted(last_activity_at);
