DROP TABLE IF EXISTS annonces;
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

-- Table des utilisateurs
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table des catégories
CREATE TABLE categories (
    id    BIGSERIAL PRIMARY KEY,
    label VARCHAR(100) NOT NULL UNIQUE
);

-- Table des annonces
CREATE TABLE annonces (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(64)  NOT NULL,
    description VARCHAR(256) NOT NULL,
    adress      VARCHAR(64)  NOT NULL,
    mail        VARCHAR(64)  NOT NULL,
    date        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status      VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    version     BIGINT       NOT NULL DEFAULT 0,
    author_id   BIGINT       NOT NULL,
    category_id BIGINT       NOT NULL,
    CONSTRAINT fk_annonces_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_annonces_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT chk_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

-- Table des tokens de rafraîchissement
CREATE TABLE refresh_tokens (
    id         BIGSERIAL PRIMARY KEY,
    token      VARCHAR(512) NOT NULL UNIQUE,
    user_id    BIGINT       NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Index pour améliorer les performances
CREATE INDEX idx_annonces_author ON annonces(author_id);
CREATE INDEX idx_annonces_category ON annonces(category_id);
CREATE INDEX idx_annonces_status ON annonces(status);
CREATE INDEX idx_annonces_date ON annonces(date DESC);

-- Données de test

-- Utilisateurs (mot de passe : "password" pour tous, hashé en BCrypt)
INSERT INTO users (username, email, password, role) VALUES
    ('admin', 'admin@masterannonce.fr', '$2a$10$slYQmyNdgTY1hcT.f5rOCOEpOzLqoW9YuH0vU85z7SHkAK8G5kW/C', 'ROLE_ADMIN'),
    ('jean', 'jean.dupont@mail.com', '$2a$10$slYQmyNdgTY1hcT.f5rOCOEpOzLqoW9YuH0vU85z7SHkAK8G5kW/C', 'ROLE_USER'),
    ('marie', 'marie.martin@mail.com', '$2a$10$slYQmyNdgTY1hcT.f5rOCOEpOzLqoW9YuH0vU85z7SHkAK8G5kW/C', 'ROLE_USER');

-- Catégories
INSERT INTO categories (label) VALUES
    ('Immobilier'),
    ('Véhicules'),
    ('Emploi'),
    ('Services'),
    ('Multimédia'),
    ('Maison'),
    ('Loisirs');

-- Annonces de test
INSERT INTO annonces (title, description, adress, mail, status, author_id, category_id) VALUES
    ('Appartement T3 à louer', 'Bel appartement 3 pièces, lumineux, proche métro', 'Montreuil 93100', 'jean.dupont@mail.com', 'PUBLISHED', 2, 1),
    ('Vends vélo VTT', 'Vélo VTT en très bon état, peu utilisé', 'Paris 75020', 'marie.martin@mail.com', 'PUBLISHED', 3, 2),
    ('Cours de Java', 'Cours particuliers Java/JEE pour débutants et intermédiaires', 'Saint-Denis 93200', 'admin@masterannonce.fr', 'PUBLISHED', 1, 4),
    ('Don canapé', 'Canapé 2 places à venir chercher rapidement', 'Paris 75011', 'jean.dupont@mail.com', 'DRAFT', 2, 6),
    ('Recherche développeur', 'CDI développeur Java senior, télétravail possible', 'Paris 75009', 'admin@masterannonce.fr', 'PUBLISHED', 1, 3),
    ('Console PS5', 'PS5 avec 2 manettes et 3 jeux', 'Vincennes 94300', 'marie.martin@mail.com', 'ARCHIVED', 3, 5);
