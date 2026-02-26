package com.tp_ia;

import java.io.*;
import java.nio.file.Path;

/**
 * Prétraitement : convertit la colonne heure_depart (HH:mm) en heure décimale
 * et insère la nouvelle colonne heure_decimal dans le CSV de sortie.
 */
public class HeureDepartPreprocessor {

    public static void convertPreprocessor(Path input, Path output) {
        try (
            BufferedReader reader = new BufferedReader(new FileReader(input.toFile()));
            PrintWriter writer = new PrintWriter(new FileWriter(output.toFile()))
        ) {
            String headerLine = reader.readLine();
            if (headerLine == null) return;

            // Ajouter heure_decimal après heure_depart dans l'en-tête
            String newHeader = headerLine.replace("heure_depart,", "heure_depart,heure_decimal,");
            writer.println(newHeader);

            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(",");
                // Colonnes : id_course,heure_depart,distance_km,pluie,jour_semaine,vehicule_type,retard
                // Index     :     0          1             2       3       4              5          6
                if (cols.length < 7) continue;

                String heureDepart = cols[1].trim();
                double heureDecimale = convertirHeure(heureDepart);

                StringBuilder sb = new StringBuilder();
                sb.append(cols[0]).append(",");   // id_course
                sb.append(cols[1]).append(",");   // heure_depart
                sb.append(heureDecimale).append(","); // heure_decimal (nouveau)
                sb.append(cols[2]).append(",");   // distance_km
                sb.append(cols[3]).append(",");   // pluie
                sb.append(cols[4]).append(",");   // jour_semaine
                sb.append(cols[5]).append(",");   // vehicule_type
                sb.append(cols[6]);               // retard
                writer.println(sb.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la conversion du fichier CSV : " + e.getMessage(), e);
        }
    }

    /**
     * Convertit une heure au format HH:mm en nombre décimal.
     * Ex : "08:30" -> 8.5, "16:45" -> 16.75
     */
    private static double convertirHeure(String heure) {
        String[] parts = heure.split(":");
        int heures = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return heures + minutes / 60.0;
    }
}
