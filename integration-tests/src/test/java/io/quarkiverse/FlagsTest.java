package io.quarkiverse;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;

@QuarkusTest
public class FlagsTest {

    @Test
    public void testNoAuth() {
        String body = given().when()
                .get("/test")
                .then()
                .statusCode(200)
                .extract().response().asString();
        assertTrue(body.contains("User cannot render template"));
    }

    @TestSecurity(user = "foo")
    @Test
    public void testFooUser() {
        String body = given().when()
                .get("/test")
                .then()
                .statusCode(200)
                .extract().response().asString();
        assertTrue(body.contains("user-agent:"), body);
    }
}
