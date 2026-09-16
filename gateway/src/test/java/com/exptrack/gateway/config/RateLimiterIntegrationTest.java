package com.exptrack.gateway.config;

import com.exptrack.gateway.security.RestAuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.time.Duration;

import static org.springframework.cloud.gateway.server.mvc.filter.Bucket4jFilterFunctions.rateLimit;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.jwt.secret=s60xu8GG5YViQOjuzhIRv9WLKmqmd10tgYjyP9YuhPahleS2Gzpxpn6gmax+3w9k")
@AutoConfigureMockMvc
@Import(RateLimiterIntegrationTest.TestSecurityAndRouteConfiguration.class)
class RateLimiterIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void secondRequestIsRejectedWhenCapacityIsOne() throws Exception {
		mockMvc.perform(get("/api/rate-limit-test"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/rate-limit-test"))
				.andExpect(status().is(HttpStatus.TOO_MANY_REQUESTS.value()))
				.andExpect(header().string("X-RateLimit-Remaining", "0"));
	}

	@TestConfiguration
	static class TestSecurityAndRouteConfiguration {

		@Bean
		RouterFunction<ServerResponse> rateLimitTestRoute() {
			return route("rate-limit-test")
					.GET("/api/rate-limit-test", request -> mutableHeadersResponse())
					.filter(rateLimit(c -> c.setCapacity(1)
							.setPeriod(Duration.ofMinutes(1))
							.setKeyResolver(request -> "test-user")))
					.build();
		}

		@Bean
		@Order(0)
		SecurityFilterChain testFilterChain(HttpSecurity http, RestAuthenticationEntryPoint entryPoint) throws Exception {
			http.securityMatcher("/api/rate-limit-test")
					.csrf(AbstractHttpConfigurer::disable)
					.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
					.exceptionHandling(eh -> eh.authenticationEntryPoint(entryPoint))
					.authorizeHttpRequests(auth -> auth
							.requestMatchers("/api/rate-limit-test").permitAll()
							.anyRequest().authenticated());
			return http.build();
		}

		private static ServerResponse mutableHeadersResponse() {
			return new ServerResponse() {

				private final HttpHeaders headers = new HttpHeaders();

				@Override
				public HttpStatus statusCode() {
					return HttpStatus.OK;
				}

				@Override
				public HttpHeaders headers() {
					return headers;
				}

				@Override
				public MultiValueMap<String, jakarta.servlet.http.Cookie> cookies() {
					return new LinkedMultiValueMap<>();
				}

				@Override
				public ModelAndView writeTo(HttpServletRequest request, HttpServletResponse response,
						ServerResponse.Context context) {
					response.setStatus(statusCode().value());
					headers().forEach((name, values) -> values.forEach(value -> response.addHeader(name, value)));
					return null;
				}
			};
		}
	}
}