package dto;

import entities.RoleEntity;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.UriBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import outils.HateOAS;
import outils.Link;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RoleDto {
    @Schema(example = "admin", maxLength = 50)
    private String libelleRole;
    @Schema(readOnly = true)
    private HateOAS hateOAS;


    public RoleDto (String libelleRole){
        this.libelleRole = libelleRole;
    }


    public RoleDto(RoleEntity roleEntity, UriBuilder uriBuilder){
       this.libelleRole = roleEntity.getLibelleRole();
        hateOAS = new HateOAS();
        UriBuilder deleteUriBuilder = UriBuilder.fromUri(uriBuilder.build());
        URI deleteUri = deleteUriBuilder.path("role/{id}").build(roleEntity.getId());
        hateOAS.addLink(new Link("delete", HttpMethod.DELETE, deleteUri));
    }

    public static List<RoleDto> toDtoList(List <RoleEntity> roleEntities, UriBuilder uriBuilder ){
        List <RoleDto> roleDtoList = new ArrayList<>();
        for(RoleEntity roleEntity : roleEntities){
            UriBuilder uriBuilder1 = UriBuilder.fromUri(uriBuilder.build());
            roleDtoList.add(new RoleDto(roleEntity, uriBuilder1));
        }
        return roleDtoList;
    }

    public String getLibelleRole() {
        return libelleRole;
    }
}
