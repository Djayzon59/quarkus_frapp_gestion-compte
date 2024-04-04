package dto;

import entities.EmployeEntity;
import entities.RoleEntity;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.UriBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import outils.HateOAS;
import outils.Link;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class EmployeDto {
    private String nom;
    private String prenom;
    private String mail;
    private HateOAS hateOAS;


    public EmployeDto(EmployeEntity employeEntity, UriBuilder uriBuilder){
        this.nom = employeEntity.getNomEmploye();
        this.prenom = employeEntity.getPrenomEmploye();
        this.mail = employeEntity.getMailEmploye();
        hateOAS = new HateOAS();
        UriBuilder deleteUriBuilder = UriBuilder.fromUri(uriBuilder.build());
        URI deleteUri = deleteUriBuilder.path("employe/{id}").build(employeEntity.getIdEmploye());
        hateOAS.addLink(new Link("delete", HttpMethod.DELETE, deleteUri));
    }

    public static List<EmployeDto> toDtoList(List <EmployeEntity> employeEntities, UriBuilder uriBuilder ){
        List <EmployeDto> employeDtoList = new ArrayList<>();
        for(EmployeEntity employeEntity : employeEntities){
            UriBuilder uriBuilder1 = UriBuilder.fromUri(uriBuilder.build());
            employeDtoList.add(new EmployeDto(employeEntity, uriBuilder1));
        }
        return employeDtoList;
    }
}
