package repo;

import entities.VilleEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class VilleRepo implements PanacheRepositoryBase<VilleEntity,Integer> {
}
