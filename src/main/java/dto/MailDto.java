package dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MailDto {

    @Schema(required = true, example = "djadja59670@gmail.com", maxLength = 255)
    private String sendTo;

    @Schema(example = "Objet : Candidature Stage CDA", maxLength = 255)
    private String subject;

    @Schema(readOnly = true)
    private LocalDateTime sendAt;

    @Schema(example = "Contenu du mail", maxLength = 255)
    private String texte;
}
