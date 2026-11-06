-- V1: Initial schema migration
-- Creates all base tables for DoraMCU

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS players (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    login_pin VARCHAR(6) NOT NULL UNIQUE,
    empire_name VARCHAR(255) NOT NULL,
    government_type VARCHAR(20) NOT NULL,
    ethos VARCHAR(20) NOT NULL,
    user_id BIGINT,
    CONSTRAINT uk_player_email UNIQUE (email),
    CONSTRAINT uk_player_login_pin UNIQUE (login_pin),
    CONSTRAINT fk_player_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_player_email ON players(email);
CREATE INDEX IF NOT EXISTS idx_player_login_pin ON players(login_pin);
CREATE INDEX IF NOT EXISTS idx_player_user_id ON players(user_id);

CREATE TABLE IF NOT EXISTS devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50),
    location VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS stars (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    star_type VARCHAR(50),
    mass DOUBLE,
    temperature DOUBLE,
    row INT NOT NULL,
    col INT NOT NULL
);

CREATE TABLE IF NOT EXISTS galaxies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    galaxy_type VARCHAR(50),
    size DOUBLE
);

CREATE TABLE IF NOT EXISTS solar_systems (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    galaxy_id BIGINT,
    row INT NOT NULL,
    col INT NOT NULL,
    CONSTRAINT fk_solar_system_galaxy FOREIGN KEY (galaxy_id) REFERENCES galaxies(id)
);

CREATE TABLE IF NOT EXISTS planets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    planet_type VARCHAR(50),
    size VARCHAR(20),
    mineral_quality VARCHAR(20),
    solar_system_id BIGINT,
    owner_id BIGINT,
    row INT NOT NULL,
    col INT NOT NULL,
    CONSTRAINT fk_planet_solar_system FOREIGN KEY (solar_system_id) REFERENCES solar_systems(id)
);

CREATE TABLE IF NOT EXISTS game_sessions_persisted (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    session_name VARCHAR(255) NOT NULL,
    game_state LONGTEXT,
    player_ids VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_activity_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_game_session_last_activity ON game_sessions_persisted(last_activity_at);
