package cn.altaria.currentlimiting.aspect;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import cn.altaria.currentlimiting.annotation.CurrentLimiting;
import cn.altaria.currentlimiting.config.CurrentLimitingConfig;
import cn.altaria.currentlimiting.enums.SceneStrategy;
import cn.altaria.currentlimiting.handler.CurrentLimitingProcessor;
import cn.altaria.currentlimiting.handler.TimesLimitingProcessor;
import cn.altaria.currentlimiting.pojo.LimitingPointInfo;
import cn.altaria.currentlimiting.util.RequestUtils;


/**
 * 限流AOP
 *
 * @author xuzhou
 * @since 2022/7/6
 */
@Aspect
@Component
public class CurrentLimitingAspect {

    private final Logger log = LoggerFactory.getLogger(CurrentLimitingAspect.class);

    @Resource
    private CurrentLimitingProcessor currentLimitingProcessor;

    @Resource
    private TimesLimitingProcessor timesLimitingProcessor;

    @Resource
    private CurrentLimitingConfig currentLimitingConfig;


    @Autowired
    public CurrentLimitingAspect() {
        // 限流切面构建方法
    }

    @Pointcut(value = "@annotation(currentLimiting)")
    public void limitPoint(CurrentLimiting currentLimiting) {
        // 声明切入点：@CurrentLimiting
    }

    @Before(value = "limitPoint(currentLimiting)", argNames = "currentLimiting")
    public void doBefore(CurrentLimiting currentLimiting) {

        if (Boolean.FALSE.equals(currentLimitingConfig.getEnable())) {
            return;
        }

        // 获取 RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            log.error("【限流组件】限流失败，获取请求 RequestAttributes 为空！");
            return;
        }

        // 获取 HttpServletRequest
        HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
        if (request == null) {
            log.error("【限流组件】限流失败，获取请求 HttpServletRequest 为空！");
            return;
        }

        LimitingPointInfo limitingPointInfo = new LimitingPointInfo();
        limitingPointInfo.setStrategy(currentLimiting.strategy());
        limitingPointInfo.setStrategyTime(currentLimiting.strategyTime());
        limitingPointInfo.setLimit(currentLimiting.limit());
        limitingPointInfo.setScene(currentLimiting.scene());

        // 获取限流需要的信息
        String ip = RequestUtils.getIpAddress(request);
        limitingPointInfo.setIp(ip);
        String uri = request.getRequestURI();
        limitingPointInfo.setRequestTag(uri);

        if (currentLimiting.scene().getKey().equals(SceneStrategy.APP.getKey())) {
            String appId = RequestUtils.getValueFromRequest(request, "appId");
            String secretKey = RequestUtils.getValueFromRequest(request, "appId");
            limitingPointInfo.setAppId(appId);
            limitingPointInfo.setSecretKey(secretKey);
            timesLimitingProcessor.filterRequest( limitingPointInfo);
        } else {
            currentLimitingProcessor.filterRequest(limitingPointInfo);
        }

    }

}
