package com.exptrack.gateway.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.caffeine.CaffeineProxyManager;
import io.github.bucket4j.distributed.proxy.AsyncProxyManager;
import io.github.bucket4j.distributed.remote.RemoteBucketState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimiterConfig {
    @Bean
    public AsyncProxyManager<String> caffeineProxyManager() {
        Caffeine<String, RemoteBucketState> builder =
                (Caffeine<String, RemoteBucketState>) (Caffeine<?, ?>) Caffeine.newBuilder()
                        .maximumSize(10_000);

        return new CaffeineProxyManager<>(builder, Duration.ofMinutes(1))
                .asAsync();
    }
}
