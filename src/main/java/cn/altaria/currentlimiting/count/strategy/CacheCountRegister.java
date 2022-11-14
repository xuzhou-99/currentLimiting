package cn.altaria.currentlimiting.count.strategy;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
import lombok.Setter;

/**
 * 本地缓存限流策略
 *
 * @author xuzhou
 * @since 2022/7/6
 */
public class CacheCountRegister {

    private static final Logger log = LoggerFactory.getLogger(CacheCountRegister.class);

    private static final Map<String, CacheLimit> CACHE_LIMIT_MAP = new HashMap<>(1024);

    /**
     * 默认触发移除过期限流限量
     */
    private static final Integer DEFAULT_BOUNDARY_SIZE = 500;

    private static final CacheCountRegister INSTANCE = new CacheCountRegister();

    private CacheCountRegister() {
    }

    public static CacheCountRegister getInstance() {
        return INSTANCE;
    }

    /**
     * 移除过期限流key-value
     * {@code 后续考虑添加守护线程处理过期，暂时不考虑}
     */
    public static void removeOutdated() {
        // 移除过期的限流 key-value

        long currentTimeMillis = System.currentTimeMillis();
        log.info("开始移除过期限流 key-value：");
        for (Map.Entry<String, CacheLimit> cacheLimit : CACHE_LIMIT_MAP.entrySet()) {
            if (currentTimeMillis > cacheLimit.getValue().getEx()) {
                CACHE_LIMIT_MAP.remove(cacheLimit.getKey());
                log.info("移除过期限流 key：{}", cacheLimit.getKey());
            }
        }
        log.info("完成移除过期限流 key-value。");
    }

    /**
     * 移除过期限流key-value，默认500
     *
     * @param boundarySize 触发移除限量
     */
    public static void removeOutdated(Integer boundarySize) {
        if (boundarySize == null) {
            boundarySize = DEFAULT_BOUNDARY_SIZE;
        }
        if (getCacheSize() >= boundarySize) {
            removeOutdated();
        }
    }

    /**
     * 获取限流key-value大小
     *
     * @return 限流组大小
     */
    public static int getCacheSize() {
        return CACHE_LIMIT_MAP.size();
    }


    /**
     * 获取值
     *
     * @param key {@link String}
     * @return {@link CacheLimit}
     */
    public CacheLimit get(final String key) {
        return CACHE_LIMIT_MAP.getOrDefault(key, null);
    }

    /**
     * 获取值
     *
     * @param key {@link String}
     */
    public void put(final String key, final CacheLimit cacheLimit) {
        CACHE_LIMIT_MAP.put(key, cacheLimit);
    }


    @Setter
    @Getter
    public static class CacheLimit {
        private long limit;
        private long ex;
    }

}
