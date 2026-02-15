package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.controller;

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
public class AnnonceController {

    private final AnnonceService annonceService = new AnnonceService();

    @GET
    public Response getAll(@QueryParam("page") @DefaultValue("0") int page,
                           @QueryParam("size") @DefaultValue("10") int size) {
        List<Annonce> annonces = annonceService.findAllPaginated(page, size);
        long total = annonceService.count();
        List<AnnonceDTO> dtos = AnnonceMapper.toDTOList(annonces);
        return Response.ok(new PaginatedResponse<>(dtos, page, size, total)).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Annonce annonce = annonceService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce non trouvée avec l'id : " + id));
        return Response.ok(AnnonceMapper.toDTO(annonce)).build();
    }

    @POST
    @Secured
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
    public Response delete(@PathParam("id") Long id,
                           @Context ContainerRequestContext requestContext) {
        Long userId = (Long) requestContext.getProperty(AuthTokenFilter.USER_ID_PROPERTY);
        annonceService.delete(id, userId);
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{id}")
    @Secured
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
    public Response publish(@PathParam("id") Long id,
                            @Context ContainerRequestContext requestContext) {
        Long userId = (Long) requestContext.getProperty(AuthTokenFilter.USER_ID_PROPERTY);
        Annonce published = annonceService.publish(id, userId);
        return Response.ok(AnnonceMapper.toDTO(published)).build();
    }

    @PUT
    @Path("/{id}/archive")
    @Secured
    public Response archive(@PathParam("id") Long id,
                            @Context ContainerRequestContext requestContext) {
        Long userId = (Long) requestContext.getProperty(AuthTokenFilter.USER_ID_PROPERTY);
        Annonce archived = annonceService.archive(id, userId);
        return Response.ok(AnnonceMapper.toDTO(archived)).build();
    }
}
