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
import cn.altaria.currentlimiting.exception.CurrentLimitingException;
import cn.altaria.currentlimiting.handler.CurrentLimitingProcessor;
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

    @Autowired
    public CurrentLimitingAspect() {

    }

    @Pointcut(value = "@annotation(currentLimiting)")
    public void limitPoint(CurrentLimiting currentLimiting) {

    }

    @Before(value = "limitPoint(currentLimiting)", argNames = "currentLimiting")
    public void doBefore(CurrentLimiting currentLimiting) {

        String uri = "";
        String ip = "";

        // 获取 RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (null != requestAttributes) {
            // 获取 HttpServletRequest
            HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
            // 获取IP
            ip = RequestUtils.getIpAddress(request);
            assert request != null;
            uri = request.getRequestURI();
        }

        boolean isPass = currentLimitingProcessor.filterRequest(currentLimiting, uri, ip);

        if (!isPass) {
            log.info("【限流组件】IP：{}, 请求 URI：{}，在 {} 内达到限流上限 {} 次", ip, uri,
                    currentLimiting.strategyTime() + currentLimiting.strategy().getName(), currentLimiting.limit());
            throw new CurrentLimitingException("请求限流");
        }

    }

}
