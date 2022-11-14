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
import cn.altaria.currentlimiting.pojo.LimitingPointInfo;
import cn.altaria.currentlimiting.util.RequestUtils;

/**
 * 限流过滤器：对 filterCache 中的限流key进行动态限流
 *
 * @author xuzhou
 * @since 2022/7/6
 */
@Component
public class CurrentLimitingFilter extends AbstractLimitingProcess implements Filter {

    private static final Logger log = LoggerFactory.getLogger(CurrentLimitingFilter.class);

    /**
     * 动态限流集合
     */
    private static final Map<String, LimitingPointInfo> filterCache = new HashMap<>();

    @Resource
    private CurrentLimitingProcessor currentLimitingProcessor;

    @Resource
    private TimesLimitingProcessor timesLimitingProcessor;

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
    public static Map<String, LimitingPointInfo> getFilterCache() {
        return filterCache;
    }

    /**
     * 获取动态限流集合
     *
     * @param key 限流key
     * @return 动态限流集合
     */
    public static LimitingPointInfo getFilterLimit(String key) {
        return filterCache.get(key);
    }

    /**
     * 获取限流
     *
     * @param scene      限流场景
     * @param requestTag 请求标识：例如uri
     * @return 限流key
     */
    public static LimitingPointInfo getFilterLimit(SceneStrategy scene, String requestTag) {
        String key = buildLimitKey(scene, requestTag);
        return getFilterLimit(key);
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
     * @return 限流是否存在
     */
    public static Boolean isExitLimit(SceneStrategy scene, String requestTag) {
        String key = buildLimitKey(scene, requestTag);
        return isExitLimit(key);
    }

    /**
     * 新增限流
     *
     * @param key            限流key
     * @param limitingRecord 限流操作记录
     */
    public static void addLimit(String key, LimitingPointInfo limitingRecord) {
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
     * @return 限流key
     */
    public static String addLimit(final LimitingStrategy limitingStrategy, final SceneStrategy scene,
                                  final long strategyTime, final long limit,
                                  final String requestTag) {

        String key = buildLimitKey(scene, requestTag);
        LimitingPointInfo limitingRecord = LimitingPointInfo.builder()
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
        log.info("【限流】动态限流：移除对 {} 限流", key);
        filterCache.remove(key);
    }

    /**
     * 移除限流
     *
     * @param scene      限流场景
     * @param requestTag 请求标识：例如uri
     */
    public static String removeFilterKey(SceneStrategy scene, String requestTag) {
        String key = buildLimitKey(scene, requestTag);
        removeFilterKey(key);
        return key;
    }

    /**
     * 构建限流key
     *
     * @param scene      限流场景
     * @param requestTag 限流资源
     * @return 限流key
     */
    private static String buildLimitKey(SceneStrategy scene, String requestTag) {
        String key;
        switch (scene) {
            case APP:
                key = LIMIT_HEADER_TIMES + requestTag;
                break;
            case IP:
            case ALL:
            case USER:
            default:
                key = LIMIT_HEADER_CURRENT + requestTag;
                break;
        }

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

        LimitingPointInfo limitingPointInfo = null;
        // 是否存在计次
        String limitKeyApp = buildLimitKey(SceneStrategy.APP, uri);
        // 是否存在限流
        String limitKeyAll = buildLimitKey(SceneStrategy.ALL, uri);
        if (filterCache.containsKey(limitKeyApp)) {
            limitingPointInfo = filterCache.get(limitKeyApp);
            limitingPointInfo.setIp(ip);
            limitingPointInfo.setRequestTag(uri);
            timesLimitingProcessor.filterRequest(limitingPointInfo);
        } else if (filterCache.containsKey(limitKeyAll)) {
            limitingPointInfo = filterCache.get(limitKeyAll);
            limitingPointInfo.setIp(ip);
            limitingPointInfo.setRequestTag(uri);
            currentLimitingProcessor.filterRequest(limitingPointInfo);
        }


        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }

}
