package entities;

import dto.ClientDto;
import dto.VilleDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Client", schema = "dbo")
public class ClientEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "denominationSociale")
    private String denominationSociale;
    @Column(name = "numeroRue")
    private String numeroRue;
    @Column(name = "libelleRue")
    private String libelleRue;
    @Column(name = "complementAdresse")
    private String complementAdresse;
    @Column(name = "codePostal")
    private String codePostal;
    @Column(name = "numTel")
    private String numTel;
    @Column(name = "siren")
    private String siren;

    @ManyToOne
    @JoinColumn(name = "id_ville")
    private VilleEntity villeEntity;

    @OneToOne
    @JoinColumn(name = "id_user")
    private UtilisateurEntity utilisateurEntity;

}
