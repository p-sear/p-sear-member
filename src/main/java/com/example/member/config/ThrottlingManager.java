package com.example.member.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;

public class ThrottlingManager {
    private static final int MAX_TOTAL_BUCKETS = 100;
    private Bucket bucket;

    public boolean allowRequest() {
        return bucket.tryConsume(1);
    }

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(MAX_TOTAL_BUCKETS, Refill.intervally(1, Duration.ofSeconds(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
