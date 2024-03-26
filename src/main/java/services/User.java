package services;

import dto.MailDto;
import entities.UtilisateurEntity;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import outils.SecurityTools;
import outils.Validator;
import repo.RoleRepo;
import repo.UtilisateurRepo;
import restClient.MailClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

@Path("user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class User {

    @Inject
    UriInfo uriInfo;
    @RestClient
    private MailClient mailClient;
    @Inject
    private RoleRepo roleRepo;
    @Inject
    private UtilisateurRepo utilisateurRepo;



    @Transactional
    @POST
    @APIResponse(responseCode = "200", description = "OK !")
    @APIResponse(responseCode = "250", description = "Clef Api Invalide !")
    @APIResponse(responseCode = "251", description = "Quota Clef API dépassé !")
    @APIResponse(responseCode = "400", description = "Format mail invalide !")
    @APIResponse(responseCode = "401", description = "Format password invalide !")
    @APIResponse(responseCode = "417", description = "Le login existe déjà !")
    @Operation(summary = "Créer compte user", description = "Créer compte user")
    public Response creerCompteConsommateur(@HeaderParam("login") String login, @HeaderParam("password") String password) {

        if (utilisateurRepo.findById(login) != null)
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

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path("user").path("confirm");
        uriBuilder.queryParam("code", cryptedParameters);

        String bodyResponse = String.format("Veuillez cliquer sur le lien suivant pour confirmer la création de votre compte : %s", uriBuilder.build());
        MailDto mail = new MailDto(login,"Confirmation de compte", LocalDateTime.now(), bodyResponse);
        Response response = mailClient.sendEmail(mail, "t56J6FiHFI8+dA==");
        if (response.getStatus() == 200)
            return Response.ok("Un mail de confirmation vous a été envoyé !").status(200).build();
        return response;
    }


    @Transactional
    @GET
    @APIResponse(responseCode = "200", description = "Compte user créé !")
    @APIResponse(responseCode = "400", description = "Lien expiré ou invalide !")
    @APIResponse(responseCode = "401", description = "Lien déjà utilisé !")
    @Path("/confirm")
    public Response confirmCreateEmploye(@QueryParam("code") String code) {

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
        if (utilisateurRepo.findById(user.getMail_utilisateur()) != null)
            return Response.ok("Le lien a déjà été utilisé !").status(401, "Le lien a déjà été utilisé !").build();
        user.setPassword(params[1]);
        user.setRoleEntity(roleRepo.findById(5));
        user.setIsValidate(false);
        utilisateurRepo.persist(user);

        return Response.ok("Compte user créé").status(200).build();
    }

    @Transactional
    @DELETE
    @Path("/{login}/")
    @Operation(hidden = true, summary = "Delete connexions by user")
    @APIResponse(responseCode = "200", description = "OK !")
    @APIResponse(responseCode = "500", description = "Echec supression !")
    public Response deleteUserById(@PathParam("login") String login) {
        try {
            utilisateurRepo.deleteById(login);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

}
