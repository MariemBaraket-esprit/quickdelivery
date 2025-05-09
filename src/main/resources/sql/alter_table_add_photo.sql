USE gestion_utilisateurs;

ALTER TABLE utilisateur
ADD COLUMN photo_url VARCHAR(255) DEFAULT NULL; 