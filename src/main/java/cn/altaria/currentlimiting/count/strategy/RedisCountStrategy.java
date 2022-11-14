package cn.altaria.currentlimiting.count.strategy;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import cn.altaria.currentlimiting.count.ICountStrategy;


/**
 * 限流计数-redis策略
 *
 * @author xuzhou
 * @since 2022/7/6
 */
@Component
public class RedisCountStrategy implements ICountStrategy {

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public boolean filter(final long expireTime, final long limit, final String fullKey) {

        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();

        // TODO：封装处理成原子操作
        Long increment = opsForValue.increment(fullKey, 1L);
        if (increment == null) {
            return true;
        }
        if (increment == 1) {
            opsForValue.set(fullKey, String.valueOf(increment), expireTime, TimeUnit.SECONDS);
        }

        return increment <= limit;
    }


}
