package services;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ConnexionTest {

    @Test
    void chekConnexions() {
        given()
                .contentType(ContentType.JSON)
                .pathParams("login","djadja59670@gmail.com")
                .when()
                .get("connexion/{login}")
                .then()
                .statusCode(200);
    }

    @Test
    void addConnexion() {
        given()
                .contentType(ContentType.JSON)
                .pathParams("login","djadja59670@gmail.com")
                .when()
                .post("connexion/{login}")
                .then()
                .statusCode(200);
    }
    @Test
    void addConnexionnInvalidLogin() {
        given()
                .contentType(ContentType.JSON)
                .pathParams("login","djadja5670@gmail.com")
                .when()
                .post("connexion/{login}")
                .then()
                .statusCode(417);
    }

    @Test
    void deleteConnexionsByUser() {
        given()
                .contentType(ContentType.JSON)
                .pathParams("login","djadja59670@gmail.com")
                .when()
                .delete("connexion/{login}")
                .then()
                .statusCode(200);
    }
}