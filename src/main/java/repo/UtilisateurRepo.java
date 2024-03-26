package repo;

import entities.UtilisateurEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class UtilisateurRepo implements PanacheRepositoryBase<UtilisateurEntity, String> {
}
