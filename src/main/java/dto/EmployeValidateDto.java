package dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@NoArgsConstructor
public class EmployeValidateDto {

    @Schema(example = "DUBOIS", maxLength = 50)
    private String nom;

    @Schema(example = "Jonathan", maxLength = 50)
    private String prenom;

    @Schema(example = "MyPassword59")
    private String password;

    @Schema(example = "MyPassword59")
    private String confirmPassword;




}
