package cn.altaria.currentlimiting.count.strategy;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * 本地缓存限流策略
 *
 * @author xuzhou
 * @since 2022/7/6
 */
public class CacheCountRegister {

    private static Map<String, CacheLimit> cacheLimitMap = new HashMap<>(1024);

    /**
     * 获取本地限流缓存
     *
     * @return 限流缓存
     */
    public static Map<String, CacheLimit> getCacheLimitMap() {
        return cacheLimitMap;
    }

    public static void removeOutdated() {
        // TODO:移除过期的限流 key-value
    }


    /**
     * 限流校验
     *
     * @param key        key
     * @param expireTime 过期时间
     * @param limit      限流次数
     * @return 是否限流
     */
    public static synchronized boolean checkFilter(final String key, final long expireTime, final long limit) {
        long currentTimeMillis = System.currentTimeMillis();
        CacheLimit cacheLimit = cacheLimitMap.get(key);
        // 尚未限流
        if (cacheLimit == null) {
            cacheLimit = new CacheLimit();
            cacheLimit.setLimit(1);
            cacheLimit.setEx(currentTimeMillis + expireTime * 1000);
            cacheLimitMap.put(key, cacheLimit);
            return true;
        }

        // 上一个限流窗口过期
        if (currentTimeMillis > cacheLimit.getEx()) {
            cacheLimit.setLimit(1);
            cacheLimit.setEx(currentTimeMillis + expireTime * 1000);
            cacheLimitMap.put(key, cacheLimit);
            return true;
        }

        // 未到限流上限
        long count = cacheLimit.getLimit() + 1;
        if (count <= limit) {
            cacheLimit.setLimit(count);
            cacheLimitMap.put(key, cacheLimit);
            return true;
        }

        // 限流
        return false;
    }

    @Setter
    @Getter
    public static class CacheLimit {
        private long limit;
        private long ex;
    }

}
