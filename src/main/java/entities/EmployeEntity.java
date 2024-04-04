package entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Table(name = "Employe", schema = "dbo", catalog = "filrougeDTB")
public class EmployeEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id_employe", nullable = false)
    private int idEmploye;
    @Basic
    @Column(name = "nomEmploye", length = 50)
    private String nomEmploye;
    @Basic
    @Column(name = "prenomEmploye", length = 50)
    private String prenomEmploye;
    @Basic
    @Column(name = "mailEmploye", length = 50)
    private String mailEmploye;
    @Basic
    @Column(name = "dateEntreeEmploye")
    private LocalDateTime dateEntree;
    @Basic
    @Column(name = "dateSortieEmploye")
    private LocalDateTime dateSortie;

    @Getter
    @OneToOne
    @JoinColumn(name = "id_user")
    private UtilisateurEntity utilisateurEntity;

    @Getter
    @ManyToOne
    @JoinColumn(name = "id_client")
    private ClientEntity clientEntity;

}
