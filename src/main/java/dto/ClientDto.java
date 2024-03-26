package dto;

import entities.ClientEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
@Getter
@NoArgsConstructor

public class ClientDto {

    @Schema(example = "Occaz' en Nord", maxLength = 50)
    private String denominationSociale;

    @Schema(example = "850", maxLength = 6)
    private String numRue;

    @Schema(example = "Boulevard de Paris", maxLength = 60)
    private String libelleRue;

    @Schema(example = "Appt 4", maxLength = 50)
    private String complementAdresse;

    @Schema(example = "59100", maxLength = 10)
    private String codePostal;

    @Schema(example = "524547854", maxLength = 9)
    private String siren;

    @Schema(example = "0780552023", maxLength = 10)
    private String numTel;

    @Schema(example = "Lille", maxLength = 50)
    private String libelleVille;

    @Schema(example = "France", maxLength = 40)
    private String libellePays;


    public ClientDto(ClientEntity clientEntity){
        this.denominationSociale = clientEntity.getDenominationSociale();
        this.numRue = clientEntity.getNumeroRue();
        this.libelleRue = clientEntity.getLibelleRue();
        this.codePostal = clientEntity.getCodePostal();
        this.siren = clientEntity.getSiren();
        this.numTel = clientEntity.getNumTel();
        this.libelleVille = clientEntity.getVilleEntity().getLibelleVille();
        this.libellePays = clientEntity.getVilleEntity().getPaysEntity().getLibellePays();
    }
}
