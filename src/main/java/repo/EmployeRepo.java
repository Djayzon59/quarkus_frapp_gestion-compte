package repo;

import entities.EmployeEntity;
import entities.RoleEntity;
import entities.UtilisateurEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.RequestScoped;

import java.util.ArrayList;
import java.util.stream.Collectors;


@RequestScoped
public class EmployeRepo implements PanacheRepositoryBase<EmployeEntity,Integer> {


    public ArrayList<EmployeEntity> findEmployeByIdClient(int id) {
        ArrayList<EmployeEntity> employeEntities = (ArrayList<EmployeEntity>) listAll();
        return (ArrayList<EmployeEntity>) employeEntities.stream()
                .filter(employe -> employe.getClientEntity().getId() == id)
                .collect(Collectors.toList());
    }
}
