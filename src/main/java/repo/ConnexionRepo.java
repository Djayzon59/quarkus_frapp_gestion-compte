package repo;

import entities.ConnexionEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.PathParam;

import java.time.LocalDateTime;
import java.util.List;

@RequestScoped
public class ConnexionRepo implements PanacheRepositoryBase<ConnexionEntity,Integer> {

    public long getConnexionFailedCount(String id_user) {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        return count("userEntity.mail_utilisateur = ?1 AND dateFail > ?2", id_user, twentyFourHoursAgo);
    }


    public ConnexionEntity getLastConnexion(String id_user) {
        List<ConnexionEntity> connexions = find("userEntity.mail_utilisateur = ?1 ORDER BY dateFail DESC", id_user)
                .range(0, 1)
                .list();
        if (!connexions.isEmpty()) {
            return connexions.get(0);
        } else {
            return null;
        }
    }

    public boolean deleteConnexionsByUser(@PathParam("login") String login) {
        try {
            delete("userEntity.mail_utilisateur = ?1", login);
            return true;
        } catch (Exception e) {
            return false;
        }

    }
}
