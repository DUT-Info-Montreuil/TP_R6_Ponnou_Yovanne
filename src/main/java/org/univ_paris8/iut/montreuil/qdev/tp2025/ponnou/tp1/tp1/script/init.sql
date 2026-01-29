DROP DATABASE IF EXISTS "MasterAnnonce";
CREATE DATABASE "MasterAnnonce";

\c "MasterAnnonce";
DROP TABLE IF EXISTS annonce;
CREATE TABLE annonce (
                         id          SERIAL PRIMARY KEY,
                         title       VARCHAR(64)  NOT NULL,
                         description VARCHAR(256) NOT NULL,
                         adress      VARCHAR(64)  NOT NULL,
                         mail        VARCHAR(64)  NOT NULL,
                         date        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO annonce (title, description, adress, mail)
VALUES
    ('Vends vélo', 'Vélo en bon état, peu utilisé', 'Montreuil', 'velo@mail.com'),
    ('Don canapé', 'Canapé 2 places à venir chercher', 'Paris', 'canape@mail.com'),
    ('Cours Java', 'Cours Java/JEE pour débutants', 'Saint-Denis', 'java@mail.com');

SELECT * FROM annonce ORDER BY date DESC;
