package org.testcontainers.bookstore.catalog.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.bookstore.common.AbstractIntegrationTest;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class ProductControllerTest extends AbstractIntegrationTest {

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        overridePropertiesInternal(registry);
    }

    @RepeatedTest(4)
    void shouldGetAllProducts() {
        mockGetPromotions();

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/products")
                .then()
                .statusCode(200)
                .body("data", hasSize(products.size()))
                .body("totalElements", is(products.size()))
                .body("pageNumber", is(1))
                .body("totalPages", is(1))
                .body("isFirst", is(true))
                .body("isLast", is(true))
                .body("hasNext", is(false))
                .body("hasPrevious", is(false));
    }


    @RepeatedTest(4)
    void shouldGetProductByCode() {
        mockGetPromotion("P100", new BigDecimal("2.5"));
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/products/{code}", "P100")
                .then()
                .statusCode(200)
                .body("code", is("P100"))
                .body("name", is("Product 1"))
                .body("description", is("Product 1 desc"))
                .body("price", is(7.5f))
        ;
    }


    @RepeatedTest(4)
    void shouldReturnNotFoundWhenProductCodeNotExists() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/products/{code}", "invalid_product_code")
                .then()
                .statusCode(404);
    }
}