package com.habittracker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@Slf4j
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    /**
     * Configuration de la connexion Redis
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        log.info("🔗 Configuration Redis: {}:{}", redisHost, redisPort);

        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisHost, redisPort);
        factory.setValidateConnection(true);

        return factory;
    }

    /**
     * ObjectMapper configuré pour Redis (SIMPLIFIÉ)
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Support des dates Java 8+
        mapper.registerModule(new JavaTimeModule());

        // Configuration simple sans type information
        mapper.findAndRegisterModules();

        return mapper;
    }

    /**
     * Template Redis principal avec sérialisation JSON simplifiée
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Sérialisation des clés
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);

        // Sérialisation des valeurs JSON SIMPLIFIÉE
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper());
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.setDefaultSerializer(jsonSerializer);
        template.afterPropertiesSet();

        log.info("✅ RedisTemplate configuré avec sérialisation JSON simplifiée");
        return template;
    }

    /**
     * Configuration du Cache Manager avec TTL personnalisés
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        log.info("⚙️ Configuration du Cache Manager Redis");

        // Configuration par défaut
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // TTL par défaut : 30 minutes
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper())))
                .disableCachingNullValues();

        // Configurations spécifiques par cache
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Cache statistiques utilisateur - 1 heure
        cacheConfigurations.put("user-stats", defaultConfig
                .entryTtl(Duration.ofHours(1))
                .prefixCacheNameWith("habit:user:stats:"));

        // Cache statistiques habitudes - 30 minutes
        cacheConfigurations.put("habit-stats", defaultConfig
                .entryTtl(Duration.ofMinutes(30))
                .prefixCacheNameWith("habit:stats:"));

        // Cache données graphiques - 15 minutes
        cacheConfigurations.put("chart-data", defaultConfig
                .entryTtl(Duration.ofMinutes(15))
                .prefixCacheNameWith("habit:charts:"));

        // Cache progressions récentes - 5 minutes
        cacheConfigurations.put("recent-progress", defaultConfig
                .entryTtl(Duration.ofMinutes(5))
                .prefixCacheNameWith("habit:progress:recent:"));

        // Cache achievements - 2 heures
        cacheConfigurations.put("achievements", defaultConfig
                .entryTtl(Duration.ofHours(2))
                .prefixCacheNameWith("habit:achievements:"));

        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();

        log.info("✅ Cache Manager configuré avec {} caches spécialisés", cacheConfigurations.size());

        return cacheManager;
    }
}