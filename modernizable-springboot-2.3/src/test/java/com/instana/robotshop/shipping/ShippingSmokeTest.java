package com.instana.robotshop.shipping;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sql.DataSource;

import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.ResultActions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

// Smoke test: arranca el contexto real contra H2 (en lugar de MySQL) y ejercita
// JPA, Spring Data, la serializacion JSON y los endpoints publicos.
@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Import(ShippingSmokeTest.H2Config.class)
class ShippingSmokeTest {

    @TestConfiguration
    static class H2Config {
        @Bean
        DataSource getDataSource() {
            return DataSourceBuilder.create()
                    .url("jdbc:h2:mem:cities;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                    .username("sa").password("").build();
        }
    }

    @Autowired MockMvc mvc;
    @Autowired CityRepository cities;

    // Guarda cada respuesta en target/evidence/<nombre>.txt (estado + cuerpo) para
    // compararla antes y despues de la migracion.
    private ResultActions record(String name, String path) throws Exception {
        ResultActions r = mvc.perform(get(path));
        MockHttpServletResponse res = r.andReturn().getResponse();
        Path dir = Paths.get(System.getProperty("evidence.dir", "target/evidence"));
        Files.createDirectories(dir);
        Files.write(dir.resolve(name + ".txt"),
                (res.getStatus() + "\n" + res.getContentAsString() + "\n").getBytes(StandardCharsets.UTF_8));
        return r;
    }

    @BeforeEach
    void seed() {
        cities.deleteAll();
        City c = new City();
        c.setCode("DE");
        c.setCity("Dusseldorf");
        c.setName("Dusseldorf");
        c.setRegion("NRW");
        c.setLatitude(51.2277);
        c.setLongitude(6.7735);
        cities.save(c);
    }

    @Test
    void healthAndCount() throws Exception {
        record("health", "/health").andExpect(status().isOk()).andExpect(content().string("OK"));
        record("count", "/count").andExpect(status().isOk()).andExpect(content().string("1"));
        record("actuator-health", "/actuator/health").andExpect(status().isOk());
    }

    @Test
    void jsonEndpoints() throws Exception {
        record("cities", "/cities/DE").andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value("Dusseldorf"));
        record("match", "/match/DE/Dus").andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("DE"));
        record("codes", "/codes").andExpect(status().isOk());
        long id = cities.findByCode("DE").get(0).getUuid();
        record("calc", "/calc/" + id).andExpect(status().isOk())
                .andExpect(jsonPath("$.distance").exists());
    }
}
