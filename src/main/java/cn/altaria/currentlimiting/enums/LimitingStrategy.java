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
    second_strategy("1", "秒", 1L, "second"),
    /**
     * 限流策略：每分钟
     */
    minute_strategy("2", "分钟", 60L, "minute"),
    /**
     * 限流策略：每小时
     */
    hour_strategy("3", "小时", 60 * 60L, "hour"),
    /**
     * 限流策略：每天
     */
    day_strategy("4", "天", 24 * 60 * 60L, "day"),
    /**
     * 限流策略：每周
     */
    week_strategy("5", "周", 7 * 24 * 60 * 60L, "week"),
    /**
     * 限流策略：每月
     */
    month_strategy("3", "月", 30 * 24 * 60 * 60L, "month"),
    /**
     * 限流策略：每年
     */
    year_strategy("3", "年", 365 * 24 * 60 * 60L, "year"),

    ;

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
     * 枚举缓存
     */
    private static Map<String, LimitingStrategy> enumCache = new HashMap<>();

    /**
     * 根据策略编码获取策略枚举
     *
     * @param key 策略类型
     * @return 策略
     */
    public LimitingStrategy getStrategyByCode(String key) {
        if (enumCache == null || enumCache.isEmpty()) {
            enumCache = Arrays.stream(LimitingStrategy.values()).collect(Collectors.toMap(LimitingStrategy::getCode, e -> e));
        }
        return enumCache.get(key);
    }

}
