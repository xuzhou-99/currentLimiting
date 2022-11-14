package cn.altaria.currentlimiting.handler;

import cn.altaria.currentlimiting.annotation.CurrentLimiting;
import cn.altaria.currentlimiting.enums.LimitingStrategy;
import cn.altaria.currentlimiting.enums.SceneStrategy;
import cn.altaria.currentlimiting.pojo.LimitingPointInfo;

/**
 * @author xuzhou
 * @since 2022/11/14
 */
public abstract class AbstractLimitingProcess {

    public static final String LIMIT_HEADER_CURRENT = "limit:";

    public static final String LIMIT_HEADER_TIMES = "times:";

    /**
     * 拼接限流key
     *
     * @param currentLimitingRecord 限流注解
     * @param requestTag            请求标识：例如uri
     * @param requestIp             请求IP
     * @return 限流key
     */
    public static String spliceFullKey(final LimitingPointInfo currentLimitingRecord,
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
    public static String spliceFullKey(final CurrentLimiting currentLimiting,
                                       final String requestTag, final String requestIp) {
        return spliceFullKey(currentLimiting.strategy(), currentLimiting.scene(),
                currentLimiting.strategyTime(), currentLimiting.limit(), requestTag, requestIp);
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
            case IP:
                fullKey = LIMIT_HEADER_CURRENT + scene.getKey() + ":" + requestTag + ":" + requestIp + ":" + exTime + ":" + limit;
                break;
            case APP:
            case ALL:
            case USER:
            default:
                fullKey = LIMIT_HEADER_CURRENT + scene.getKey() + ":" + requestTag + ":" + exTime + ":" + limit;
                break;
        }

        return fullKey;
    }

    /**
     * 拼接限流key
     *
     * @param limitingPointInfo 请求信息
     * @return 限流key
     */
    public static String spliceFullKey(final LimitingPointInfo limitingPointInfo) {
        String fullKey;

        // 限流时间，单位秒
        long exTime = limitingPointInfo.getStrategy().getExTime() * limitingPointInfo.getStrategyTime();

        switch (limitingPointInfo.getScene()) {
            case IP:
                fullKey = LIMIT_HEADER_CURRENT + limitingPointInfo.getScene().getKey() +
                        ":" + limitingPointInfo.getRequestTag() + ":" + limitingPointInfo.getIp() +
                        ":" + exTime + ":" + limitingPointInfo.getLimit();
                break;
            case APP:
                fullKey = LIMIT_HEADER_TIMES + limitingPointInfo.getScene().getKey() +
                        ":" + limitingPointInfo.getAppId() +
                        ":" + limitingPointInfo.getRequestTag() + ":" + limitingPointInfo.getIp() +
                        ":" + exTime + ":" + limitingPointInfo.getLimit();
                break;
            case ALL:
            case USER:
            default:
                fullKey = LIMIT_HEADER_CURRENT + limitingPointInfo.getScene().getKey() +
                        ":" + limitingPointInfo.getRequestTag() +
                        ":" + exTime + ":" + limitingPointInfo.getLimit();
                break;
        }

        return fullKey;
    }


}
