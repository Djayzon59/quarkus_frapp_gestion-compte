package dto;

import entities.PaysEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PaysDto {

    @Schema(example = "France", maxLength = 40)
    private String libellePays;

    public PaysDto(PaysEntity paysEntity){
        this.libellePays = paysEntity.getLibellePays();
    }
}
