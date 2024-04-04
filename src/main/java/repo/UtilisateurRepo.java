package repo;

import entities.ClientEntity;
import entities.RoleEntity;
import entities.UtilisateurEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.RequestScoped;
import services.Client;

@RequestScoped
public class UtilisateurRepo implements PanacheRepositoryBase<UtilisateurEntity, Integer> {

    public UtilisateurEntity findClientByMail(String mail) {
        RoleEntity roleEntity = new RoleEntity(2004,"client");
        RoleEntity roleEntity2 = new RoleEntity(4005,"intermediaire");
        UtilisateurEntity utilisateurEntity = find("mail_utilisateur = ?1 AND (roleEntity = ?2 OR roleEntity = ?3)", mail, roleEntity, roleEntity2).firstResult();
        return utilisateurEntity;
    }
    public void deleteClientByMail(String mail) {
        RoleEntity roleEntity = new RoleEntity(2004,"client");
        delete("mail_utilisateur = ?1 AND roleEntity = ?2", mail, roleEntity);
    }


    public UtilisateurEntity findSaByMail(String mail) {
        RoleEntity roleEntity = new RoleEntity(2005,"super-admin");
        UtilisateurEntity utilisateurEntity = find("mail_utilisateur = ?1 AND roleEntity = ?2", mail, roleEntity).firstResult();
        return utilisateurEntity;
    }
    public void deleteSaByMail(String mail) {
        RoleEntity roleEntity = new RoleEntity(2005,"super-admin");
        delete("mail_utilisateur = ?1 AND roleEntity = ?2", mail, roleEntity);
    }


    public UtilisateurEntity findUserByMail(String mail) {
        RoleEntity roleEntity = new RoleEntity(5,"User");
        UtilisateurEntity utilisateurEntity = find("mail_utilisateur = ?1 AND roleEntity = ?2", mail, roleEntity).firstResult();
        return utilisateurEntity;
    }
    public void deleteUserByMail(String mail) {
        RoleEntity roleEntity = new RoleEntity(5,"User");
        delete("mail_utilisateur = ?1 AND roleEntity = ?2", mail, roleEntity);
    }


    public UtilisateurEntity findByMail(String mail) {
        UtilisateurEntity utilisateurEntity = find("mail_utilisateur = ?1 ",mail).firstResult();
        return utilisateurEntity;
    }

    public int findIdUserByMail(String mail){
        int id = find("mail_utilisateur = ?1", mail)
                .stream()
                .map(UtilisateurEntity::getId)
                .findFirst()
                .orElse(null);
        return id;
    }

    public UtilisateurEntity findEmployeByMail(String mail) {
        RoleEntity roleEntity1 = new RoleEntity(2006,"employe");
        RoleEntity roleEntity2 = new RoleEntity(4005,"intermediaire");
        UtilisateurEntity utilisateurEntity = find("mail_utilisateur = ?1 AND (roleEntity = ?2 OR roleEntity = ?3)", mail, roleEntity1, roleEntity2).firstResult();
        return utilisateurEntity;
    }




}

