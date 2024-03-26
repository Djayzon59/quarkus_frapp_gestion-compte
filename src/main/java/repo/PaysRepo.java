package repo;

import entities.PaysEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class PaysRepo implements PanacheRepositoryBase<PaysEntity,Integer> {
}
