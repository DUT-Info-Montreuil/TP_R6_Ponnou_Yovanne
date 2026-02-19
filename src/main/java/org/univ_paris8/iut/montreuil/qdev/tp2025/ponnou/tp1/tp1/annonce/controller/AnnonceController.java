package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto.AnnonceCreateDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto.AnnonceDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto.AnnoncePatchDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto.AnnonceUpdateDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.mappers.AnnonceMapper;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.service.AnnonceService;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.security.AuthenticatedUser;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.dto.PaginatedResponse;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ResourceNotFoundException;

import java.net.URI;

@RestController
@RequestMapping("/api/annonces")
@RequiredArgsConstructor
@Tag(name = "Annonces", description = "Gestion des annonces")
public class AnnonceController {

    private final AnnonceService annonceService;
    private final AnnonceMapper annonceMapper;

    @GetMapping
    @Operation(summary = "Lister les annonces", description = "Retourne la liste paginee des annonces.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des annonces retournee"),
            @ApiResponse(responseCode = "500", description = "Erreur interne")
    })
    public ResponseEntity<PaginatedResponse<AnnonceDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) AnnonceStatus status) {
        Page<Annonce> annonces = annonceService.searchWithFilters(
                keyword, categoryId, authorId, status, PageRequest.of(page, size, Sort.by("date").descending()));
        return ResponseEntity.ok(new PaginatedResponse<>(
                annonces.map(annonceMapper::toDTO).getContent(),
                page,
                size,
                annonces.getTotalElements()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer une annonce", description = "Retourne le detail d'une annonce par son id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Annonce retournee"),
            @ApiResponse(responseCode = "404", description = "Annonce non trouvee"),
            @ApiResponse(responseCode = "500", description = "Erreur interne")
    })
    public ResponseEntity<AnnonceDTO> getById(@PathVariable Long id) {
        Annonce annonce = annonceService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce non trouvee avec l'id : " + id));
        return ResponseEntity.ok(annonceMapper.toDTO(annonce));
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Creer une annonce", description = "Cree une annonce en statut DRAFT pour l'utilisateur authentifie.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Annonce creee"),
            @ApiResponse(responseCode = "400", description = "Requete invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifie"),
            @ApiResponse(responseCode = "404", description = "Utilisateur ou categorie introuvable"),
            @ApiResponse(responseCode = "500", description = "Erreur interne")
    })
    public ResponseEntity<AnnonceDTO> create(@Valid @RequestBody AnnonceCreateDTO dto, Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        Annonce created = annonceService.create(
                dto.getTitle(),
                dto.getDescription(),
                dto.getAdress(),
                dto.getMail(),
                userId,
                dto.getCategoryId()
        );
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(annonceMapper.toDTO(created));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mettre a jour une annonce", description = "Met a jour une annonce existante.")
    public ResponseEntity<AnnonceDTO> update(@PathVariable Long id,
                                             @Valid @RequestBody AnnonceUpdateDTO dto,
                                             Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        Annonce updated = annonceService.update(
                id,
                userId,
                dto.getTitle(),
                dto.getDescription(),
                dto.getAdress(),
                dto.getMail(),
                dto.getCategoryId()
        );
        return ResponseEntity.ok(annonceMapper.toDTO(updated));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Supprimer une annonce", description = "Supprime une annonce archivee.")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        annonceService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Patch partiel d'une annonce", description = "Met a jour partiellement une annonce.")
    public ResponseEntity<AnnonceDTO> patch(@PathVariable Long id,
                                            @Valid @RequestBody AnnoncePatchDTO dto,
                                            Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        Annonce patched = annonceService.patch(id, userId, dto);
        return ResponseEntity.ok(annonceMapper.toDTO(patched));
    }

    @PutMapping("/{id}/publish")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Publier une annonce", description = "Passe une annonce de DRAFT a PUBLISHED.")
    public ResponseEntity<AnnonceDTO> publish(@PathVariable Long id, Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        Annonce published = annonceService.publish(id, userId);
        return ResponseEntity.ok(annonceMapper.toDTO(published));
    }

    @PutMapping("/{id}/archive")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Archiver une annonce", description = "Passe une annonce au statut ARCHIVED.")
    public ResponseEntity<AnnonceDTO> archive(@PathVariable Long id, Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        Annonce archived = annonceService.archive(id, userId);
        return ResponseEntity.ok(annonceMapper.toDTO(archived));
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        Object principal = authentication != null ? authentication.getPrincipal() : null;
        if (principal instanceof AuthenticatedUser user) {
            return user.getUserId();
        }
        throw new ResourceNotFoundException("Utilisateur authentifie introuvable");
    }
}
