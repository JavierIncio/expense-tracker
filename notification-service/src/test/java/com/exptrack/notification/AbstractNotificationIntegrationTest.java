package com.exptrack.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class AbstractNotificationIntegrationTest {

    @Autowired protected MockMvc mockMvc;

    protected final UUID userId = UUID.randomUUID();

    // Auth via real headers, passing through XUserAuthFilter
    protected RequestPostProcessor asUser(UUID userId) {
        return request -> {
            request.addHeader("X-User-Id", userId.toString());
            request.addHeader("X-User-Email", "user" + userId + "@example.com");
            request.addHeader("X-User-Roles", "USER");
            return request;
        };
    }
}