package cn.altaria.currentlimiting.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.altaria.currentlimiting.enums.LimitingStrategy;
import cn.altaria.currentlimiting.enums.SceneStrategy;


/**
 * 限流注解
 *
 * @author xuzhou
 * @since 2022/7/6
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentLimiting {

    /**
     * 限流策略
     *
     * @return {@link LimitingStrategy}
     */
    LimitingStrategy strategy();

    /**
     * 限流策略组合时长
     *
     * @return 限流策略组合时长
     */
    long strategyTime() default 1L;

    /**
     * 限流场景
     *
     * @return {@link SceneStrategy}
     */
    SceneStrategy scene() default SceneStrategy.ip;

    /**
     * 限制访问次数
     *
     * @return 限制访问次数
     */
    long limit();
}
