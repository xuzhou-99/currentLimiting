package cn.altaria.currentlimiting.enums;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 限流策略
 *
 * @author xuzhou
 * @since 2022/7/6
 */
@AllArgsConstructor
@Getter
public enum LimitingStrategy {

    /**
     * 限流策略：每秒
     */
    SECOND_STRATEGY("1", "秒", 1L, "second"),
    /**
     * 限流策略：每分钟
     */
    MINUTE_STRATEGY("2", "分钟", 60L, "minute"),
    /**
     * 限流策略：每小时
     */
    HOUR_STRATEGY("3", "小时", 60 * 60L, "hour"),
    /**
     * 限流策略：每天
     */
    DAY_STRATEGY("4", "天", 24 * 60 * 60L, "day"),
    /**
     * 限流策略：每周
     */
    WEEK_STRATEGY("5", "周", 7 * 24 * 60 * 60L, "week"),
    /**
     * 限流策略：每月
     */
    MONTH_STRATEGY("6", "月", 30 * 24 * 60 * 60L, "month"),
    /**
     * 限流策略：每年
     */
    YEAR_STRATEGY("7", "年", 365 * 24 * 60 * 60L, "year"),

    ;

    /**
     * 枚举缓存
     */
    private static Map<String, LimitingStrategy> enumCache = new HashMap<>();
    /**
     * 策略编码
     */
    private final String code;
    /**
     * 策略名称
     */
    private final String name;
    /**
     * 策略过期时间 （s）
     */
    private final Long exTime;
    /**
     * 关键词
     */
    private final String key;

    /**
     * 根据策略编码获取策略枚举
     *
     * @param key 策略类型
     * @return 策略
     */
    public static LimitingStrategy getStrategyByCode(String key) {
        if (enumCache == null || enumCache.isEmpty()) {
            enumCache = Arrays.stream(LimitingStrategy.values()).collect(Collectors.toMap(LimitingStrategy::getCode, e -> e));
        }
        return enumCache.get(key);
    }

}
