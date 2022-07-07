package cn.altaria.currentlimiting.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


import cn.altaria.currentlimiting.enums.LimitingStrategy;
import cn.altaria.currentlimiting.enums.SceneStrategy;
import cn.altaria.currentlimiting.exception.CurrentLimitingException;
import cn.altaria.currentlimiting.pojo.CurrentLimitingRecord;
import cn.altaria.currentlimiting.util.RequestUtils;

/**
 * 限流过滤器：对 filterCache 中的限流key进行动态限流
 *
 * @author xuzhou
 * @since 2022/7/6
 */
@Component
public class LimitFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(LimitFilter.class);

    private static final String CURRENT_LIMIT_HEADER = "limit:";

    /**
     * 动态限流集合
     */
    private static Map<String, CurrentLimitingRecord> filterCache = new HashMap<>();

    @Resource
    private CurrentLimitingProcessor currentLimitingProcessor;

    /**
     * 初始化动态限流集合
     */
    public static void initFilterCache() {
        filterCache.clear();
        log.info("【限流】动态限流：集合初始化完毕！");
    }

    /**
     * 获取动态限流集合
     *
     * @return 动态限流集合
     */
    public static Map<String, CurrentLimitingRecord> getFilterCache() {
        return filterCache;
    }

    /**
     * 获取动态限流集合
     *
     * @param key 限流key
     * @return 动态限流集合
     */
    public static CurrentLimitingRecord getFilterLimit(String key) {
        return filterCache.get(key);
    }

    /**
     * 获取限流
     *
     * @param scene      限流场景
     * @param requestTag 请求标识：例如uri
     * @param requestIp  请求Ip
     * @return 限流key
     */
    public static String getFilterLimit(SceneStrategy scene, String requestTag, String requestIp) {
        String key = buildLimitKey(scene, requestTag, requestIp);
        getFilterLimit(key);
        return key;
    }

    /**
     * 限流是否存在
     *
     * @param key 限流key
     * @return 限流是否存在
     */
    public static Boolean isExitLimit(String key) {
        return filterCache.containsKey(key);
    }


    /**
     * 限流是否存在
     *
     * @param scene      限流场景
     * @param requestTag 请求标识：例如uri
     * @param requestIp  请求Ip
     * @return 限流是否存在
     */
    public static Boolean isExitLimit(SceneStrategy scene, String requestTag, String requestIp) {
        String key = buildLimitKey(scene, requestTag, requestIp);
        return isExitLimit(key);
    }

    /**
     * 新增限流
     *
     * @param key            限流key
     * @param limitingRecord 限流操作记录
     */
    public static void addLimit(String key, CurrentLimitingRecord limitingRecord) {
        filterCache.put(key, limitingRecord);
    }

    /**
     * 新增限流
     *
     * @param limitingStrategy 限流策略
     * @param scene            限流场景
     * @param strategyTime     限流时间
     * @param limit            限流上限
     * @param requestTag       请求标识：例如uri
     * @param requestIp        请求Ip
     * @return 限流key
     */
    public static String addLimit(final LimitingStrategy limitingStrategy, final SceneStrategy scene,
                                  final long strategyTime, final long limit,
                                  final String requestTag, final String requestIp) {

        String key = buildLimitKey(scene, requestTag, requestIp);
        CurrentLimitingRecord limitingRecord = CurrentLimitingRecord.builder()
                .strategy(limitingStrategy)
                .scene(scene)
                .strategyTime(strategyTime)
                .limit(limit)
                .build();

        addLimit(key, limitingRecord);
        log.info("【限流】动态限流：添加对 {} 限流", key);

        return key;
    }

    /**
     * 移除限流
     *
     * @param key 动态限流key
     */
    public static void removeFilterKey(String key) {
        filterCache.remove(key);
    }

    /**
     * 移除限流
     *
     * @param scene      限流场景
     * @param requestTag 请求标识：例如uri
     * @param requestIp  请求Ip
     * @return 限流key
     */
    public static String removeFilterKey(SceneStrategy scene, String requestTag, String requestIp) {
        String key = buildLimitKey(scene, requestTag, requestIp);
        removeFilterKey(key);
        log.info("【限流】动态限流：移除对 {} 限流", key);
        return key;
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws ServletException, IOException {

        if (filterCache.isEmpty()) {
            // 动态限流集合为空
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        // 获取IP
        String ip = RequestUtils.getIpAddress(request);
        String uri = request.getRequestURI();

        boolean isPass = true;
        CurrentLimitingRecord currentLimitingRecord = null;
        // 是否存在全局限流
        String limitKeyAll = buildLimitKey(SceneStrategy.all, uri, ip);
        if (filterCache.containsKey(limitKeyAll)) {
            currentLimitingRecord = filterCache.get(limitKeyAll);
            isPass = currentLimitingProcessor.filterRequest(filterCache.get(limitKeyAll), uri, ip);
        }

        // 是否存在Ip限流
        String limitKeyIp = buildLimitKey(SceneStrategy.ip, uri, ip);
        if (filterCache.containsKey(limitKeyIp)) {
            currentLimitingRecord = filterCache.get(limitKeyIp);
            isPass = currentLimitingProcessor.filterRequest(filterCache.get(limitKeyIp), uri, ip);
        }

        if (!isPass) {
            log.info("【限流组件】动态限流：IP：{}, 请求 URI：{}，在 {} 内达到限流上限 {} 次", ip, uri,
                    currentLimitingRecord.getStrategyTime() + currentLimitingRecord.getStrategy().getName(),
                    currentLimitingRecord.getLimit());
            throw new CurrentLimitingException("请求限流");
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }

    private static String buildLimitKey(SceneStrategy scene, String requestTag, String requestIp) {
        String key;
        if (SceneStrategy.all.getKey().equals(scene.getKey())) {
            key = CURRENT_LIMIT_HEADER + requestTag;
        } else if (SceneStrategy.ip.getKey().equals(scene.getKey())) {
            key = CURRENT_LIMIT_HEADER + requestTag + ":" + requestIp;
        } else {
            key = CURRENT_LIMIT_HEADER + requestTag;
        }
        return key;
    }

}
