package services;

import dto.MailDto;
import entities.UtilisateurEntity;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import outils.SecurityTools;
import outils.Validator;
import repo.ConnexionRepo;
import repo.RoleRepo;
import repo.UtilisateurRepo;
import restClient.MailClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

@Path("super-admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SuperAdmin {
    @Context
    UriInfo uriInfo;
    @RestClient
    private MailClient mailClient;
    @Inject
    private RoleRepo roleRepo;
    @Inject
    private UtilisateurRepo utilisateurRepo;
    @Inject
    private Connexion connexion;

    @RolesAllowed("super-admin")
    @Transactional
    @POST
    @APIResponse(responseCode = "200", description = "OK !")
    @APIResponse(responseCode = "250", description = "Clef Api Invalide !")
    @APIResponse(responseCode = "251", description = "Quota Clef API dépassé !")
    @APIResponse(responseCode = "400", description = "Format mail invalide !")
    @APIResponse(responseCode = "401", description = "Format password invalide !")
    @APIResponse(responseCode = "403", description = "Token invalide !")
    @APIResponse(responseCode = "417", description = "Le login existe déjà !")
    @Operation(summary = "Créer compte super-admin", description = "Créer compte super-admin")
    public Response creerCompteSuperAdmin(@HeaderParam("login") String login, @HeaderParam("password") String password) {

        if (utilisateurRepo.findSaByMail(login) != null)
            return Response.status(417).build();
        if (!Validator.isMailValid(login))
            return Response.status(400, "Adresse mail non valide !").build();
        /*if (!Validator.isValidPassword(password))
            return Response.status(401, "MDP invalide !").build();*/

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 10);
        Date expiration = calendar.getTime();
        String plainTextParameters = String.format("%s|%s|%s",
                login,
                BcryptUtil.bcryptHash(password, 31, "MyPersonnalSalt0".getBytes()),
                new SimpleDateFormat("dd-MM-yy-HH:mm:ss").format(expiration));
        String cryptedParameters = SecurityTools.encrypt(plainTextParameters);

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path("super-admin").path("confirm");
        uriBuilder.queryParam("code", cryptedParameters);

        String bodyResponse = String.format("Veuillez cliquer sur le lien suivant pour confirmer la création de votre compte : %s", uriBuilder.build());
        MailDto mail = new MailDto(login, "Confirmation de compte", LocalDateTime.now(), bodyResponse);
        Response response = mailClient.sendEmail(mail, "t56J6FiHFI8+dA==");
        if (response.getStatus() == 200)
            return Response.ok("Un mail de confirmation vous a été envoyé !").status(200).build();
        return response;
    }


    @Transactional
    @GET
    @APIResponse(responseCode = "400", description = "Lien expiré ou invalide !")
    @APIResponse(responseCode = "401", description = "Lien déjà utilisé !")
    @Path("/confirm")
    public Response confirmCreateSuperAdmin(@QueryParam("code") String code) {

        String[] params = SecurityTools.decrypt(code).split("\\|");
        try {
            Date expireAt = new SimpleDateFormat("dd-MM-yy-HH:mm:ss").parse(params[2]);
            if (expireAt.before(Calendar.getInstance().getTime()))
                return Response.ok("Le lien n'est plus valide !").status(400, "Le lien n'est plus valide !").build();
        } catch (ParseException e) {
            return Response.ok("Le lien n'est pas valide !").status(400, "Le lien n'est pas valide !").build();
        }

        UtilisateurEntity user = new UtilisateurEntity();
        user.setMail_utilisateur(params[0]);
        if (utilisateurRepo.findSaByMail(user.getMail_utilisateur()) != null)
            return Response.ok("Le lien a déjà été utilisé !").status(401, "Le lien a déjà été utilisé !").build();
        user.setPassword(params[1]);
        user.setRoleEntity(roleRepo.findById(2005));
        user.setIsValidate(true);
        utilisateurRepo.persist(user);

        return Response.ok("Compte super-admin créé").status(200).build();
    }


    @Transactional
    @POST
    @Path("/authentification")
    @APIResponse(responseCode = "200", description = "OK !")
    @APIResponse(responseCode = "401", description = "Connexion non autorisée (bloqué temporairement)")
    @APIResponse(responseCode = "404", description = "L'utilisateur n'existe pas")
    public Response authenticate(@HeaderParam("login") String login, @HeaderParam("password") String password) {

        UtilisateurEntity utilisateur = utilisateurRepo.findSaByMail(login);
        if (utilisateur == null)
            return Response.ok().status(404).build();

        Response responseAfterCheck = connexion.chekConnexions(login);
        if (responseAfterCheck.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode())
            return responseAfterCheck;

        if (BcryptUtil.matches(password, utilisateur.getPassword())) {
            String token = SecurityTools.getToken(utilisateur);
            connexion.deleteConnexionsByUser(login);
            return Response.ok().header("Authorization", "Bearer " + token).build();
        } else {
            connexion.addConnexion(login);
            return Response.status(401).build();
        }
    }

    @RolesAllowed("super-admin")
    @Transactional
    @DELETE
    @Path("/{login}/")
    @Operation(summary = "Delete connexions by user")
    @APIResponse(responseCode = "200", description = "OK !")
    @APIResponse(responseCode = "500", description = "Echec supression !")
    public Response deleteSuperAById(@PathParam("login") String login) {

        try {
            utilisateurRepo.deleteSaByMail(login);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

}
