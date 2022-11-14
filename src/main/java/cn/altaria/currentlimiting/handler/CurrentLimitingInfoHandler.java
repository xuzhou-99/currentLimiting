package cn.altaria.currentlimiting.handler;

import org.springframework.stereotype.Component;

import cn.altaria.currentlimiting.pojo.LimitingPointInfo;

/**
 * @author xuzhou
 * @since 2022/11/14
 */
@Component
public class CurrentLimitingInfoHandler implements ILimitingInfoHandle {

    @Override
    public boolean isSupport(String type) {
        return "limit".equals(type);
    }

    /**
     * 校验 请求中 AppId和 SecretKey 是否有效
     *
     * @param limitInfo 限流参数信息
     * @return 有效
     */
    @Override
    public boolean doBefore(LimitingPointInfo limitInfo) {
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
