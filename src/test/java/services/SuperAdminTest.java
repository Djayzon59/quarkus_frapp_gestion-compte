package services;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.*;
import outils.SecurityTools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SuperAdminTest {

    private static String token;
    static String cryptedParameters;

    @BeforeAll
    public static void setup() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 10);
        Date expiration = calendar.getTime();
        String plainTextParameters = String.format("%s|%s|%s",
                "jason-dem@laposte.net",
                BcryptUtil.bcryptHash("Jason59-@Pirate62", 31, "MyPersonnalSalt0".getBytes()),
                new SimpleDateFormat("dd-MM-yy-HH:mm:ss").format(expiration));
        cryptedParameters = SecurityTools.encrypt(plainTextParameters);
    }

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
    }

    @Test
    @Order(2)
    void creerCompte() {
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
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .header("login", "jason-dem@laposte.net")
                .header("password", "Pirate62")
                .when()
                .post("super-admin/")
                .then()
                .statusCode(417);
    }
    @Test
    @Order(4)
    void creerCompteInvalidMail() {
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
    void confirmCreate() {
        given()
                .contentType(ContentType.JSON)
                .queryParam("code", cryptedParameters)
                .when()
                .get("super-admin/confirm")
                .then()
                .statusCode(200);
    }

    @Test
    void delete() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .pathParam("login","jason-dem@laposte.net")
                .when()
                .delete("super-admin/{login}")
                .then()
                .statusCode(200);
    }


}