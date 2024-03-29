package services;

import entities.UtilisateurEntity;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import outils.SecurityTools;
import repo.UtilisateurRepo;

@Path("double-authenticate")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DoubleAuth {

    @Inject
    JsonWebToken jsonWebToken;
    @Inject
    UtilisateurRepo utilisateurRepo;

    @RolesAllowed({"User"})
    @Transactional
    @POST
    @APIResponse(responseCode = "403", description = "Connexion non autorisée")
    @APIResponse(responseCode = "404", description = "Identifiant ou mot de passe incorrect")
    @APIResponse(responseCode = "401", description = "Code OTP incorrect")
    @APIResponse(responseCode = "200", description = "Authentification réussie")
    @Operation(summary = "For User only", description = "For User only")
    public Response authenticate(@HeaderParam("login") String login,
                                 @HeaderParam("password") String password,
                                 @HeaderParam("otp") String otp) {
        UtilisateurEntity utilisateur = utilisateurRepo.findByMail(login);
        if (utilisateur == null)
            return Response.ok().status(404).build();
        if (!BcryptUtil.matches(password, utilisateur.getPassword()))
            return Response.ok().status(404).build();
        if (!jsonWebToken.getClaim("otp").equals(otp))
            return Response.status(Response.Status.FORBIDDEN).build();
        String token = SecurityTools.getToken(utilisateur);
        return Response.ok().header("Authorization", "Bearer " + token).build();
    }
}
