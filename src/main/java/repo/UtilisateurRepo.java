package repo;

import entities.RoleEntity;
import entities.UtilisateurEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class UtilisateurRepo implements PanacheRepositoryBase<UtilisateurEntity, String> {

    public UtilisateurEntity findClientByMail(String mail) {
        RoleEntity roleEntity = new RoleEntity(2004,"client");
        UtilisateurEntity utilisateurEntity = find("mail_utilisateur = ?1 AND roleEntity = ?2", mail, roleEntity).firstResult();
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

}

