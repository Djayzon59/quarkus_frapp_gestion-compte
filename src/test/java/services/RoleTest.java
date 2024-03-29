package services;

import dto.RoleDto;
import entities.RoleEntity;
import entities.UtilisateurEntity;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import repo.UtilisateurRepo;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class RoleTest {

    private static String token;
    @Inject
    UtilisateurRepo utilisateurRepo;


    @Order(1)
    @Test
    void authenticate() {
        token = given()
                .contentType(ContentType.JSON)
                .header("login", "djadja59670@gmail.com")
                .header("password", "Pirate62")
                .when()
                .post("super-admin/authentification")
                .then()
                .statusCode(200)
                .extract()
                .header("Authorization");
    }

    @Order(2)
    @Test
    void getAll() {
        utilisateurRepo.findSaByMail("djadja59670@gmail.com");
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .header("login", "jason59@laposte.net")
                .header("password", "Pirate62")
                .when()
                .get("role")
                .then()
                .statusCode(200);
    }

    @Order(3)
    @Test
    void insert() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(new RoleDto("Stagiaire"))
                .when()
                .post("role")
                .then()
                .statusCode(200);
    }

    @Order(4)
    @Test
    void delete() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .pathParam("libelle", "Stagiaire" )
                .when()
                .delete("role/{libelle}")
                .then()
                .statusCode(200);
    }
}