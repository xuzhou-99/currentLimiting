package cn.altaria.currentlimiting.count;

/**
 * 限流计数策略接口
 *
 * @author xuzhou
 * @since 2022/7/6
 */
public interface ICountStrategy {

    /**
     * 限流策略处理
     *
     * @param expireTime 限流过期，单位s
     * @param limit      限流次数
     * @param fullKey    key
     * @return 是否限流
     */
    boolean filter(final long expireTime, final long limit, final String fullKey);

}
