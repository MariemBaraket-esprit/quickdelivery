USE gestion_utilisateurs;

-- Add nom and prenom columns
ALTER TABLE utilisateur
ADD COLUMN nom VARCHAR(50) NOT NULL DEFAULT 'Nom' AFTER id_user,
ADD COLUMN prenom VARCHAR(50) NOT NULL DEFAULT 'Prénom' AFTER nom;

-- Update existing records with default values if needed
UPDATE utilisateur
SET nom = SUBSTRING_INDEX(email, '@', 1),
    prenom = SUBSTRING_INDEX(email, '@', 1)
WHERE nom = 'Nom' AND prenom = 'Prénom'; 