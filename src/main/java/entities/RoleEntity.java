package entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name ="Role")
public class RoleEntity {

    @Id
    @Column(name = "id_role", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name ="libelleRole", nullable = false)
    private String libelleRole;

    @OneToMany(mappedBy = "roleEntity")
    private List<UtilisateurEntity> userEntities;


    public RoleEntity(int id, String libelle){
        this.libelleRole = libelle;
        this.id = id;
    }
}
