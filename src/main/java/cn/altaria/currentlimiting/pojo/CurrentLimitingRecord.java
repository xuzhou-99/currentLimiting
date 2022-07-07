package cn.altaria.currentlimiting.pojo;



import cn.altaria.currentlimiting.enums.LimitingStrategy;
import cn.altaria.currentlimiting.enums.SceneStrategy;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 限流操作记录
 *
 * @author xuzhou
 * @since 2022/7/7
 */
@Setter
@Getter
@Builder
public class CurrentLimitingRecord {
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
}
