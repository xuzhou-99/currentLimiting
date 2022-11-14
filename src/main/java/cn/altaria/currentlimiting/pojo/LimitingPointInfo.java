package cn.altaria.currentlimiting.pojo;

import cn.altaria.currentlimiting.enums.LimitingStrategy;
import cn.altaria.currentlimiting.enums.SceneStrategy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * 限流参数信息
 *
 * @author xuzhou
 * @since 2022/11/11
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LimitingPointInfo {
    /**
     * 请求标识
     */
    private String requestTag;
    /**
     * 请求Ip
     */
    private String ip;

    /**
     * 应用id
     */
    private String appId;
    /**
     * 应用密钥
     */
    private String secretKey;

    /**
     * 限流策略
     */
    private LimitingStrategy strategy;

    /**
     * 限流策略组合时长
     */
    private long strategyTime;

    /**
     * 限流场景
     */
    private SceneStrategy scene;

    /**
     * 限制访问次数
     */
    private long limit;


    public String getRequestTag() {
        return requestTag;
    }

    public void setRequestTag(String requestTag) {
        this.requestTag = requestTag;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public LimitingStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(LimitingStrategy strategy) {
        this.strategy = strategy;
    }

    public long getStrategyTime() {
        return strategyTime;
    }

    public void setStrategyTime(long strategyTime) {
        this.strategyTime = strategyTime;
    }

    public SceneStrategy getScene() {
        return scene;
    }

    public void setScene(SceneStrategy scene) {
        this.scene = scene;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }
}
