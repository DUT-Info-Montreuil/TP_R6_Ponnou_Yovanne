package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.utils;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class AnnonceFieldValidator {

    private static final Set<String> SORTABLE_FIELDS;
    private static final Set<String> FILTERABLE_FIELDS;

    static {
        SORTABLE_FIELDS = Arrays.stream(Annonce.class.getDeclaredFields())
                .map(Field::getName)
                .filter(name -> !name.equals("author") && !name.equals("category") && !name.equals("version"))
                .collect(Collectors.toUnmodifiableSet());

        FILTERABLE_FIELDS = Set.of("keyword", "status", "categoryId", "authorId", "fromDate", "toDate");
    }

    private AnnonceFieldValidator() {
    }

    public static void validateSortField(String field) {
        if (!SORTABLE_FIELDS.contains(field)) {
            throw new IllegalArgumentException(
                    "Champ de tri invalide : '" + field + "'. Champs autorises : " + SORTABLE_FIELDS);
        }
    }

    public static Set<String> getSortableFields() {
        return SORTABLE_FIELDS;
    }

    public static Set<String> getFilterableFields() {
        return FILTERABLE_FIELDS;
    }
}
