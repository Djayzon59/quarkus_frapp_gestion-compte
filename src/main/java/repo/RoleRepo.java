package repo;

import entities.RoleEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.RequestScoped;


import javax.swing.*;

@RequestScoped
public class RoleRepo implements PanacheRepositoryBase<RoleEntity, Integer> {
}
