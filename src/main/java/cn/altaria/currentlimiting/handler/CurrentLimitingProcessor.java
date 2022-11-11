package cn.altaria.currentlimiting.handler;

import org.springframework.stereotype.Component;

import cn.altaria.currentlimiting.annotation.CurrentLimiting;
import cn.altaria.currentlimiting.config.CurrentLimitingConfig;
import cn.altaria.currentlimiting.count.ICountStrategy;
import cn.altaria.currentlimiting.enums.CountStrategy;
import cn.altaria.currentlimiting.enums.LimitingStrategy;
import cn.altaria.currentlimiting.enums.SceneStrategy;
import cn.altaria.currentlimiting.pojo.CuLimitInfo;
import cn.altaria.currentlimiting.pojo.CurrentLimitingRecord;
import cn.altaria.currentlimiting.spring.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 限流过滤器
 *
 * @author xuzhou
 * @since 2022/7/6
 */
@Component
@Slf4j
public class CurrentLimitingProcessor {

    private static final String LIMIT_HEADER_CURRENT = "limit:";

    private static final String LIMIT_HEADER_TIMES = "times:";

    private ICountStrategy countStrategy;

    private CurrentLimitingConfig currentLimitingConfig;

    /**
     * 策略加载限流计数器
     */
    private void getCountStrategy() {
        if (currentLimitingConfig == null) {
            log.info("【限流组件】加载限流配置 currentLimitingConfig ");
            currentLimitingConfig = SpringContextUtils.getBean(CurrentLimitingConfig.class);
        }
        if (countStrategy == null) {
            String beanName = CountStrategy.getStrategyNameByCode(currentLimitingConfig.getCountStrategy());
            log.info("【限流组件】加载 {} : {} 限流...", currentLimitingConfig.getCountStrategy(), beanName);
            countStrategy = (ICountStrategy) SpringContextUtils.getBean(beanName);
        }
        log.info("【限流组件】限流组件开始工作...");
    }

    /**
     * 过滤器处理请求
     *
     * @param currentLimiting 限流注解参数
     * @param cuLimitInfo     请求信息
     * @return 是否限流
     */
    public boolean filterRequest(final CurrentLimiting currentLimiting, final CuLimitInfo cuLimitInfo) {
        String fullKey = spliceFullKey(currentLimiting, cuLimitInfo);
        long expireTime = currentLimiting.strategy().getExTime() * currentLimiting.strategyTime();
        return filterRequest(expireTime, currentLimiting.limit(), fullKey);
    }

    /**
     * 过滤器处理请求
     *
     * @param currentLimiting 限流注解参数
     * @param requestTag      请求标识
     * @param requestIp       请求Ip
     * @return 是否限流
     */
    public boolean filterRequest(final CurrentLimiting currentLimiting, final String requestTag,
                                 final String requestIp) {
        String fullKey = spliceFullKey(currentLimiting, requestTag, requestIp);
        long expireTime = currentLimiting.strategy().getExTime() * currentLimiting.strategyTime();
        return filterRequest(expireTime, currentLimiting.limit(), fullKey);
    }

    /**
     * 过滤器处理请求
     *
     * @param currentLimitingRecord 限流操作记录
     * @param requestTag            请求标识
     * @param requestIp             请求Ip
     * @return 是否限流
     */
    public boolean filterRequest(final CurrentLimitingRecord currentLimitingRecord, final String requestTag,
                                 final String requestIp) {
        String fullKey = spliceFullKey(currentLimitingRecord, requestTag, requestIp);
        long expireTime = currentLimitingRecord.getStrategy().getExTime() * currentLimitingRecord.getStrategyTime();
        return filterRequest(expireTime, currentLimitingRecord.getLimit(), fullKey);
    }

    /**
     * 过滤器处理请求
     *
     * @param expireTime 过期时间，单位s
     * @param limit      限流上限
     * @param fullKey    限流key
     * @return 是否限流
     */
    public boolean filterRequest(final long expireTime, final long limit, String fullKey) {
        getCountStrategy();
        return countStrategy.filter(expireTime, limit, fullKey);
    }


