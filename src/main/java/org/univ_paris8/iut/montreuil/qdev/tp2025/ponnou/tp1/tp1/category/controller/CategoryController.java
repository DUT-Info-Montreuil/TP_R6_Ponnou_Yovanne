package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dto.CategoryDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dto.CategoryCreateDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dto.CategoryUpdateDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.mapper.CategoryMapper;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dto.CategoryPatchDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.model.Category;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.service.CategoryService;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.model.Secured;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.dto.PaginatedResponse;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ResourceNotFoundException;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Categories", description = "Gestion des categories")
public class CategoryController {

    private final CategoryService categoryService = new CategoryService();

    @GET
    @Operation(summary = "Lister les categories", description = "Retourne la liste paginee des categories.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des categories retournee", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content())
    })
    public Response getAll(@QueryParam("page") @DefaultValue("0") int page,
                           @QueryParam("size") @DefaultValue("10") int size) {
        List<Category> categories = categoryService.findAllPaginated(page, size);
        long total = categoryService.count();
        List<CategoryDTO> dtos = CategoryMapper.toDTOList(categories);
        return Response.ok(new PaginatedResponse<>(dtos, page, size, total)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Recuperer une categorie", description = "Retourne le detail d'une categorie par son id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categorie retournee", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Categorie non trouvee", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content())
    })
    public Response getById(@PathParam("id") Long id) {
        Category category = categoryService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée avec l'id : " + id));
        return Response.ok(CategoryMapper.toDTO(category)).build();
    }

    @POST
    @Secured
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Creer une categorie", description = "Cree une nouvelle categorie.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categorie creee", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Requete invalide", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Non authentifie", content = @Content()),
            @ApiResponse(responseCode = "409", description = "Conflit metier", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content())
    })
    public Response create(@Valid CategoryCreateDTO dto, @Context UriInfo uriInfo) {
        Category created = categoryService.create(dto.getLabel());
        CategoryDTO responseDTO = CategoryMapper.toDTO(created);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.getId())).build();
        return Response.created(location).entity(responseDTO).build();
    }

    @PUT
    @Path("/{id}")
    @Secured
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mettre a jour une categorie", description = "Met a jour une categorie existante.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categorie mise a jour", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Requete invalide", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Non authentifie", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Categorie non trouvee", content = @Content()),
            @ApiResponse(responseCode = "409", description = "Conflit metier", content = @Content())
    })
    public Response update(@PathParam("id") Long id, @Valid CategoryUpdateDTO dto) {
        Category updated = categoryService.update(id, dto.getLabel());
        return Response.ok(CategoryMapper.toDTO(updated)).build();
    }

    @DELETE
    @Path("/{id}")
    @Secured
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Supprimer une categorie", description = "Supprime une categorie si elle n'est pas utilisee.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categorie supprimee", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Non authentifie", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Categorie non trouvee", content = @Content()),
            @ApiResponse(responseCode = "409", description = "Conflit metier", content = @Content())
    })
    public Response delete(@PathParam("id") Long id) {
        categoryService.delete(id);
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{id}")
    @Secured
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Patch partiel d'une categorie", description = "Met a jour partiellement une categorie.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categorie mise a jour", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Requete invalide", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Non authentifie", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Categorie non trouvee", content = @Content()),
            @ApiResponse(responseCode = "409", description = "Conflit metier", content = @Content())
    })
    public Response patch(@PathParam("id") Long id, @Valid CategoryPatchDTO dto) {
        Category patched = categoryService.patch(id, dto.getLabel());
        return Response.ok(CategoryMapper.toDTO(patched)).build();
    }
}
