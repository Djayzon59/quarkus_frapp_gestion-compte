package services;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SuperAdminTest {

    private static String token;

    @Test
    @Order(1)
    void authenticate() {
        token =  given()
                .contentType(ContentType.JSON)
                .header("login", "djadja59670@gmail.com")
                .header("password", "Pirate62")
                .when()
                .post("super-admin/authentification")
                .then()
                .statusCode(200)
                .extract()
                .header("Authorization");
        System.out.println(token);
    }

    @Test
    @Order(2)
    void creerCompteSuperAdmin() {
        System.out.println(token);
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .header("login", "jason59@laposte.net")
                .header("password", "Pirate62")
                .when()
                .post("super-admin/")
                .then()
                .statusCode(200);
    }
    @Test
    @Order(3)
    void creerCompteLoginExist() {
        System.out.println(token);
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .header("login", "jasonbailleul59@laposte.net")
                .header("password", "Pirate62")
                .when()
                .post("super-admin/")
                .then()
                .statusCode(417);
    }
    @Test
    @Order(4)
    void creerCompteInvalidMail() {
        System.out.println(token);
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .header("login", "jason59laposte.net")
                .header("password", "Pirate62")
                .when()
                .post("super-admin/")
                .then()
                .statusCode(400);
    }

    @Test
    void confirmCreateSuperAdmin() {
    }


}