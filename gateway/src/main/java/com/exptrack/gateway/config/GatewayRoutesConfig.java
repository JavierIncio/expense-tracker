package com.exptrack.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.InetSocketAddress;
import java.time.Duration;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.filter.Bucket4jFilterFunctions.rateLimit;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@Configuration
public class GatewayRoutesConfig {

    private static final String RATE_LIMIT_HEADER = "X-RateLimit-Remaining";
    private static final String USER_HEADER = "X-User-Id";
    private static final int AUTH_RATE_LIMIT = 20;
    private static final int API_RATE_LIMIT = 100;

    @Bean
    RouterFunction<ServerResponse> gatewayRoutes(
            @Value("${IDENTITY_SERVICE_URI:http://localhost:8081}") String identityUri,
            @Value("${EXPENSE_SERVICE_URI:http://localhost:8082}") String expenseUri,
            @Value("${NOTIFICATION_SERVICE_URI:http://localhost:8083}") String notificationUri
    ) {
        return route("identity-service")
                .route(req -> req.path().startsWith("/api/auth"), http())
                .before(uri(identityUri))
                .filter(rateLimit(c -> c.setCapacity(AUTH_RATE_LIMIT)
                        .setPeriod(Duration.ofMinutes(1))
                        .setKeyResolver(this::ipKey)
                        .setHeaderName(RATE_LIMIT_HEADER)))
                .build()

                .and(route("expense-transactions")
                        .route(req -> req.path().startsWith("/api/transactions"), http())
                        .before(uri(expenseUri))
                        .filter(rateLimit(c -> c.setCapacity(API_RATE_LIMIT)
                                .setPeriod(Duration.ofMinutes(1))
                                .setKeyResolver(this::userKey)
                                .setHeaderName(RATE_LIMIT_HEADER)))
                        .build())

                .and(route("expense-categories")
                        .route(req -> req.path().startsWith("/api/categories"), http())
                        .before(uri(expenseUri))
                        .filter(rateLimit(c -> c.setCapacity(API_RATE_LIMIT)
                                .setPeriod(Duration.ofMinutes(1))
                                .setKeyResolver(this::userKey)
                                .setHeaderName(RATE_LIMIT_HEADER)))
                        .build())

                .and(route("expense-budgets")
                        .route(req -> req.path().startsWith("/api/budgets"), http())
                        .before(uri(expenseUri))
                        .filter(rateLimit(c -> c.setCapacity(API_RATE_LIMIT)
                                .setPeriod(Duration.ofMinutes(1))
                                .setKeyResolver(this::userKey)
                                .setHeaderName(RATE_LIMIT_HEADER)))
                        .build())

                .and(route("expense-summary")
                        .route(req -> req.path().startsWith("/api/summary"), http())
                        .before(uri(expenseUri))
                        .filter(rateLimit(c -> c.setCapacity(API_RATE_LIMIT)
                                .setPeriod(Duration.ofMinutes(1))
                                .setKeyResolver(this::userKey)
                                .setHeaderName(RATE_LIMIT_HEADER)))
                        .build())

                .and(route("notification-service")
                        .route(req -> req.path().startsWith("/api/notifications"), http())
                        .before(uri(notificationUri))
                        .filter(rateLimit(c -> c.setCapacity(API_RATE_LIMIT)
                                .setPeriod(Duration.ofMinutes(1))
                                .setKeyResolver(this::userKey)
                                .setHeaderName(RATE_LIMIT_HEADER)))
                        .build());
    }

    private String userKey(ServerRequest request) {
        String userId = request.headers().firstHeader(USER_HEADER);
        return (userId == null || userId.isBlank()) ? ipKey(request) : userId;
    }

    private String ipKey(ServerRequest request) {
        return request.remoteAddress()
                .map(InetSocketAddress::getHostString)
                .orElse("unknown");
    }
}