package services;

import entities.ConnexionEntity;
import entities.UtilisateurEntity;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import repo.ConnexionRepo;
import repo.UtilisateurRepo;

import java.time.Duration;
import java.time.LocalDateTime;

@Path("connexion")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Connexion {
    @Inject
    ConnexionRepo connexionRepo;
    @Inject
    UtilisateurRepo userRepo;

    @GET
    @Path("/{login}/")
    @Operation(hidden = true, summary = "Check connexions")
    @APIResponse(responseCode = "200", description = "OK !")
    @APIResponse(responseCode = "401", description = "Non autorisé (temporairement bloqué)")
    public Response chekConnexions(@PathParam("login") String login) {

        long countFailedConnexions = connexionRepo.getConnexionFailedCount(login);
        try {
            LocalDateTime blockingSince = connexionRepo.getLastConnexion(login).getDateFail();

            if (countFailedConnexions >= 5 && !blockingSince.plusHours(24).isBefore(LocalDateTime.now())) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Accès bloqué pendant 24 heures en raison de 5 connexions échouées.")
                        .header("Retry-After", Duration.ofHours(24).getSeconds())
                        .build();
            } else if (countFailedConnexions == 4 && !blockingSince.plusMinutes(15).isBefore(LocalDateTime.now())) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Accès bloqué pendant 15 minutes en raison de 4 connexions échouées.")
                        .header("Retry-After", Duration.ofMinutes(15).getSeconds())
                        .build();
            } else if (countFailedConnexions == 3 && !blockingSince.plusMinutes(2).isBefore(LocalDateTime.now())) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Accès bloqué pendant 2 minutes en raison de 3 connexions échouées.")
                        .header("Retry-After", Duration.ofMinutes(2).getSeconds())
                        .build();
            } else
                return Response.ok().status(200).build();
        } catch (NullPointerException e) {
            return Response.status(200).build();
        }

    }

    @Transactional
    @POST
    @Path("/{login}/")
    @Operation(hidden = true, summary = "Add connexion")
    @APIResponse(responseCode = "200", description = "OK !")
    @APIResponse(responseCode = "404", description = "L'utilisateur n'existe pas !")
    @APIResponse(responseCode = "500", description = "Echec persistence !")
    public Response addConnexion(@PathParam("login") String login) {

        try {
            UtilisateurEntity userEntity = userRepo.findById(login);
            if (userEntity == null)
                return Response.status(417).entity("L'utilisateur n'existe pas !").build();

            ConnexionEntity connexionEntity = new ConnexionEntity();
            connexionEntity.setUserEntity(userEntity);
            connexionEntity.setDateFail(LocalDateTime.now());
            connexionRepo.persist(connexionEntity);
            return Response.ok().status(200).build();

        } catch (PersistenceException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Erreur lors de l'ajout de la Connexion !").build();

        }
    }

    @Transactional
    @DELETE
    @Path("/{login}/")
    @Operation(hidden = true, summary = "Delete connexions by user")
    @APIResponse(responseCode = "200", description = "OK !")
    @APIResponse(responseCode = "500", description = "Echec supression !")
    public Response deleteConnexionsByUser(@PathParam("login") String login) {
        try {
            connexionRepo.delete("userEntity.mail_utilisateur = ?1", login);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }
}
