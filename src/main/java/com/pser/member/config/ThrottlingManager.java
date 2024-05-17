package com.pser.member.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;

public class ThrottlingManager {
    private static final int BUCKET_LIMIT = 100;
    private final Bucket bucket;

    public ThrottlingManager() {
        Bandwidth bandwidth = Bandwidth.classic(BUCKET_LIMIT, Refill.intervally(1, Duration.ofSeconds(1)));
        bucket = Bucket.builder().addLimit(bandwidth).build();
    }

    public boolean allowRequest() {
        return bucket.tryConsume(1);
    }
}
