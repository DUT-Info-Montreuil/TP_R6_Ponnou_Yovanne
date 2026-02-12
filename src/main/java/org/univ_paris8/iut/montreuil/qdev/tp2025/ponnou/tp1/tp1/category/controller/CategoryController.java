package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.controller;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dto.CategoryDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dto.CategoryCreateDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dto.CategoryUpdateDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dto.CategoryPatchDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.dto.CategoryMapper;
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
public class CategoryController {

    private final CategoryService categoryService = new CategoryService();

    @GET
    public Response getAll(@QueryParam("page") @DefaultValue("0") int page,
                           @QueryParam("size") @DefaultValue("10") int size) {
        List<Category> categories = categoryService.findAllPaginated(page, size);
        long total = categoryService.count();
        List<CategoryDTO> dtos = CategoryMapper.toDTOList(categories);
        return Response.ok(new PaginatedResponse<>(dtos, page, size, total)).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Category category = categoryService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée avec l'id : " + id));
        return Response.ok(CategoryMapper.toDTO(category)).build();
    }

    @POST
    @Secured
    public Response create(@Valid CategoryCreateDTO dto, @Context UriInfo uriInfo) {
        Category created = categoryService.create(dto.getLabel());
        CategoryDTO responseDTO = CategoryMapper.toDTO(created);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.getId())).build();
        return Response.created(location).entity(responseDTO).build();
    }

    @PUT
    @Path("/{id}")
    @Secured
    public Response update(@PathParam("id") Long id, @Valid CategoryUpdateDTO dto) {
        Category updated = categoryService.update(id, dto.getLabel());
        return Response.ok(CategoryMapper.toDTO(updated)).build();
    }

    @DELETE
    @Path("/{id}")
    @Secured
    public Response delete(@PathParam("id") Long id) {
        categoryService.delete(id);
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{id}")
    @Secured
    public Response patch(@PathParam("id") Long id, @Valid CategoryPatchDTO dto) {
        Category patched = categoryService.patch(id, dto.getLabel());
        return Response.ok(CategoryMapper.toDTO(patched)).build();
    }
}
