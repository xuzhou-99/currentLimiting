package cn.altaria.currentlimiting.handler;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import cn.altaria.currentlimiting.config.CurrentLimitingConfig;
import cn.altaria.currentlimiting.count.ICountStrategy;
import cn.altaria.currentlimiting.enums.CountStrategy;
import cn.altaria.currentlimiting.exception.CurrentLimitingException;
import cn.altaria.currentlimiting.pojo.LimitingPointInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * 限流过滤器
 *
 * @author xuzhou
 * @since 2022/7/6
 */
@Component
@Slf4j
public class CurrentLimitingProcessor extends AbstractLimitingProcess {


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
            log.info("【限流组件】加载 {} : {} 限流...", currentLimitingConfig.getCountStrategy(), beanName);
            countStrategy = LimitingStrategyFactory.getInstance().get(beanName);
        }
        if (countStrategy == null) {
            log.warn("【限流组件】暂无适用的限流组件...");
            return;
        }

        if (limitingInfoHandle == null) {
            limitingInfoHandle = LimitingStrategyFactory.getInstance().getHandle("limit");
        }

        log.debug("【限流组件】限流组件开始工作...");
    }

    /**
     * 过滤器处理请求
     *
     * @param limitingPointInfo 请求信息
     */
    public void filterRequest(final LimitingPointInfo limitingPointInfo) {

        getCountStrategy();

        limitingInfoHandle.doBefore(limitingPointInfo);

        String fullKey = spliceFullKey(limitingPointInfo);
        long expireTime = limitingPointInfo.getStrategy().getExTime() * limitingPointInfo.getStrategyTime();
        boolean isPass = countStrategy.filter(expireTime, limitingPointInfo.getLimit(), fullKey);

        if (!isPass) {
            log.info("【限流组件】IP：{}, 请求 URI：{}，在 {} 内达到限流上限 {} 次",
                    limitingPointInfo.getIp(), limitingPointInfo.getRequestTag(),
                    limitingPointInfo.getStrategyTime() + limitingPointInfo.getStrategy().getName(), limitingPointInfo.getLimit());
            throw new CurrentLimitingException("请求限流");
        }

        limitingInfoHandle.callback(limitingPointInfo);
    }

}
