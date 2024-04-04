package services;

import dto.EmployeDto;
import dto.EmployeValidateDto;
import dto.MailDto;
import entities.*;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import outils.Link;
import outils.SecurityTools;
import outils.Validator;
import repo.ClientRepo;
import repo.EmployeRepo;
import repo.RoleRepo;
import repo.UtilisateurRepo;
import restClient.MailClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Collectors;

@Path("employe")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Employe {

    @Context
    UriInfo uriInfo;
    @RestClient
    MailClient mailClient;
    @Inject
    private UtilisateurRepo utilisateurRepo;
    @Inject
    private JsonWebToken jsonWebToken;
    @Inject
    private RoleRepo roleRepo;
    @Inject
    private EmployeRepo employeRepo;
    @Inject
    private HateOas hateOas;
    @Inject
    private ClientRepo clientRepo;


    @RolesAllowed("client")
    @Transactional
    @POST
    @APIResponse(responseCode = "200", description = "OK !")
    @APIResponse(responseCode = "250", description = "Clef Api Invalide !")
    @APIResponse(responseCode = "251", description = "Quota Clef API dépassé !")
    @APIResponse(responseCode = "400", description = "Format mail invalide !")
    @APIResponse(responseCode = "401", description = "Format password invalide !")
    @APIResponse(responseCode = "403", description = "Vous n'avez pas les droits !")
    @APIResponse(responseCode = "417", description = "Cet employé existe déjà")
    @Operation(summary = "Créer compte employé", description = "Créer compte employé")
    public Response creerCompteEmploye(@HeaderParam("login") String login, @HeaderParam("password") String password) {

        if (!jsonWebToken.getGroups().contains("client"))
            return Response.status(Response.Status.FORBIDDEN).build();
        if (!Validator.isMailValid(login))
            return Response.status(400, "Adresse mail non valide !").build();
        if (utilisateurRepo.findEmployeByMail(login) != null)
            return Response.status(417).build();

        int idUser = Integer.valueOf(jsonWebToken.getClaim("id"));
        ClientEntity clientEntity = clientRepo.findByIdUser(idUser);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 10);
        Date expiration = calendar.getTime();
        String plainTextParameters = String.format("%s|%s|%s|%s",
                login,
                BcryptUtil.bcryptHash(password, 31, "MyPersonnalSalt0".getBytes()),
                new SimpleDateFormat("dd-MM-yy-HH:mm:ss").format(expiration),
                clientEntity.getId());

        String cryptedParameters = SecurityTools.encrypt(plainTextParameters);

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path("employe").path("confirm");
        uriBuilder.queryParam("code", cryptedParameters);

        String bodyResponse = String.format("Veuillez cliquer sur le lien suivant pour confirmer la création de votre compte employé: %s", uriBuilder.build());
        MailDto mail = new MailDto(login, "Confirmation de compte", LocalDateTime.now(), bodyResponse);
        Response response = mailClient.sendEmail(mail, "t56J6FiHFI8+dA==");
        if (response.getStatus() == 200) {
            UriBuilder uriAuthenticate = uriInfo.getBaseUriBuilder();
            hateOas.addLink(new Link("Authentification", HttpMethod.POST, uriAuthenticate.path("employe/authentification").build()));
            return Response.ok(hateOas).status(200).build();
        }
        return Response.serverError().build();


    }

    @Transactional
    @GET
    @Operation(hidden = true)
    @APIResponse(responseCode = "200", description = "Compte employé créé !")
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
        RoleEntity roleEntity = roleRepo.findById(4005);
        if (utilisateurRepo.findEmployeByMail(user.getMail_utilisateur()) != null)
            return Response.ok("Le lien a déjà été utilisé !").status(401, "Le lien a déjà été utilisé !").build();
        user.setPassword(params[1]);
        user.setIdClient(Integer.valueOf(params[3]));
        user.setRoleEntity(roleEntity);
        utilisateurRepo.persist(user);
        return Response.ok("Compte employé créé").status(200).build();

    }

    @Transactional
    @POST
    @Path("/authentification")
    @APIResponse(responseCode = "200", description = "OK !")
    @APIResponse(responseCode = "404", description = "L'utilisateur n'existe pas")
    public Response authenticate(@HeaderParam("login") String login, @HeaderParam("password") String password) {

        UtilisateurEntity utilisateurEntity = utilisateurRepo.findEmployeByMail(login);

        if (utilisateurEntity == null)
            return Response.ok().status(404).build();

        if (BcryptUtil.matches(password, utilisateurEntity.getPassword())) {
            if (utilisateurEntity.getRoleEntity().getLibelleRole().equals("intermediaire")) {
                String token = SecurityTools.getToken(utilisateurEntity);
                UriBuilder uriValidation = uriInfo.getBaseUriBuilder();
                hateOas.addLink(new Link("Validation", HttpMethod.POST, uriValidation.path("employe/validation").build()));
                return Response.ok(hateOas).header("Authorization", "Bearer " + token).build();
            }
            String token = SecurityTools.getToken(utilisateurEntity);
            return Response.ok().header("Authorization", "Bearer " + token).build();
        } else {
            return Response.status(401).build();
        }

    }

    @RolesAllowed("intermediaire")
    @Transactional
    @POST
    @Path("/validation")
    @APIResponse(responseCode = "200", description = "OK !")
    @APIResponse(responseCode = "401", description = "Login ou password invalide !")
    @APIResponse(responseCode = "403", description = "Pas les droits requis !")
    public Response validate(@HeaderParam("login") String login, @HeaderParam("password") String password,
                             EmployeValidateDto employeDto) {

        UtilisateurEntity utilisateur = utilisateurRepo.findEmployeByMail(login);
        if (utilisateur == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();
        if (!utilisateur.getRoleEntity().getLibelleRole().equals("intermediaire"))
            return Response.status(Response.Status.UNAUTHORIZED).build();
        if (!BcryptUtil.matches(password, utilisateur.getPassword()))
            return Response.status(Response.Status.UNAUTHORIZED).build();

        ClientEntity clientEntity = clientRepo.findById(utilisateur.getIdClient());

        EmployeEntity employeEntity = new EmployeEntity();
        employeEntity.setMailEmploye(login);
        employeEntity.setNomEmploye(employeDto.getNom());
        employeEntity.setPrenomEmploye(employeDto.getPrenom());
        employeEntity.setClientEntity(clientEntity);
        employeEntity.setUtilisateurEntity(utilisateur);
        utilisateur.setPassword(BcryptUtil.bcryptHash(password, 31, "MyPersonnalSalt0".getBytes()));
        utilisateur.setRoleEntity(roleRepo.findById(2006));
        employeRepo.persist(employeEntity);
        String token = SecurityTools.getToken(utilisateur);
        return Response.ok().header("Authorization", "Bearer " + token).build();
    }

    @RolesAllowed({"client"})
    @Transactional
    @DELETE
    @Path("/{id}/")
    @Operation(summary = "Delete employé by id")
    @APIResponse(responseCode = "200", description = "OK !")
    @APIResponse(responseCode = "403", description = "Accès interdit !")
    @APIResponse(responseCode = "404", description = "L'utilisateur n'existe pas")
    @APIResponse(responseCode = "500", description = "Echec supression !")
    public Response deleteById(@PathParam("id") int id) {

        EmployeEntity employeEntity = employeRepo.findById(id);
        UtilisateurEntity utilisateurEntity = utilisateurRepo.findById(employeEntity.getUtilisateurEntity().getId());
        if (employeEntity == null || utilisateurEntity == null)
            return Response.status(Response.Status.NOT_FOUND).build();

        int idUserClient = Integer.valueOf(jsonWebToken.getClaim("id"));

        if (idUserClient != employeEntity.getClientEntity().getUtilisateurEntity().getId())
            return Response.status(Response.Status.UNAUTHORIZED).build();

        try {
            employeRepo.deleteById(id);
            utilisateurRepo.deleteById(utilisateurEntity.getId());
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @RolesAllowed("client")
    @GET
    @APIResponse(responseCode = "200", description = "OK !")
    public Response getAll(){
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();

        int idUserOfCLient = Integer.valueOf(jsonWebToken.getClaim("id"));
        ClientEntity clientEntity = clientRepo.findByIdUser(idUserOfCLient);
        ArrayList<EmployeDto> listeEmployDto = new ArrayList<>(EmployeDto.toDtoList(employeRepo.findEmployeByIdClient(clientEntity.getId()),uriBuilder));
        return Response.ok(listeEmployDto).build();
    }
}

