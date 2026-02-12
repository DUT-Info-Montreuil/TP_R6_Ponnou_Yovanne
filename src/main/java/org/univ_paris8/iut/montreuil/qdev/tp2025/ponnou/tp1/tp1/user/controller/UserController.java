package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.controller;

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
public class UserController {

    private final UserService userService = new UserService();

    @GET
    public Response getAll(@QueryParam("page") @DefaultValue("0") int page,
                           @QueryParam("size") @DefaultValue("10") int size) {
        List<User> users = userService.findAllPaginated(page, size);
        long total = userService.count();
        List<UserDTO> dtos = UserMapper.toDTOList(users);
        return Response.ok(new PaginatedResponse<>(dtos, page, size, total)).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + id));
        return Response.ok(UserMapper.toDTO(user)).build();
    }

    @POST
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
    public Response delete(@PathParam("id") Long id) {
        userService.delete(id);
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{id}")
    @Secured
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
