package services;

import dto.ClientDto;
import dto.MailDto;
import dto.PaysDto;
import entities.*;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import outils.HateOAS;
import outils.Link;
import outils.SecurityTools;
import outils.Validator;
import repo.*;
import restClient.MailClient;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

@Path("professionnel")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Client {

    @Context
    UriInfo uriInfo;
    @Context
    HttpServletRequest httpServletRequest;
    @RestClient
    MailClient mailClient;
    @Inject
    private UtilisateurRepo utilisateurRepo;
    @Inject
    private RoleRepo roleRepo;
    @Inject
    private ConnexionRepo connexionRepo;
    @Inject
    private Connexion connexion;
    @Inject
    private PaysRepo paysRepo;
    @Inject
    private VilleRepo villeRepo;
    @Inject
    private ClientRepo clientRepo;
    @Inject
    JsonWebToken jsonWebToken;
    @Inject
    HateOas hateOas;


    @Transactional
    @POST
    @APIResponse(responseCode = "200", description = "OK !")
    @APIResponse(responseCode = "250", description = "Clef Api Invalide !")
    @APIResponse(responseCode = "251", description = "Quota Clef API dépassé !")
    @APIResponse(responseCode = "400", description = "Format mail invalide !")
    @APIResponse(responseCode = "401", description = "Format password invalide !")
    @APIResponse(responseCode = "417", description = "Vous avez déjà un compte pro !")
    @Operation(summary = "Créer compte pro", description = "Créer compte pro")
    public Response creerComptePro(@HeaderParam("login") String login, @HeaderParam("password") String password) {

        if (utilisateurRepo.findClientByMail(login) != null)
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

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path("professionnel").path("confirm");
        uriBuilder.queryParam("code", cryptedParameters);

        String bodyResponse = String.format("Veuillez cliquer sur le lien suivant pour confirmer la création de votre compte : %s", uriBuilder.build());
        MailDto mail = new MailDto(login, "Confirmation de compte", LocalDateTime.now(), bodyResponse);
        Response response = mailClient.sendEmail(mail, "t56J6FiHFI8+dA==");
        if (response.getStatus() == 200) {
            UriBuilder uriAuthenticate = uriInfo.getBaseUriBuilder();
            hateOas.addLink(new Link("Authentification", HttpMethod.POST, uriAuthenticate.path("professionnel/authentification").build()));
            return Response.ok(hateOas).status(200).build();
        }
        return Response.serverError().build();
    }


    @Transactional
    @GET
    @Operation(hidden = true)
    @APIResponse(responseCode = "200", description = "Compte professionnel créé !")
    @APIResponse(responseCode = "400", description = "Lien expiré ou invalide !")
    @APIResponse(responseCode = "401", description = "Lien déjà utilisé !")
    @Path("/confirm")
    public Response confirmCreate(@QueryParam("code") String code) {

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
        RoleEntity roleEntity = roleRepo.findById(2004);
        if (utilisateurRepo.findClientByMail(user.getMail_utilisateur()) != null)
            return Response.ok("Le lien a déjà été utilisé !").status(401, "Le lien a déjà été utilisé !").build();
        user.setPassword(params[1]);
        user.setRoleEntity(roleRepo.findById(2004));
        user.setIsValidate(false);
        utilisateurRepo.persist(user);
        return Response.ok("Compte utilisateur créé").status(200).build();
    }

    @Transactional
    @POST
    @Path("/authentification")
    @APIResponse(responseCode = "200", description = "OK !")
    @APIResponse(responseCode = "401", description = "Connexion non autorisée (bloqué temporairement)")
    @APIResponse(responseCode = "404", description = "L'utilisateur n'existe pas")
    public Response authenticate(@HeaderParam("login") String login, @HeaderParam("password") String password) {

        UtilisateurEntity utilisateur = utilisateurRepo.findClientByMail(login);
        if (utilisateur == null)
            return Response.ok().status(404).build();

        Response responseAfterCheck = connexion.chekConnexions(login);
        if (responseAfterCheck.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode())
            return responseAfterCheck;

        if (BcryptUtil.matches(password, utilisateur.getPassword())) {
            if (utilisateur.getIsValidate() == false) {
                String token = SecurityTools.getfirstToken(utilisateur);
                UriBuilder uriValidation = uriInfo.getBaseUriBuilder();
                hateOas.addLink(new Link("Validation", HttpMethod.POST, uriValidation.path("professionnel/validation").build()));
                return Response.ok(hateOas).header("Authorization", "Bearer " + token).build();
            }
            String token = SecurityTools.getToken(utilisateur);
            connexion.deleteConnexionsByUser(login);
            return Response.ok().header("Authorization", "Bearer " + token).build();
        } else {
            connexion.addConnexion(login);
            return Response.status(401).build();
        }
    }

    @RolesAllowed("client")
    @Transactional
    @POST
    @Path("/validation")
    @APIResponse(responseCode = "200", description = "OK !")
    @APIResponse(responseCode = "401", description = "Login ou password invalide !")
    @APIResponse(responseCode = "403", description = "Pas les droits requis !")
    public Response validate(@HeaderParam("login") String login, @HeaderParam("password") String password,
                             ClientDto clientDto) {

        if (!jsonWebToken.containsClaim("isValidate"))
            return Response.status(Response.Status.UNAUTHORIZED).build();
        UtilisateurEntity utilisateur = utilisateurRepo.findClientByMail(login);
        if (utilisateur == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();
        if (!BcryptUtil.matches(password, utilisateur.getPassword()))
            return Response.status(Response.Status.UNAUTHORIZED).build();

        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setDenominationSociale(clientDto.getDenominationSociale());
        clientEntity.setNumeroRue(clientDto.getNumRue());
        clientEntity.setLibelleRue(clientDto.getLibelleRue());
        clientEntity.setComplementAdresse(clientDto.getComplementAdresse());
        clientEntity.setCodePostal(clientDto.getCodePostal());
        clientEntity.setNumTel(clientDto.getNumTel());
        clientEntity.setSiren(clientDto.getSiren());
        clientEntity.setVilleEntity(new VilleEntity(clientDto.getLibelleVille(), new PaysEntity(clientDto.getLibellePays())));
        clientEntity.setUtilisateurEntity(utilisateur);
        paysRepo.persist(clientEntity.getVilleEntity().getPaysEntity());
        villeRepo.persist(clientEntity.getVilleEntity());
        utilisateur.setIsValidate(true);
        clientRepo.persist(clientEntity);
        String token = SecurityTools.getToken(utilisateur);
        return Response.ok().header("Authorization", "Bearer " + token).build();
    }

    @RolesAllowed({"super-admin", "client"})
    @Transactional
    @DELETE
    @Path("/{login}/")
    @Operation(summary = "Delete client by mail")
    @APIResponse(responseCode = "200", description = "OK !")
    @APIResponse(responseCode = "403", description = "Accès interdit !")
    @APIResponse(responseCode = "500", description = "Echec supression !")
    public Response deleteByMail(@PathParam("login") String login) {
        if (jsonWebToken.getGroups().contains("client") && !jsonWebToken.getClaim("upn").equals(login))
            return Response.status(Response.Status.FORBIDDEN).build();
        try {
            UtilisateurEntity utilisateur = utilisateurRepo.findClientByMail(login);
            clientRepo.deleteClientByUserId(utilisateur.getId());
            utilisateurRepo.deleteClientByMail(login);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

}
