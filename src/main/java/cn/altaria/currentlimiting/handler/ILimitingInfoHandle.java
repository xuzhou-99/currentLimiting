package cn.altaria.currentlimiting.handler;

import cn.altaria.currentlimiting.pojo.LimitingPointInfo;

/**
 * @author xuzhou
 * @since 2022/11/14
 */
public interface ILimitingInfoHandle {

    /**
     * 支持限流模式
     *
     * @param type 限流模式：limit限流，times计次
     * @return 支持
     */
    boolean isSupport(String type);

    /**
     * 校验 请求中 AppId和 SecretKey 是否有效
     *
     * @param limitInfo 限流参数信息
     * @return 有效
     */
    default boolean doBefore(LimitingPointInfo limitInfo) {
        return true;
    }


    /**
     * 回调处理
     *
     * @param limitInfo 限流信息
     */
    default void callback(LimitingPointInfo limitInfo) {
        // 计次通过后，剩余次数-1
    }

}
