package dto;

import entities.PaysEntity;
import entities.VilleEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VilleDto {
    @Schema(example = "Lille", maxLength = 50)
    private String libelleVille;
    @Schema(example = "France")
    private PaysDto paysDto;

    public VilleDto(VilleEntity villeEntity){
        this.libelleVille = villeEntity.getLibelleVille();
    }
}
