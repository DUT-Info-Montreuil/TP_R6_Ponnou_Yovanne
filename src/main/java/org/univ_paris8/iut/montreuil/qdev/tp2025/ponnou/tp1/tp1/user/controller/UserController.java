package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.dto.UserDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.dto.UserCreateDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.dto.UserUpdateDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.mapper.UserMapper;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.dto.UserPatchDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.service.UserService;
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

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "Gestion des utilisateurs")
public class UserController {

    private final UserService userService = new UserService();

    @GET
    @Operation(summary = "Lister les utilisateurs", description = "Retourne la liste paginee des utilisateurs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des utilisateurs retournee", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content())
    })
    public Response getAll(@QueryParam("page") @DefaultValue("0") int page,
                           @QueryParam("size") @DefaultValue("10") int size) {
        List<User> users = userService.findAllPaginated(page, size);
        long total = userService.count();
        List<UserDTO> dtos = UserMapper.toDTOList(users);
        return Response.ok(new PaginatedResponse<>(dtos, page, size, total)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Recuperer un utilisateur", description = "Retourne le detail d'un utilisateur par son id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur retourne", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouve", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content())
    })
    public Response getById(@PathParam("id") Long id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + id));
        return Response.ok(UserMapper.toDTO(user)).build();
    }

    @POST
    @Operation(summary = "Creer un utilisateur", description = "Cree un nouveau compte utilisateur.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Utilisateur cree", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Requete invalide", content = @Content()),
            @ApiResponse(responseCode = "409", description = "Conflit metier", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content())
    })
    public Response create(@Valid UserCreateDTO dto, @Context UriInfo uriInfo) {
        User created = userService.create(
                dto.getUsername(),
                dto.getEmail(),
                dto.getPassword()
        );
        UserDTO responseDTO = UserMapper.toDTO(created);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.getId())).build();
        return Response.created(location).entity(responseDTO).build();
    }

    @PUT
    @Path("/{id}")
    @Secured
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mettre a jour un utilisateur", description = "Met a jour un utilisateur existant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur mis a jour", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Requete invalide", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Non authentifie", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouve", content = @Content()),
            @ApiResponse(responseCode = "409", description = "Conflit metier", content = @Content())
    })
    public Response update(@PathParam("id") Long id, @Valid UserUpdateDTO dto) {
        User updated = userService.update(
                id,
                dto.getUsername(),
                dto.getEmail()
        );
        return Response.ok(UserMapper.toDTO(updated)).build();
    }

    @DELETE
    @Path("/{id}")
    @Secured
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Supprimer un utilisateur", description = "Supprime un utilisateur.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Utilisateur supprime", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Non authentifie", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouve", content = @Content()),
            @ApiResponse(responseCode = "409", description = "Conflit metier", content = @Content())
    })
    public Response delete(@PathParam("id") Long id) {
        userService.delete(id);
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{id}")
    @Secured
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Patch partiel d'un utilisateur", description = "Met a jour partiellement un utilisateur.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur mis a jour", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Requete invalide", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Non authentifie", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouve", content = @Content()),
            @ApiResponse(responseCode = "409", description = "Conflit metier", content = @Content())
    })
    public Response patch(@PathParam("id") Long id, @Valid UserPatchDTO dto) {
        User patched = userService.patch(
                id,
                dto.getUsername(),
                dto.getEmail(),
                dto.getPassword()
        );
        return Response.ok(UserMapper.toDTO(patched)).build();
    }
}
