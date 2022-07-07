package cn.altaria.currentlimiting.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 限流配置
 *
 * @author xuzhou
 * @since 2022/7/6
 */
@Component
@ConfigurationProperties(prefix = "security.limit")
public class CurrentLimitingConfig {

    /**
     * 是否开启限流
     * 默认关闭
     */
    private Boolean enable = false;

    /**
     * 限流策略
     * 默认本地缓存 cache
     */
    private String countStrategy = "cache";

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getCountStrategy() {
        return countStrategy;
    }

    public void setCountStrategy(String countStrategy) {
        this.countStrategy = countStrategy;
    }
}
