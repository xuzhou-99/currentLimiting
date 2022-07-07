package cn.altaria.currentlimiting.enums;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 限流计数策略
 *
 * @author xuzhou
 * @since 2022/7/6
 */
@AllArgsConstructor
@Getter
public enum CountStrategy {
    /**
     * 本地缓存
     */
    cache("cache", "cacheCountStrategy"),

    /**
     * redis
     */
    redis("redis", "redisCountStrategy"),
    ;

    /**
     * 策略key
     */
    private final String key;
    /**
     * 策略bean名称
     */
    private final String strategyName;


    /**
     * 枚举缓存
     */
    private static Map<String, CountStrategy> enumCache = new HashMap<>();

    /**
     * 根据策略编码获取策略枚举
     *
     * @param key 策略类型
     * @return 策略
     */
    public static CountStrategy getStrategyByCode(String key) {
        if (enumCache == null || enumCache.isEmpty()) {
            enumCache = Arrays.stream(CountStrategy.values()).collect(Collectors.toMap(CountStrategy::getKey, e -> e));
        }
        return enumCache.get(key);
    }

    /**
     * 根据策略编码获取策略枚举
     *
     * @param key 策略类型
     * @return 策略
     */
    public static String getStrategyNameByCode(String key) {
        if (enumCache == null || enumCache.isEmpty()) {
            enumCache = Arrays.stream(CountStrategy.values()).collect(Collectors.toMap(CountStrategy::getKey, e -> e));
        }
        if (enumCache.get(key) == null) {
            return CountStrategy.cache.getStrategyName();
        }
        return enumCache.get(key).getStrategyName();
    }
}
