package services;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import outils.SecurityTools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserTest {
    private static String cryptedParameters;
    private static String token;


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

    @Order(1)
    @Test
    void createUserAlreadyExist() {
        given()
                .contentType(ContentType.JSON)
                .header("login", "jasonbailleul@jasonbailleul.fr")
                .header("password", "Jason59-@Pirate62")
                .when()
                .post("user/")
                .then()
                .statusCode(417);
    }

    @Order(2)
    @Test
    void createUserInvalidMail() {
        given()
                .contentType(ContentType.JSON)
                .header("login", "djadja59670@gmail.")
                .header("password", "Pirate62")
                .when()
                .post("user")
                .then()
                .statusCode(400);
    }

    @Order(3)
    @Test
    void createUserAccount() {
        given()
                .contentType(ContentType.JSON)
                .header("login", "jason-dem@laposte.net")
                .header("password", "Jason59-@Pirate62")
                .when()
                .post("user")
                .then()
                .statusCode(200);
    }
    @Order(4)
    @Test
    void confirmCreateEmploye() {
        given()
                .contentType(ContentType.JSON)
                .queryParam("code", cryptedParameters)
                .when()
                .get("user/confirm")
                .then()
                .statusCode(200);
    }
    @Order(5)
    @Test
    void authenticate(){
        token =  given()
                .contentType(ContentType.JSON)
                .header("login", "jason-dem@laposte.net")
                .header("password", "Jason59-@Pirate62")
                .when()
                .post("user/authentification")
                .then()
                .statusCode(200)
                .extract()
                .header("Authorization");
    }


    @Order(6)
    @Test
    void deleteUserById() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .pathParam("login","jason-dem@laposte.net")
                .when()
                .delete("user/{login}")
                .then()
                .statusCode(200);
    }

}