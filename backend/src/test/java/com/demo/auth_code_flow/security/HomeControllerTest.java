package com.demo.auth_code_flow.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HomeControllerTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new HomeController())
            .build();

    @Test
    void returnsPublicBffLandingInformation() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("auth-code-flow"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.loginUrl")
                        .value("/oauth2/authorization/oauth2-authorization-flow"));
    }
}
