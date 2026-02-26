package com.tp_ia;

import org.junit.jupiter.api.*;
import org.tribuo.*;
import org.tribuo.classification.*;
import org.tribuo.classification.evaluation.*;
import org.tribuo.classification.sgd.linear.LogisticRegressionTrainer;
import org.tribuo.classification.dtree.CARTClassificationTrainer;
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
public class PluiePredictionTests {

    private static final String csvSource   = "livraison_retards_dataset.csv";
    private static final String modelFile   = "pluie_model.ser";

    private static final Path input      = Paths.get("src", "main", "resources", csvSource);
    private static final Path MODEL_PATH = Paths.get("src", "main", "resources", modelFile);

    private static LabelFactory                          labelFactory;
    private static LinkedHashMap<String, FieldProcessor> fieldProcessors;
    private static RowProcessor<Label>                   rowProcessor;

    private static CSVDataSource<Label>  dataSource;
    private static MutableDataset<Label> train;
    private static MutableDataset<Label> test;

    private static Model<Label> logisticModel;
    private static Model<Label> cartModel;

    @BeforeAll
    public static void setUp() {
        labelFactory    = new LabelFactory();
        fieldProcessors = new LinkedHashMap<>();
        configFile();
    }

    @AfterAll
    public static void tearDown() {
        if (MODEL_PATH.toFile().exists()) {
            boolean deleted = MODEL_PATH.toFile().delete();
            assertTrue(deleted, "Le fichier du modèle de pluie doit être supprimé après les tests");
        }
    }


    private static void configFile() {
        fieldProcessors.put("jour_semaine", new IdentityProcessor("jour_semaine"));
        fieldProcessors.put("retard",       new IdentityProcessor("retard"));

        FieldResponseProcessor<Label> responseProcessor =
                new FieldResponseProcessor<>("pluie", "non", labelFactory);

        rowProcessor = new RowProcessor<>(responseProcessor, fieldProcessors);
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    @Order(1)
    void loadDatasets() throws IOException {
        dataSource = new CSVDataSource<>(input, rowProcessor, true);
        assertNotNull(dataSource);
        System.out.println("Source de données pluie chargée : " + dataSource);
    }

    @Test
    @Order(2)
    void splitTrainTest() {
        var splitter = new TrainTestSplitter<>(dataSource, 0.8, 42L);
        train = new MutableDataset<>(splitter.getTrain());
        test  = new MutableDataset<>(splitter.getTest());

        assertTrue(train.size() > 0, "Train ne doit pas être vide");
        assertTrue(test.size()  > 0, "Test ne doit pas être vide");

        System.out.printf("Train : %d exemples | Test : %d exemples%n",
                train.size(), test.size());
    }

    @Test
    @Order(3)
    void trainingLogistic() {
        var trainer = new LogisticRegressionTrainer();
        logisticModel = trainer.train(train);
        assertNotNull(logisticModel);
        System.out.println("Modèle logistique (pluie) entraîné.");
    }

    @Test
    @Order(4)
    void trainingCART() {
        var trainer = new CARTClassificationTrainer();
        cartModel = trainer.train(train);
        assertNotNull(cartModel);
        System.out.println("Modèle CART (arbre de décision, pluie) entraîné.");
    }

    @Test
    @Order(5)
    void evaluator() {
        var evaluator = new LabelEvaluator();

        System.out.println("=== Évaluation – Régression Logistique ===");
        LabelEvaluation evalLogistic = evaluator.evaluate(logisticModel, test);
        System.out.println(evalLogistic.toString());
        System.out.printf("Accuracy Logistic : %.2f%%%n", evalLogistic.accuracy() * 100);

        System.out.println("=== Évaluation – CART (Arbre de décision) ===");
        LabelEvaluation evalCart = evaluator.evaluate(cartModel, test);
        System.out.println(evalCart.toString());
        System.out.printf("Accuracy CART : %.2f%%%n", evalCart.accuracy() * 100);

        assertTrue(evalLogistic.accuracy() >= 0.0 && evalLogistic.accuracy() <= 1.0);
        assertTrue(evalCart.accuracy()     >= 0.0 && evalCart.accuracy()     <= 1.0);
    }

    @Test
    @Order(6)
    void saveModel() throws Exception {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(MODEL_PATH.toFile()))) {
            oos.writeObject(cartModel);
        }
        assertTrue(MODEL_PATH.toFile().exists(), "Le fichier du modèle CART doit exister");
        System.out.println("Modèle CART sauvegardé dans : " + MODEL_PATH.toAbsolutePath());
    }

    @Test
    @Order(7)
    @SuppressWarnings("unchecked")
    void predictor() throws Exception {
        File modelFileObj = MODEL_PATH.toFile();
        assertTrue(modelFileObj.exists(), "Le fichier du modèle doit exister");

        Model<Label> loadedModel;
        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(modelFileObj))) {
            loadedModel = (Model<Label>) ois.readObject();
        }
        assertNotNull(loadedModel);

        Example<Label> example = new ArrayExample<>(new Label("non"));
        example.add(new Feature("jour_semaine@vendredi", 1.0));
        example.add(new Feature("retard@oui",            1.0));

        Prediction<Label> pred = loadedModel.predict(example);
        assertNotNull(pred);

        System.out.println("=== Prédiction Pluie ===");
        System.out.println("Entrée : jour_semaine=vendredi, retard=oui");
        System.out.println("Pluie prédite : " + pred.getOutput());
        System.out.println("Distribution des scores : " + pred.getOutputScores());
    }

    @Test
    @Order(8)
    void interpretationArbre() {
        assertNotNull(cartModel, "Le modèle CART doit être entraîné");
        System.out.println("=== Interprétation de l'arbre de probabilités (CART) ===");
        System.out.println("Features utilisées par le modèle CART :");
        cartModel.getFeatureIDMap().forEach(f -> System.out.println("  - " + f.getName()));
        System.out.println();
        System.out.println("Interprétation :");
        System.out.println("  L'arbre de décision CART divise les données sur les features");
        System.out.println("  'jour_semaine' et 'retard' afin de prédire la probabilité de pluie.");
        System.out.println("  Chaque feuille de l'arbre donne la distribution P(pluie=oui) / P(pluie=non).");
        System.out.println("  Un score élevé sur 'oui' indique que le modèle prédit de la pluie.");
    }
}
