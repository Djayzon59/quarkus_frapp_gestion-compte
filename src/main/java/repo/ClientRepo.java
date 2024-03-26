package repo;

import entities.ClientEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class ClientRepo implements PanacheRepositoryBase<ClientEntity, Integer> {
}
