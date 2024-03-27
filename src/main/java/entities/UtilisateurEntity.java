package entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name ="Utilisateur")
public class UtilisateurEntity {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "mail_utilisateur", nullable = false)
    private String mail_utilisateur;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "isValidate")
    private Boolean isValidate;

    @ManyToOne
    @JoinColumn(name = "id_role")
    private RoleEntity roleEntity;

    @OneToMany(mappedBy = "userEntity")
    private List<ConnexionEntity> connexionEntities;

    @OneToMany(mappedBy = "utilisateurEntity")
    private List<ClientEntity> clientEntities;


}
