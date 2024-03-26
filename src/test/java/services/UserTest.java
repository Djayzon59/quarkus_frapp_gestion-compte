package services;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import outils.SecurityTools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class UserTest {
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

    @Test
    void deleteUserById() {
        given()
                .contentType(ContentType.JSON)
                .pathParam("login","jason-dem@laposte.net")
                .when()
                .delete("user/{login}")
                .then()
                .statusCode(200);
    }
}