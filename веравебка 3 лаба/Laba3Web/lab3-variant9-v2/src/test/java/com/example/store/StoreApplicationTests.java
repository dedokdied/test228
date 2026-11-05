package com.example.store;

import com.jayway.jsonpath.JsonPath;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StoreApplicationTests {

    private static final EmbeddedPostgres POSTGRES = startPostgres();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");
    }

    private static EmbeddedPostgres startPostgres() {
        try {
            return EmbeddedPostgres.builder().start();
        } catch (IOException ex) {
            throw new IllegalStateException("Не удалось запустить встроенный PostgreSQL", ex);
        }
    }

    @Test
    void flywaySeedsCatalogAndJpaMappingIsValid() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(12)))
                .andExpect(jsonPath("$[0].name").isNotEmpty());
    }

    @Test
    void anonymousCanReadButCannotCreate() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Test","brand":"Test","price":10.00,"countInStock":1}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCanLoginAndBuyButCannotCreateProduct() throws Exception {
        String token = login("user", "user123");

        mockMvc.perform(post("/api/products/1/purchase")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countInStock").value(4));

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Test","brand":"Test","price":10.00,"countInStock":1}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateProduct() throws Exception {
        String token = login("admin", "admin123");

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Тестовый товар","brand":"Test","price":100.50,"countInStock":2}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.price").value(100.50));
    }

    @Test
    void filteringWorksThroughSpecifications() throws Exception {
        mockMvc.perform(get("/api/products").param("brand", "samsung"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/products").param("inStock", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void postgresStoresDataAndFlywayMigrationsAreApplied() throws Exception {
        String token = login("admin", "admin123");
        String uniqueName = "Compliance-" + System.nanoTime();

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","brand":"Check","price":123.45,"countInStock":7}
                                """.formatted(uniqueName)))
                .andExpect(status().isCreated());

        Integer stored = jdbcTemplate.queryForObject(
                "select count(*) from products where name = ? and brand = 'Check' and price = 123.45",
                Integer.class, uniqueName);
        assertThat(stored).isEqualTo(1);

        Integer migrations = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where version in ('1','2') and success = true",
                Integer.class);
        assertThat(migrations).isEqualTo(2);

        Integer constraints = jdbcTemplate.queryForObject("""
                select count(*) from information_schema.table_constraints
                 where constraint_name in ('chk_products_price','chk_products_count','chk_app_users_role')
                """, Integer.class);
        assertThat(constraints).isEqualTo(3);

        String hash = jdbcTemplate.queryForObject(
                "select password_hash from app_users where username = 'admin'", String.class);
        assertThat(hash).startsWith("$2");
    }

    private String login(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(response, "$.accessToken");
    }
}
