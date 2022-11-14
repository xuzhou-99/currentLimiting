package cn.altaria.currentlimiting.handler;


import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import cn.altaria.currentlimiting.config.CurrentLimitingConfig;
import cn.altaria.currentlimiting.count.ICountStrategy;


/**
 * 限流策略类集合
 *
 * @author xuzhou
 * @version v0.0.1
 * @title LimitingStrategy
 * @date 2022-11-14
 */
@Component
public class LimitingStrategyFactory implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(LimitingStrategyFactory.class);

    private static final Map<String, ICountStrategy> COUNT_STRATEGY_MAP = new HashMap<>();

    private static final Map<String, ILimitingInfoHandle> COUNT_HANDLE_MAP = new HashMap<>();

    private static final LimitingStrategyFactory INSTANCE = new LimitingStrategyFactory();


    @Resource
    private CurrentLimitingConfig currentLimitingConfig;
    private ApplicationContext context = null;

    private LimitingStrategyFactory() {
    }

    public static LimitingStrategyFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (context == null) {
            context = applicationContext;
        }

        init();
    }

    private void init() {
        if (currentLimitingConfig != null) {
            log.info("【限流组件】加载限流配置...");
            log.info("【限流组件】限流开启：{}，限流策略：{}", currentLimitingConfig.getEnable(), currentLimitingConfig.getCountStrategy());
        }
        log.info("【限流组件】加载限流策略...");
        Map<String, ICountStrategy> beansOfType = context.getBeansOfType(ICountStrategy.class, false, true);
        for (Map.Entry<String, ICountStrategy> bean : beansOfType.entrySet()) {
            COUNT_STRATEGY_MAP.put(bean.getKey(), bean.getValue());
            log.info("【限流组件】加载 {} 类: {}", ICountStrategy.class.getName(), bean.getKey());
        }

        log.info("【限流组件】加载限流处理类...");
        Map<String, ILimitingInfoHandle> beansOfType1 = context.getBeansOfType(ILimitingInfoHandle.class, false, true);
        for (Map.Entry<String, ILimitingInfoHandle> bean : beansOfType1.entrySet()) {
            COUNT_HANDLE_MAP.put(bean.getKey(), bean.getValue());
            log.info("【限流组件】加载 {} 类: {}", ILimitingInfoHandle.class.getName(), bean.getKey());
        }
    }

    /**
     * 获取值
     *
     * @param key {@link String}
     * @return {@link ICountStrategy}
     */
    public ICountStrategy get(final String key) {
        return COUNT_STRATEGY_MAP.getOrDefault(key, null);
    }

    /**
     * 获取值
     *
     * @param key {@link String}
     * @return {@link ICountStrategy}
     */
    public ILimitingInfoHandle getHandle(final String key) {
        for (Map.Entry<String, ILimitingInfoHandle> entry : COUNT_HANDLE_MAP.entrySet()) {
            if (entry.getValue().isSupport(key)) {
                return entry.getValue();
            }
        }

        return type -> true;
    }

}
