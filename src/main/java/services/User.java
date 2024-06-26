package services;

import dto.MailDto;
import entities.UtilisateurEntity;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import outils.Otp;
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
    @Inject
    private Connexion connexion;
    @Inject
    JsonWebToken jsonWebToken;



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

        if (utilisateurRepo.findUserByMail((login)) != null)
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
    @Operation( hidden = true)
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
        if (utilisateurRepo.findUserByMail(user.getMail_utilisateur()) != null)
            return Response.ok("Le lien a déjà été utilisé !").status(401, "Le lien a déjà été utilisé !").build();
        user.setPassword(params[1]);
        user.setRoleEntity(roleRepo.findById(5));
        utilisateurRepo.persist(user);

        return Response.ok("Compte user créé").status(200).build();
    }

    @Transactional
    @POST
    @Path("/authentification")
    @APIResponse(responseCode = "200", description = "OK !")
    @APIResponse(responseCode = "401", description = "Connexion non autorisée (bloqué temporairement)")
    @APIResponse(responseCode = "404", description = "L'utilisateur n'existe pas")
    public Response authenticate(@HeaderParam("login") String login, @HeaderParam("password") String password) {

        UtilisateurEntity utilisateur = utilisateurRepo.findUserByMail(login);
        if (utilisateur == null)
            return Response.ok().status(404).build();

        Response responseAfterCheck = connexion.chekConnexions(login);
        if (responseAfterCheck.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode())
            return responseAfterCheck;

        if (BcryptUtil.matches(password, utilisateur.getPassword())) {
            String otp = Otp.generateOtp();
            String token = SecurityTools.getTokenOtp(utilisateur, otp);
            MailDto mailDto = new MailDto(utilisateur.getMail_utilisateur(), "Double authentification",LocalDateTime.now(),"Code : " + otp);
            mailClient.sendEmail(mailDto, "t56J6FiHFI8+dA==");
            connexion.deleteConnexionsByUser(login);
            return Response.ok().header("Authorization", "Bearer " + token).build();
        } else {
            connexion.addConnexion(login);
            return Response.status(401).build();
        }
    }



    @RolesAllowed({"User","super-admin"})
    @Transactional
    @DELETE
    @Path("/{login}/")
    @Operation(summary = "Delete connexions by user")
    @APIResponse(responseCode = "200", description = "OK !")
    @APIResponse(responseCode = "403", description = "Accès interdit !")
    @APIResponse(responseCode = "500", description = "Echec supression !")
    public Response deleteUserById(@PathParam("login") String login) {
        if(jsonWebToken.getGroups().contains("User") && !jsonWebToken.getClaim("upn").equals(login))
            return Response.status(Response.Status.FORBIDDEN).build();
        try {
            utilisateurRepo.deleteUserByMail(login);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    /*@GET
    @Produces({MediaType.TEXT_HTML})
    @Consumes(MediaType.APPLICATION_JSON)
    @APIResponse(responseCode = "200", description = "OK !")
    @APIResponse(responseCode = "250", description = "Clef Api Invalide !")
    @APIResponse(responseCode = "251", description = "Quota mail dépassé !")
    @APIResponse(responseCode = "400", description = "Mail invalide !")
    @APIResponse(responseCode = "400", description = "Mail non renseigné")
    @APIResponse(responseCode = "401", description = "Pas de compte associé à ce mail")
    @Path("forgotPassword/{id}/")
    public Response getNewPassword(@PathParam("id") String email) {
        if (email == null)
            return Response.status(400).build();

        UserEntity userEntity = userRepo.findById(email);
        if (userEntity == null)
            return Response.status(401).build();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 10);
        Date expiration = calendar.getTime();
        String plainTextParameters = String.format("%s|%s",
                userEntity.getMail_utilisateur(),
                new SimpleDateFormat("dd-MM-yy-HH:mm:ss").format(expiration));
        String cryptedParameters = SecurityTools.encrypt(plainTextParameters);

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path("users").path(userEntity.getMail_utilisateur());
        uriBuilder.queryParam("code", cryptedParameters);

        String bodyResponse = String.format("Veuillez cliquer sur le lien suivant pour confirmer la modification du password : %s", uriBuilder.build());

        MailDto mail = new MailDto();
        mail.setSendTo(email);
        mail.setSubject("Réinitialisation de mot de passe");
        mail.setTexte(bodyResponse);
        Response response = mailClient.sendEmail(mail, "SECRET_KEY");

        if (response.getStatus() == 200)
            return Response.ok("Un mail de confirmation vous a été envoyé !").status(200).build();
        return response;
    }


    @Transactional
    @PUT
    @Produces({MediaType.TEXT_HTML})
    @Consumes(MediaType.APPLICATION_JSON)
    @APIResponse(responseCode = "200", description = "OK !")
    @APIResponse(responseCode = "400", description = "Lien expiré !")
    @APIResponse(responseCode = "401", description = "Lien invalide !")
    @APIResponse(responseCode = "402", description = "Identifiant erroné !")
    @Path("newPassword/{id}/")
    public Response confirmNewPassword(@PathParam("id") String mail, @QueryParam("code") String
            code, UtilisateurPasswordDto userPasswordDto) {

        String[] params = SecurityTools.decrypt(code).split("\\|");

        UserEntity user = userRepo.findById(params[0]);
        if (mail != user.getMail_utilisateur())
            return Response.status(402).build();
        try {
            Date expireAt = new SimpleDateFormat("dd-MM-yy-HH:mm:ss").parse(params[1]);
            if (expireAt.before(Calendar.getInstance().getTime()))
                return Response.ok("Le lien n'est plus valide !").status(400, "Le lien n'est plus valide !").build();
        } catch (ParseException e) {
            return Response.ok("Le lien n'est pas valide !").status(401, "Le lien n'est pas valide !").build();
        }

        String passwordHash = BcryptUtil.bcryptHash(userPasswordDto.getPassword(), 31, "MyPersonnalSalt0".getBytes());
        user.setPassword(passwordHash);
        userRepo.persist(user);
        return Response.ok().build();
    }

     */

}
