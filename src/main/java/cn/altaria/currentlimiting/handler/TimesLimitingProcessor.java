package cn.altaria.currentlimiting.handler;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import cn.altaria.currentlimiting.config.CurrentLimitingConfig;
import cn.altaria.currentlimiting.count.ICountStrategy;
import cn.altaria.currentlimiting.enums.CountStrategy;
import cn.altaria.currentlimiting.exception.TimesLimitingException;
import cn.altaria.currentlimiting.pojo.LimitingPointInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * 计次过滤器
 *
 * @author xuzhou
 * @version v0.0.1
 * @since 2022/11/11
 */
@Component
@Slf4j
public class TimesLimitingProcessor extends AbstractLimitingProcess {


    private ICountStrategy countStrategy;

    @Resource
    private CurrentLimitingConfig currentLimitingConfig;

    private ILimitingInfoHandle limitingInfoHandle;


    /**
     * 策略加载限流计数器
     */
    private void getCountStrategy() {

        if (countStrategy == null) {
            String beanName = CountStrategy.getStrategyNameByCode(currentLimitingConfig.getCountStrategy());
            log.info("【计次组件】加载 {} : {} 计次...", currentLimitingConfig.getCountStrategy(), beanName);
            countStrategy = LimitingStrategyFactory.getInstance().get(beanName);
        }
        if (countStrategy == null) {
            log.warn("【计次组件】暂无适用的计次组件...");
            return;
        }
        if (limitingInfoHandle == null) {
            limitingInfoHandle = LimitingStrategyFactory.getInstance().getHandle("limit");
        }
        log.debug("【计次组件】计次组件开始工作...");
    }

    /**
     * 过滤器处理请求
     *
     * @param limitingPointInfo 限流参数信息
     */
    public void filterRequest(final LimitingPointInfo limitingPointInfo) {

        getCountStrategy();

        boolean valid = limitingInfoHandle.doBefore(limitingPointInfo);
        if (!valid) {
            log.info("【限流组件】应用Application：{}，请求 URI：{}，尚未开通服务", limitingPointInfo.getAppId(), limitingPointInfo.getRequestTag());
            throw new TimesLimitingException("您的Application应用请求尚未开通服务，如需要继续请求服务，请联系服务支持！");
        }


        String fullKey = spliceFullKey(limitingPointInfo);
        long expireTime = limitingPointInfo.getStrategy().getExTime() * limitingPointInfo.getStrategyTime();
        boolean isPass = countStrategy.filter(expireTime, limitingPointInfo.getLimit(), fullKey);

        if (!isPass) {
            log.info("【限流组件】应用Application：{}，IP：{}, 请求 URI：{}，在 {} 内达到计次上限 {} 次",
                    limitingPointInfo.getAppId(), limitingPointInfo.getIp(), limitingPointInfo.getRequestTag(),
                    limitingPointInfo.getStrategyTime() + limitingPointInfo.getStrategy().getName(), limitingPointInfo.getLimit());
            throw new TimesLimitingException("您的应用请求服务次数已经达到上限" + limitingPointInfo.getLimit() + "次，如需要继续请求服务，请联系服务支持！");
        }

        limitingInfoHandle.callback(limitingPointInfo);
    }

}
