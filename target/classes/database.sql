-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS quick_dilevery;

-- Use the database
USE quick_dilevery;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create reclamations table
CREATE TABLE IF NOT EXISTS reclamations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RECEIVED',
    image_path VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create responses table
CREATE TABLE IF NOT EXISTS responses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    reclamation_id INT NOT NULL,
    user_id INT NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reclamation_id) REFERENCES reclamations(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create utilisateur table (from reclamation project)
CREATE TABLE IF NOT EXISTS utilisateur (
    id_user INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(50) NOT NULL,
    prenom VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    telephone VARCHAR(20),
    adresse TEXT,
    date_naissance DATE,
    date_debut_contrat DATE,
    date_fin_contrat DATE,
    password VARCHAR(255) NOT NULL,
    salaire DECIMAL(10,2),
    type_utilisateur VARCHAR(20) NOT NULL,
    statut VARCHAR(20) NOT NULL DEFAULT 'ACTIF'
);

-- Insert default admin user (password: admin123)
INSERT INTO users (username, password, email, role) 
VALUES ('admin', '$2a$10$8K1p/a0dR1Ux5Y5Y5Y5Y5O5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y', 'admin@quickdelivery.com', 'ADMIN')
ON DUPLICATE KEY UPDATE username = username; 