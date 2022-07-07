package cn.altaria.currentlimiting.count.strategy;

import org.springframework.stereotype.Component;

import cn.altaria.currentlimiting.count.ICountStrategy;


/**
 * 限流计数-本次缓存策略
 *
 * @author xuzhou
 * @since 2022/7/6
 */
@Component
public class CacheCountStrategy implements ICountStrategy {

    @Override
    public boolean filter(final long expireTime, final long limit, final String fullKey) {
        return CacheCountRegister.checkFilter(fullKey, expireTime, limit);
    }

}
