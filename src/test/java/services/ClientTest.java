package services;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.QueryParam;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ClientTest {

    @Test
    void createProAlreadyExist() {
        given()
                .contentType(ContentType.JSON)
                .header("login","djadja59670@gmail.com")
                .header("password","Pirate62")
                .when()
                .post("professionnel")
                .then()
                .statusCode(417);
    }
    @Test
    void createProInvalidMail() {
        given()
                .contentType(ContentType.JSON)
                .header("login","djadja59670@gmail.")
                .header("password","Pirate62")
                .when()
                .post("professionnel")
                .then()
                .statusCode(400);
    }
    @Test
    void createProAccount() {
        given()
                .contentType(ContentType.JSON)
                .header("login","jason-dem@laposte.net")
                .header("password","Pirate62")
                .when()
                .post("professionnel")
                .then()
                .statusCode(200);
    }

    @Test
    void confirmCreate() {
    }

    @Test
    void authenticateLoginFail() {
        given()
                .contentType(ContentType.JSON)
                .header("login","jason-m@laposte.net")
                .header("password","Pirate62")
                .when()
                .post("professionnel/authentification")
                .then()
                .statusCode(404);
    }
    @Test
    void authenticateMdpFail() {
        given()
                .contentType(ContentType.JSON)
                .header("login","jasonbailleul59@laposte.net")
                .header("password","Pirate")
                .when()
                .post("professionnel/authentification")
                .then()
                .statusCode(401);
    }
    @Test
    void authenticate() {
        given()
                .contentType(ContentType.JSON)
                .header("login","jasonbailleul59@laposte.net")
                .header("password","Pirate62")
                .when()
                .post("professionnel/authentification")
                .then()
                .statusCode(200);
    }

    @Test
    void validate() {
        given()
                .contentType(ContentType.JSON)
                .header("login","jasonbailleul59@laposte.net")
                .header("password","Pirate62")
                .when()
                .post("professionnel/validation")
                .then()
                .statusCode(401);
    }



}