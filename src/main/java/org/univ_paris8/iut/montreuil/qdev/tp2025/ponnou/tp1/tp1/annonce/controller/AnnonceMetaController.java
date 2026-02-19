package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.utils.AnnonceFieldValidator;

import java.util.Map;

@RestController
@RequestMapping("/api/meta/annonces")
@Tag(name = "Meta", description = "Metadonnees des annonces")
public class AnnonceMetaController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMeta() {
        return ResponseEntity.ok(Map.of(
                "sortableFields", AnnonceFieldValidator.getSortableFields(),
                "filterableFields", AnnonceFieldValidator.getFilterableFields()
        ));
    }
}
