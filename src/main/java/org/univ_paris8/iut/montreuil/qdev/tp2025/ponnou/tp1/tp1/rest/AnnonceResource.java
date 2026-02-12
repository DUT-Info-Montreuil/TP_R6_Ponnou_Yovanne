package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.rest;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.dto.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.service.AnnonceService;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

@Path("/annonces")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AnnonceResource {

    private final AnnonceService annonceService = new AnnonceService();

    /**
     * GET /api/annonces?page=0&size=10
     * Liste paginee de toutes les annonces.
     */
    @GET
    public Response getAll(@QueryParam("page") @DefaultValue("0") int page,
                           @QueryParam("size") @DefaultValue("10") int size) {
        List<Annonce> annonces = annonceService.findAllPaginated(page, size);
        long total = annonceService.count();
        List<AnnonceDTO> dtos = AnnonceMapper.toDTOList(annonces);
        return Response.ok(new PaginatedResponse<>(dtos, page, size, total)).build();
    }

    /**
     * GET /api/annonces/{id}
     * Detail d'une annonce.
     */
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Annonce annonce = annonceService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Annonce non trouvée avec l'id : " + id));
        return Response.ok(AnnonceMapper.toDTO(annonce)).build();
    }

    /**
     * POST /api/annonces
     * Creation d'une annonce. Retourne 201 Created avec le header Location.
     */
    @POST
    public Response create(AnnonceCreateDTO dto, @Context UriInfo uriInfo) {
        Annonce created = annonceService.create(
                dto.getTitle(),
                dto.getDescription(),
                dto.getAdress(),
                dto.getMail(),
                dto.getAuthorId(),
                dto.getCategoryId()
        );
        AnnonceDTO responseDTO = AnnonceMapper.toDTO(created);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.getId())).build();
        return Response.created(location).entity(responseDTO).build();
    }

    /**
     * PUT /api/annonces/{id}
     * Mise a jour complete d'une annonce.
     */
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, AnnonceUpdateDTO dto) {
        Annonce updated = annonceService.update(
                id,
                dto.getTitle(),
                dto.getDescription(),
                dto.getAdress(),
                dto.getMail(),
                dto.getCategoryId()
        );
        return Response.ok(AnnonceMapper.toDTO(updated)).build();
    }

    /**
     * DELETE /api/annonces/{id}
     * Suppression d'une annonce. Retourne 204 No Content.
     */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        annonceService.delete(id);
        return Response.noContent().build();
    }

    /**
     * PATCH /api/annonces/{id}
     * Mise a jour partielle d'une annonce (bonus).
     * Seuls les champs presents dans le body JSON sont modifies.
     * Permet par exemple de ne changer que le titre sans toucher au reste.
     */
    @PATCH
    @Path("/{id}")
    public Response patch(@PathParam("id") Long id, AnnoncePatchDTO dto) {
        Annonce patched = annonceService.patch(
                id,
                dto.getTitle(),
                dto.getDescription(),
                dto.getAdress(),
                dto.getMail(),
                dto.getCategoryId()
        );
        return Response.ok(AnnonceMapper.toDTO(patched)).build();
    }
}
