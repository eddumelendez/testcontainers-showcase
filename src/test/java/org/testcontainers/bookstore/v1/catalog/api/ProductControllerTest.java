package org.testcontainers.bookstore.v1.catalog.api;

import org.junit.jupiter.api.RepeatedTest;
import org.testcontainers.bookstore.catalog.domain.Product;
import org.testcontainers.bookstore.catalog.domain.ProductRepository;
import org.testcontainers.bookstore.v1.common.AbstractIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@TestPropertySource(properties = {
    "app.promotion-service-type=remote"
})
class ProductControllerTest extends AbstractIntegrationTest {

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        overridePropertiesInternal(registry);
    }

    @LocalServerPort
    private Integer port;

    @Autowired
    private ProductRepository productRepository;

    private final List<Product> products = List.of(
            new Product(null, "P100", "Product 1", "Product 1 desc", null, BigDecimal.TEN),
            new Product(null, "P101", "Product 2", "Product 2 desc", null, BigDecimal.valueOf(24))
    );

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        productRepository.deleteAll();
        productRepository.saveAll(products);
    }

    //@Test
    @RepeatedTest(10)
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

    //@Test
    @RepeatedTest(10)
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

    //@Test
    @RepeatedTest(10)
    void shouldReturnNotFoundWhenProductCodeNotExists() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/products/{code}", "invalid_product_code")
                .then()
                .statusCode(404);
    }
}