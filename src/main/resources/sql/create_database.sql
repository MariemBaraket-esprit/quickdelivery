CREATE DATABASE IF NOT EXISTS quickdelivery_db;
USE quickdelivery_db;

CREATE TABLE IF NOT EXISTS utilisateur (
    id_user INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(50) NOT NULL,
    prenom VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    telephone VARCHAR(8) NOT NULL UNIQUE,
    adresse TEXT NOT NULL,
    date_naissance DATE,
    date_debut_contrat DATE,
    date_fin_contrat DATE,
    password VARCHAR(255),
    salaire DOUBLE,
    type_utilisateur ENUM('ADMIN', 'RESPONSABLE', 'CLIENT', 'LIVREUR', 'MAGASINIER') NOT NULL,
    statut ENUM('CONGE', 'ACTIF', 'INACTIF', 'ABSENT') NOT NULL DEFAULT 'ACTIF',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
); 