package repo;

import entities.ClientEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class ClientRepo implements PanacheRepositoryBase<ClientEntity, Integer> {

    public void deleteClientByUserId(int id) {
        ClientEntity client = find("utilisateurEntity.id = ?1", id).firstResult();
        if (client != null) {
            delete(client);
        }
    }

    public ClientEntity findByIdUser(Integer id) {
        ClientEntity client = find("utilisateurEntity.id = ?1", id).firstResult();
        if (client != null)
            return client;
        return null;
    }





}