    /**
     * 拼接限流key
     *
     * @param currentLimiting 限流注解参数
     * @param cuLimitInfo     请求信息
     * @return 限流key
     */
    public static String spliceFullKey(CurrentLimiting currentLimiting, final CuLimitInfo cuLimitInfo) {
        return spliceFullKey(currentLimiting.strategy(), currentLimiting.scene(),
                currentLimiting.strategyTime(), currentLimiting.limit(), cuLimitInfo);
    }

    /**
     * 拼接限流key
     *
     * @param currentLimitingRecord 限流注解
     * @param requestTag            请求标识：例如uri
     * @param requestIp             请求IP
     * @return 限流key
     */
    public static String spliceFullKey(final CurrentLimitingRecord currentLimitingRecord,
                                       final String requestTag, final String requestIp) {

        return spliceFullKey(currentLimitingRecord.getStrategy(), currentLimitingRecord.getScene(),
                currentLimitingRecord.getStrategyTime(), currentLimitingRecord.getLimit(), requestTag, requestIp);
    }

    /**
     * 拼接限流key
     *
     * @param currentLimiting 限流注解
     * @param requestTag      请求标识
     * @param requestIp       请求Ip
     * @return 限流key
     */
    public static String spliceFullKey(final CurrentLimiting currentLimiting, final String requestTag,
                                       final String requestIp) {

        return spliceFullKey(currentLimiting.strategy(), currentLimiting.scene(), currentLimiting.strategyTime(),
                currentLimiting.limit(), requestTag, requestIp);
    }

    /**
     * 拼接限流key
     *
     * @param strategy     限流策略
     * @param scene        限流场景
     * @param strategyTime 限流时间
     * @param limit        限流上限
     * @param requestTag   请求标识
     * @param requestIp    请求Ip
     * @return 限流key
     */
    public static String spliceFullKey(final LimitingStrategy strategy, final SceneStrategy scene,
                                       final long strategyTime, final long limit,
                                       final String requestTag, final String requestIp) {
        String fullKey;

        // 限流时间，单位秒
        long exTime = strategy.getExTime() * strategyTime;

        switch (scene) {
            case ip:
                fullKey = LIMIT_HEADER_CURRENT + scene.getKey() + ":" + requestTag + ":" + requestIp + ":" + exTime + ":" + limit;
                break;
            case app:
            case all:
            case user:
            default:
                fullKey = LIMIT_HEADER_CURRENT + scene.getKey() + ":" + requestTag + ":" + exTime + ":" + limit;
                break;
        }

        return fullKey;
    }

    /**
     * 拼接限流key
     *
     * @param strategy     限流策略
     * @param scene        限流场景
     * @param strategyTime 限流时间
     * @param limit        限流上限
     * @param cuLimitInfo  请求信息
     * @return 限流key
     */
    public static String spliceFullKey(final LimitingStrategy strategy, final SceneStrategy scene,
                                       final long strategyTime, final long limit,
                                       final CuLimitInfo cuLimitInfo) {
        String fullKey;

        // 限流时间，单位秒
        long exTime = strategy.getExTime() * strategyTime;

        switch (scene) {
            case ip:
                fullKey = LIMIT_HEADER_CURRENT + scene.getKey() + ":" + cuLimitInfo.getRequestTag() + ":" + cuLimitInfo.getIp() + ":" + exTime + ":" + limit;
                break;
            case app:
                fullKey = LIMIT_HEADER_TIMES + scene.getKey() + ":" + cuLimitInfo.getAppId() + ":" + cuLimitInfo.getRequestTag() + ":" + exTime + ":" + limit;
                break;
            case all:
            case user:
            default:
                fullKey = LIMIT_HEADER_CURRENT + scene.getKey() + ":" + cuLimitInfo.getRequestTag() + ":" + exTime + ":" + limit;
                break;
        }

        return fullKey;
    }

}
