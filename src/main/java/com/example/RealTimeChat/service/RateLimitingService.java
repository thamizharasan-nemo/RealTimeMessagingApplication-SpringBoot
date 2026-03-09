package com.example.RealTimeChat.service;


import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.redisson.Bucket4jRedisson;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
@Service
public class RateLimitingService {

//    private Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private final Supplier<BucketConfiguration> bucketConfig;
    private final ProxyManager<String> proxyManager;
    private final RedissonClient redissonClient;

    public RateLimitingService(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        this.redissonClient = Redisson.create(config);
        this.proxyManager = Bucket4jRedisson.casBasedBuilder(((Redisson) redissonClient).getCommandExecutor()).build();

        Bandwidth limit = Bandwidth.builder()
                .capacity(30)
                .refillGreedy(30, Duration.ofMinutes(1))
                .initialTokens(30)
                .build();
        this.bucketConfig = () -> BucketConfiguration.builder()
                .addLimit(limit)
                .build();
    }

    public Bucket createDefaultBucket(){
        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillGreedy(5, Duration.ofMinutes(1))
                .initialTokens(5)
                .build();

        return Bucket.builder().addLimit(limit).build();
    }

    public boolean tryConsume(String key, long tokens){
//        Bucket bucket = buckets.computeIfAbsent(key, k -> createDefaultBucket());
//        log.info("Available tokens " + bucket.getAvailableTokens());

        RAtomicLong total = redissonClient.getAtomicLong("User " + key + ": Total ");
        RAtomicLong blocked = redissonClient.getAtomicLong("User " + key + ": Blocked ");
        total.incrementAndGet();

        System.out.println("Total access: "+total+"\nBlocked access: "+blocked);

        boolean allowed = getBucket(key).tryConsume(1);

        if (!allowed){
            blocked.incrementAndGet();
            log.warn("User {} exceeds limit", key);
        }
        return allowed;
    }

    public Bucket getBucket(String key){
//        return buckets.computeIfAbsent(key, k -> createDefaultBucket()); in-memory rate limiting

        return proxyManager.builder().build(key, bucketConfig);
    }

    public long getSecondsUntilRefill(String key){
        Bucket bucket = getBucket(key);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) return 0L;
        return Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds();
    }

    public void resetBucket(String key){
        this.proxyManager.removeProxy(key);
    }

    public Long getRemainingTokens(String key){
        return getBucket(key).getAvailableTokens();
    }
}
