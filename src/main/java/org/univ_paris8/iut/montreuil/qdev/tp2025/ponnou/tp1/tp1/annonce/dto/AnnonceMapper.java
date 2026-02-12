package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;

import java.util.List;
import java.util.stream.Collectors;

public final class AnnonceMapper {

    private AnnonceMapper() {
    }

    public static AnnonceDTO toDTO(Annonce entity) {
        return AnnonceDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .adress(entity.getAdress())
                .mail(entity.getMail())
                .date(entity.getDate())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .authorUsername(entity.getAuthor() != null ? entity.getAuthor().getUsername() : null)
                .categoryLabel(entity.getCategory() != null ? entity.getCategory().getLabel() : null)
                .build();
    }

    public static List<AnnonceDTO> toDTOList(List<Annonce> entities) {
        return entities.stream()
                .map(AnnonceMapper::toDTO)
                .collect(Collectors.toList());
    }
}
