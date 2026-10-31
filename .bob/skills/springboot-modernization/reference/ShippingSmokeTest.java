package com.instana.robotshop.shipping;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.sql.DataSource;

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
                    .url("jdbc:h2:mem:cities;DB_CLOSE_DELAY=-1")
                    .username("sa").password("").build();
        }
    }

    @Autowired MockMvc mvc;
    @Autowired CityRepository cities;

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
        mvc.perform(get("/health")).andExpect(status().isOk()).andExpect(content().string("OK"));
        mvc.perform(get("/count")).andExpect(status().isOk()).andExpect(content().string("1"));
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void jsonEndpoints() throws Exception {
        mvc.perform(get("/cities/DE")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value("Dusseldorf"));
        mvc.perform(get("/match/DE/Dus")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("DE"));
        mvc.perform(get("/codes")).andExpect(status().isOk());
        long id = cities.findByCode("DE").get(0).getUuid();
        mvc.perform(get("/calc/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.distance").exists());
    }
}
