package com.zyzylong.seeksome.admin.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GlobalErrorHandlingApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unknown_path_returns_404_with_result_body() throws Exception {
        mockMvc.perform(get("/no-such-route"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("A0404"));
    }

    @Test
    void method_not_allowed_returns_405_with_result_body() throws Exception {
        mockMvc.perform(get("/admin/auth/login"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("A0405"));
    }

    @Test
    void unsupported_media_type_returns_415_with_result_body() throws Exception {
        mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("username=admin&password=x"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("A0415"));
    }
}
