package services;

import dto.RoleDto;
import entities.RoleEntity;
import io.quarkus.runtime.annotations.CommandLineArguments;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import outils.HateOAS;
import outils.Link;
import repo.RoleRepo;

import java.util.ArrayList;

@Path("/role/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Role {

    @Inject
    RoleRepo roleRepo;
    @Inject
    UriInfo uriInfo;
    @Inject
    HateOAS hateOAS;

    @RolesAllowed("super-admin")
    @GET
    @APIResponse(responseCode = "200", description = "OK !")
    public Response getAll(){
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
        ArrayList<RoleDto> listeRoleDto = new ArrayList<>(RoleDto.toDtoList(roleRepo.listAll(),uriBuilder));
        return Response.ok(listeRoleDto).build();
    }

    @RolesAllowed("super-admin")
    @Transactional
    @POST
    @APIResponse(responseCode = "200", description = "OK !")
    public Response insert(RoleDto roleDto){
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setLibelleRole(roleDto.getLibelleRole());
        roleRepo.persist(roleEntity);
        UriBuilder uriValidation = uriInfo.getBaseUriBuilder();
        hateOAS.addLink(new Link("Get All", HttpMethod.GET, uriValidation.path("role").build()));
        return Response.ok(hateOAS).status(200).build();
    }

    @RolesAllowed("super-admin")
    @Transactional
    @DELETE
    @Path("{libelle}")
    @APIResponse(responseCode = "200", description = "OK !")
    @APIResponse(responseCode = "500", description = "Un utilisateur possède ce rôle !")
    public Response delete(@PathParam("libelle") String libelle){
        UriBuilder uriValidation = uriInfo.getBaseUriBuilder();
        hateOAS.addLink(new Link("Insérer rôle", HttpMethod.POST, uriValidation.path("role").build()));
        roleRepo.deleteByLibelle(libelle);
        return Response.ok(hateOAS).status(200).build();
    }
}
