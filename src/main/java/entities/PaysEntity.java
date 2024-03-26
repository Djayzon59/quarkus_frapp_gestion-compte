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
@Table(name = "Pays", schema = "dbo", catalog = "filrougeDTB")
public class PaysEntity {

    @Id
    @Column(name = "id_pays", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idPays;

    @Column(name = "libellePays")
    private String libellePays;

    @OneToMany(mappedBy = "paysEntity")
    private List<VilleEntity> villeEntities;

    public PaysEntity(String libellePays){
        this.libellePays = libellePays;
    }
}
