package io.quarkiverse;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class FlagsTest {

    @Test
    public void testEndpoint() {
        String body = given().when()
                .get("/test")
                .then()
                .statusCode(200)
                .extract().response().asString();
        assertTrue(body.contains("User cannot render template"));

        body = given()
                .auth().preemptive().basic("foo", "bar")
                .when()
                .get("/test")
                .then()
                .statusCode(200)
                .extract().response().asString();
        assertTrue(body.contains("user-agent:"), body);
    }

}
