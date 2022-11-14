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

    /**
     * 限流校验
     *
     * @param key        key
     * @param expireTime 过期时间
     * @param limit      限流次数
     * @return 是否限流
     */
    @Override
    public boolean filter(final long expireTime, final long limit, final String key) {

        long currentTimeMillis = System.currentTimeMillis();
        CacheCountRegister.CacheLimit cacheLimit = CacheCountRegister.getInstance().get(key);
        // 尚未限流
        if (cacheLimit == null) {
            cacheLimit = new CacheCountRegister.CacheLimit();
            cacheLimit.setLimit(1);
            cacheLimit.setEx(currentTimeMillis + expireTime * 1000);
            CacheCountRegister.getInstance().put(key, cacheLimit);
            return true;
        }

        // 上一个限流窗口过期
        if (currentTimeMillis > cacheLimit.getEx()) {
            cacheLimit.setLimit(1);
            cacheLimit.setEx(currentTimeMillis + expireTime * 1000);
            CacheCountRegister.getInstance().put(key, cacheLimit);
            return true;
        }

        // 未到限流上限
        long count = cacheLimit.getLimit() + 1;
        if (count <= limit) {
            cacheLimit.setLimit(count);
            CacheCountRegister.getInstance().put(key, cacheLimit);
            return true;
        }

        // 限流
        return false;
    }

}
