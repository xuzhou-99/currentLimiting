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
import cn.altaria.currentlimiting.exception.CurrentLimitingException;
import cn.altaria.currentlimiting.exception.TimesLimitingException;
import cn.altaria.currentlimiting.handler.CurrentLimitingProcessor;
import cn.altaria.currentlimiting.handler.TimesLimitingProcessor;
import cn.altaria.currentlimiting.pojo.CuLimitInfo;
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
        // TODO document why this constructor is empty
    }

    @Pointcut(value = "@annotation(currentLimiting)")
    public void limitPoint(CurrentLimiting currentLimiting) {
        // TODO document why this method is empty
    }

    @Before(value = "limitPoint(currentLimiting)", argNames = "currentLimiting")
    public void doBefore(CurrentLimiting currentLimiting) {

//        if (currentLimitingConfig == null) {
//            currentLimitingConfig = SpringContextUtils1.getBean(CurrentLimitingConfig.class);
//        }
        if (!currentLimitingConfig.getEnable()) {
            return;
        }

        CuLimitInfo cuLimitInfo = new CuLimitInfo();

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

        // 获取限流需要的信息
        String ip = RequestUtils.getIpAddress(request);
        cuLimitInfo.setIp(ip);
        String uri = request.getRequestURI();
        cuLimitInfo.setRequestTag(uri);

        if (currentLimiting.scene().getKey().equals(SceneStrategy.app.getKey())) {
            String appId = RequestUtils.getValueFromRequest(request, "appId");
            String secretKey = RequestUtils.getValueFromRequest(request, "appId");
            cuLimitInfo.setAppId(appId);
            cuLimitInfo.setSecretKey(secretKey);

            boolean isPass = timesLimitingProcessor.filterRequest(currentLimiting, cuLimitInfo);
            if (!isPass) {
                log.info("【限流组件】应用Application：{}，请求 URI：{}，在 {} 内达到计次上限 {} 次", cuLimitInfo.getIp(), cuLimitInfo.getRequestTag(),
                        currentLimiting.strategyTime() + currentLimiting.strategy().getName(), currentLimiting.limit());
                throw new TimesLimitingException("您的应用请求服务次数已经达到上限" + currentLimiting.limit() + "次，如需要继续请求服务，请联系提供商！");
            }
        } else {
            boolean isPass = currentLimitingProcessor.filterRequest(currentLimiting, cuLimitInfo);
            if (!isPass) {
                log.info("【限流组件】IP：{}, 请求 URI：{}，在 {} 内达到限流上限 {} 次", cuLimitInfo.getIp(), cuLimitInfo.getRequestTag(),
                        currentLimiting.strategyTime() + currentLimiting.strategy().getName(), currentLimiting.limit());
                throw new CurrentLimitingException("请求限流");
            }
        }

    }

}
