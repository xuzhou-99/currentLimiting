package cn.altaria.currentlimiting.handler;

import org.springframework.stereotype.Component;

import cn.altaria.currentlimiting.pojo.LimitingPointInfo;

/**
 * @author xuzhou
 * @since 2022/11/14
 */
@Component
public class TimesLimitingInfoHandler implements ILimitingInfoHandle {

    @Override
    public boolean isSupport(String type) {
        return "times".equals(type);
    }

    /**
     * 校验 请求中 AppId和 SecretKey 是否有效
     *
     * @param limitInfo 限流参数信息
     * @return 有效
     */
    @Override
    public boolean doBefore(LimitingPointInfo limitInfo) {
        // 判断是否有效的AppId

        // 处理 限流策略组合时长
        // 处理 限制访问次数
        // 处理 ip
        return true;
    }


    /**
     * 回调处理
     *
     * @param limitInfo 限流信息
     */
    @Override
    public void callback(LimitingPointInfo limitInfo) {
        // 计次通过后，剩余次数-1
    }

}
