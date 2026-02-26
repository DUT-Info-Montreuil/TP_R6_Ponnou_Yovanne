package com.tp_ia;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App
{
    public static void main( String[] args ) throws URISyntaxException
    {
        Path input = Paths.get(App.class.getClassLoader().getResource("livraison_retards_dataset.csv").toURI());
        Path output = input.getParent().resolve("livraison_retards_preprocessed.csv");

        HeureDepartPreprocessor.convertPreprocessor(input, output);
    }
}
