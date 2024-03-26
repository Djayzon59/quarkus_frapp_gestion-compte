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
@Table(name = "Ville", schema = "dbo", catalog = "filrougeDTB")
public class VilleEntity {

    @Id
    @Column(name = "id_ville", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idVille;

    @Column(name = "libelleVille")
    private String libelleVille;

    @ManyToOne
    @JoinColumn(name = "id_pays")
    private PaysEntity paysEntity;

    @OneToMany(mappedBy = "villeEntity")
    private List<ClientEntity> clientEntities;

    public VilleEntity(String libelleVille, PaysEntity paysEntity){
        this.libelleVille = libelleVille;
        this.paysEntity = paysEntity;
    }
}
