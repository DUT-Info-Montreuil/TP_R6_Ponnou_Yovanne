package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto.AnnonceDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto.AnnonceCreateDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto.AnnonceUpdateDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.mappers.AnnonceMapper;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.dto.AnnoncePatchDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.service.AnnonceService;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.filter.AuthTokenFilter;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.model.Secured;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.dto.PaginatedResponse;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ResourceNotFoundException;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

@Path("/annonces")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Annonces", description = "Gestion des annonces")
public class AnnonceController {

    private final AnnonceService annonceService = new AnnonceService();

    @GET
    @Operation(summary = "Lister les annonces", description = "Retourne la liste paginee des annonces.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des annonces retournee", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content())
    })
    public Response getAll(@QueryParam("page") @DefaultValue("0") int page,
                           @QueryParam("size") @DefaultValue("10") int size) {
        List<Annonce> annonces = annonceService.findAllPaginated(page, size);
        long total = annonceService.count();
        List<AnnonceDTO> dtos = AnnonceMapper.toDTOList(annonces);
        return Response.ok(new PaginatedResponse<>(dtos, page, size, total)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Recuperer une annonce", description = "Retourne le detail d'une annonce par son id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Annonce retournee", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Annonce non trouvee", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content())
    })
    public Response getById(@PathParam("id") Long id) {
        Annonce annonce = annonceService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce non trouvée avec l'id : " + id));
        return Response.ok(AnnonceMapper.toDTO(annonce)).build();
    }

    @POST
    @Secured
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Creer une annonce", description = "Cree une annonce en statut DRAFT pour l'utilisateur authentifie.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Annonce creee", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Requete invalide", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Non authentifie", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Utilisateur ou categorie introuvable", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content())
    })
    public Response create(@Valid AnnonceCreateDTO dto,
                           @Context ContainerRequestContext requestContext,
                           @Context UriInfo uriInfo) {
        Long userId = (Long) requestContext.getProperty(AuthTokenFilter.USER_ID_PROPERTY);
        Annonce created = annonceService.create(
                dto.getTitle(),
                dto.getDescription(),
                dto.getAdress(),
                dto.getMail(),
                userId,
                dto.getCategoryId()
        );
        AnnonceDTO responseDTO = AnnonceMapper.toDTO(created);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.getId())).build();
        return Response.created(location).entity(responseDTO).build();
    }

    @PUT
    @Path("/{id}")
    @Secured
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mettre a jour une annonce", description = "Met a jour une annonce existante.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Annonce mise a jour", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Requete invalide", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Non authentifie", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Operation interdite", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Annonce ou categorie introuvable", content = @Content()),
            @ApiResponse(responseCode = "409", description = "Conflit metier", content = @Content())
    })
    public Response update(@PathParam("id") Long id, @Valid AnnonceUpdateDTO dto,
                           @Context ContainerRequestContext requestContext) {
        Long userId = (Long) requestContext.getProperty(AuthTokenFilter.USER_ID_PROPERTY);
        Annonce updated = annonceService.update(
                id,
                userId,
                dto.getTitle(),
                dto.getDescription(),
                dto.getAdress(),
                dto.getMail(),
                dto.getCategoryId()
        );
        return Response.ok(AnnonceMapper.toDTO(updated)).build();
    }

    @DELETE
    @Path("/{id}")
    @Secured
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Supprimer une annonce", description = "Supprime une annonce archivee.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Annonce supprimee", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Non authentifie", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Operation interdite", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Annonce introuvable", content = @Content()),
            @ApiResponse(responseCode = "409", description = "Conflit metier", content = @Content())
    })
    public Response delete(@PathParam("id") Long id,
                           @Context ContainerRequestContext requestContext) {
        Long userId = (Long) requestContext.getProperty(AuthTokenFilter.USER_ID_PROPERTY);
        annonceService.delete(id, userId);
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{id}")
    @Secured
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Patch partiel d'une annonce", description = "Met a jour partiellement une annonce.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Annonce mise a jour", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Requete invalide", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Non authentifie", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Operation interdite", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Annonce ou categorie introuvable", content = @Content()),
            @ApiResponse(responseCode = "409", description = "Conflit metier", content = @Content())
    })
    public Response patch(@PathParam("id") Long id, @Valid AnnoncePatchDTO dto,
                          @Context ContainerRequestContext requestContext) {
        Long userId = (Long) requestContext.getProperty(AuthTokenFilter.USER_ID_PROPERTY);
        Annonce patched = annonceService.patch(
                id,
                userId,
                dto.getTitle(),
                dto.getDescription(),
                dto.getAdress(),
                dto.getMail(),
                dto.getCategoryId()
        );
        return Response.ok(AnnonceMapper.toDTO(patched)).build();
    }

    @PUT
    @Path("/{id}/publish")
    @Secured
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Publier une annonce", description = "Passe une annonce de DRAFT a PUBLISHED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Annonce publiee", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Non authentifie", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Operation interdite", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Annonce introuvable", content = @Content()),
            @ApiResponse(responseCode = "409", description = "Conflit metier", content = @Content())
    })
    public Response publish(@PathParam("id") Long id,
                            @Context ContainerRequestContext requestContext) {
        Long userId = (Long) requestContext.getProperty(AuthTokenFilter.USER_ID_PROPERTY);
        Annonce published = annonceService.publish(id, userId);
        return Response.ok(AnnonceMapper.toDTO(published)).build();
    }

    @PUT
    @Path("/{id}/archive")
    @Secured
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Archiver une annonce", description = "Passe une annonce au statut ARCHIVED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Annonce archivee", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Non authentifie", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Operation interdite", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Annonce introuvable", content = @Content()),
            @ApiResponse(responseCode = "409", description = "Conflit metier", content = @Content())
    })
    public Response archive(@PathParam("id") Long id,
                            @Context ContainerRequestContext requestContext) {
        Long userId = (Long) requestContext.getProperty(AuthTokenFilter.USER_ID_PROPERTY);
        Annonce archived = annonceService.archive(id, userId);
        return Response.ok(AnnonceMapper.toDTO(archived)).build();
    }
}
