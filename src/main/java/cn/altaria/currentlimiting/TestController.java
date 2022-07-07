package cn.altaria.currentlimiting;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.altaria.currentlimiting.annotation.CurrentLimiting;
import cn.altaria.currentlimiting.enums.LimitingStrategy;
import cn.altaria.currentlimiting.enums.SceneStrategy;
import cn.altaria.currentlimiting.handler.LimitFilter;
import cn.altaria.currentlimiting.util.RequestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xuzhou
 * @since 2022/7/7
 */
@Slf4j
@Controller
@RequestMapping("/api")
public class TestController {

    @GetMapping("/limit")
    @ResponseBody
    @CurrentLimiting(strategy = LimitingStrategy.minute_strategy, strategyTime = 1L, scene = SceneStrategy.all, limit = 10L)
    public String limit() {
        System.out.println("测试限流...");
        return null;
    }

    @GetMapping("/limit1")
    @ResponseBody
    public String limit1() {
        log.info("测试 Filter 限流...");
        return null;
    }

    @GetMapping("/add")
    @ResponseBody
    public String add(HttpServletRequest request) {
        String ipAddress = RequestUtils.getIpAddress(request);
        log.info("添加 Filter 限流...");
        String limitKey = LimitFilter.addLimit(LimitingStrategy.minute_strategy, SceneStrategy.ip, 1L,
                2, "/api/limit1", ipAddress);
        return "添加" + limitKey + "限流！";
    }

    @GetMapping("/remove")
    @ResponseBody
    public String remove(HttpServletRequest request) {
        String ipAddress = RequestUtils.getIpAddress(request);
        log.info("移除 Filter 限流...");
        String limitKey = LimitFilter.removeFilterKey(SceneStrategy.ip, "/api/limit1", ipAddress);
        return "移除" + limitKey + "限流！";
    }
}
