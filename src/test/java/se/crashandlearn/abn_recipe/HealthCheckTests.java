package se.crashandlearn.abn_recipe;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class HealthCheckTests {

    @Autowired
    private MockMvc mvc;


    @Test
    void givenSystemOK_whenAccessingHealthCheck_thenReturnOK() throws Exception {

        mvc.perform(get("/actuator/health"))
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(status().isOk());
    }
    @Test
    void givenDatabaseExists_whenAccessingHealthCheck_thenDatabaseStatusIsReturned() throws Exception {

        mvc.perform(get("/actuator/health"))
                .andExpect(jsonPath("$.components.db.status").exists())
                .andExpect(jsonPath("$.components.db.status").value("UP"))
                .andExpect(status().isOk());
    }
}
