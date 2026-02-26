package com.tp_ia;

import org.junit.jupiter.api.*;
import org.tribuo.*;
import org.tribuo.classification.*;
import org.tribuo.classification.evaluation.*;
import org.tribuo.classification.sgd.linear.LogisticRegressionTrainer;
import org.tribuo.data.csv.CSVDataSource;
import org.tribuo.evaluation.TrainTestSplitter;
import org.tribuo.impl.ArrayExample;
import org.tribuo.data.columnar.*;
import org.tribuo.data.columnar.processors.field.*;
import org.tribuo.data.columnar.processors.response.*;

import java.io.*;
import java.nio.file.*;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LogistiqueLMIATests {

    private static final String fileName     = "livraison_retards_dataset.csv";
    private static final String newFileName  = "livraison_retards_dataset_converted.csv";
    private static final String modelFile    = "livraison_regressor.ser";

    private static final Path input      = Paths.get("src", "main", "resources", fileName);
    private static final Path output     = Paths.get("src", "main", "resources", newFileName);
    private static final Path MODEL_PATH = Paths.get("src", "main", "resources", modelFile);

    private static LabelFactory                        labelFactory;
    private static LinkedHashMap<String, FieldProcessor> fieldProcessors;
    private static RowProcessor<Label>                 rowProcessor;

    private static CSVDataSource<Label>                dataSource;
    private static MutableDataset<Label>               train;
    private static MutableDataset<Label>               test;
    private static Model<Label>                        model;

    private static Prediction<Label>                   prediction;

    @BeforeAll
    public static void setUp() {
        labelFactory   = new LabelFactory();
        fieldProcessors = new LinkedHashMap<>();
        configFile();
    }

    @AfterAll
    public static void tearDown() {
        if (output.toFile().exists()) {
            boolean deleted = output.toFile().delete();
            assertTrue(deleted, "Le fichier converti doit être supprimé après les tests");
        }
        if (MODEL_PATH.toFile().exists()) {
            boolean deleted = MODEL_PATH.toFile().delete();
            assertTrue(deleted, "Le fichier du modèle doit être supprimé après les tests");
        }
    }

    // -------------------------------------------------------------------------
    // Configuration des FieldProcessors (encodage des colonnes)
    // -------------------------------------------------------------------------

    private static void configFile() {
        // Champ numérique calculé : heure décimale (ex : 08:30 -> 8.5)
        fieldProcessors.put("heure_decimal", new DoubleFieldProcessor("heure_decimal"));

        // distance_km est déjà numérique
        fieldProcessors.put("distance_km", new DoubleFieldProcessor("distance_km"));

        // Variables catégorielles -> encodées en one-hot par IdentityProcessor
        fieldProcessors.put("pluie",         new IdentityProcessor("pluie"));
        fieldProcessors.put("jour_semaine",  new IdentityProcessor("jour_semaine"));
        fieldProcessors.put("vehicule_type", new IdentityProcessor("vehicule_type"));

        // Colonne de sortie : "retard" (oui / non), valeur par défaut = "non"
        FieldResponseProcessor<Label> responseProcessor =
                new FieldResponseProcessor<>("retard", "non", labelFactory);

        rowProcessor = new RowProcessor<>(responseProcessor, fieldProcessors);
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    @Order(1)
    void prepareDatasets() {
        HeureDepartPreprocessor.convertPreprocessor(input, output);
        assertTrue(output.toFile().exists(), "Le fichier converti doit exister");
        assertTrue(output.toFile().length() > 0, "Le fichier converti ne doit pas être vide");
        System.out.println("Fichier CSV converti créé : " + output.toAbsolutePath());
    }

    @Test
    @Order(2)
    void loadDatasets() throws IOException {
        dataSource = new CSVDataSource<>(output, rowProcessor, true);
        assertNotNull(dataSource, "La source de données ne doit pas être null");
        assertFalse(dataSource.toString().isEmpty(), "La source de données doit contenir des données");
        System.out.println("Source de données chargée : " + dataSource);
    }

    @Test
    @Order(3)
    void splitTrainTest() {
        var splitter = new TrainTestSplitter<>(dataSource, 0.8, 42L);
        train = new MutableDataset<>(splitter.getTrain());
        test  = new MutableDataset<>(splitter.getTest());

        assertTrue(train.size() > 0, "L'ensemble d'entraînement ne doit pas être vide");
        assertTrue(test.size()  > 0, "L'ensemble de test ne doit pas être vide");

        System.out.printf("Train : %d exemples | Test : %d exemples%n",
                train.size(), test.size());
    }

    @Test
    @Order(4)
    void training() {
        var trainer = new LogisticRegressionTrainer();
        model = trainer.train(train);
        assertNotNull(model, "Le modèle ne doit pas être null");
        System.out.println("Modèle entraîné avec succès.");
    }

    @Test
    @Order(5)
    void evaluator() {
        assertNotNull(model, "Le modèle doit être entraîné avant l'évaluation");
        var evaluator = new LabelEvaluator();
        LabelEvaluation evaluation = evaluator.evaluate(model, test);

        System.out.println("=== Résultats de l'évaluation ===");
        System.out.println(evaluation.toString());

        double accuracy = evaluation.accuracy();
        System.out.printf("Accuracy : %.2f%%%n", accuracy * 100);
        assertTrue(accuracy >= 0.0 && accuracy <= 1.0, "L'accuracy doit être entre 0 et 1");
    }

    @Test
    @Order(6)
    void saveModel() throws Exception {
        assertNotNull(model, "Le modèle doit être entraîné avant la sauvegarde");
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(MODEL_PATH.toFile()))) {
            oos.writeObject(model);
        }
        assertTrue(MODEL_PATH.toFile().exists(), "Le fichier du modèle doit exister");
        System.out.println("Modèle sauvegardé dans : " + MODEL_PATH.toAbsolutePath());
    }

    @Test
    @Order(7)
    @SuppressWarnings("unchecked")
    void predictor() throws Exception {
        File modelFileObj = MODEL_PATH.toFile();
        assertTrue(modelFileObj.exists(), "Le fichier du modèle doit exister pour la prédiction");

        Model<Label> loadedModel;
        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(modelFileObj))) {
            loadedModel = (Model<Label>) ois.readObject();
        }
        assertNotNull(loadedModel, "Le modèle chargé ne doit pas être null");

        Example<Label> example = new ArrayExample<>(new Label("non"));
        example.add(new Feature("distance_km@value",          120.0));
        example.add(new Feature("heure_decimal@value",           8.0));
        example.add(new Feature("pluie@non",                     0.0));
        example.add(new Feature("jour_semaine@mercredi",         2.0));
        example.add(new Feature("vehicule_type@camionnette",     1.0));

        prediction = loadedModel.predict(example);
        assertNotNull(prediction, "La prédiction ne doit pas être null");

        System.out.println("=== Prédiction ===");
        System.out.println("Retard prédit : " + prediction.getOutput());
        System.out.println("Distribution des scores : " + prediction.getOutputScores());
    }
}
