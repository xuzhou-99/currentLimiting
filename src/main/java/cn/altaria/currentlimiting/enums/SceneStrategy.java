package cn.altaria.currentlimiting.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 限流场景
 *
 * @author xuzhou
 * @since 2022/7/6
 */
@AllArgsConstructor
@Getter
public enum SceneStrategy {

    /**
     * 全局请求
     */
    ALL("all", "全局请求"),

    /**
     * 单独用户限流
     * TODO:暂未实现
     */
    USER("user", "单独用户限流"),

    /**
     * 对IP进行限流
     */
    IP("ip", "对IP进行限流"),

    /**
     * 应用用户限流
     */
    APP("app", "应用限流"),
    ;

    /**
     * 限流场景key
     */
    private final String key;
    /**
     * 限流场景描述
     */
    private final String sceneName;
}
