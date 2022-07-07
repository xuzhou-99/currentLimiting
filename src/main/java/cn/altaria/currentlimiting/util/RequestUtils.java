package cn.altaria.currentlimiting.util;

import java.net.InetAddress;

import javax.servlet.http.HttpServletRequest;

/**
 * RequestUtils
 * 请求相关工具类
 *
 * @author xuzhou
 * @version v1.0.0
 * @date 2022/2/10 11:24
 */
public class RequestUtils {

    /**
     * 本地机器Ip
     */
    private static final String LOCALHOST = "127.0.0.1";

    /**
     * 定义获取 ip 的 header
     */
    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };


    /**
     * 通过请求获取对应的Ip
     *
     * @param request 请求
     * @return Ip
     */
    public static String getIpAddress(HttpServletRequest request) {

        String ip = "";
        for (String header : IP_HEADER_CANDIDATES) {
            ip = request.getHeader(header);
            if (!isUnknownIp(ip)) {
                break;
            }
        }

        if (isUnknownIp(ip)) {
            ip = request.getRemoteAddr();
            if (LOCALHOST.equals(ip)) {
                try {
                    ip = InetAddress.getLocalHost().getHostAddress();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        if (isUnknownIp(ip)) {
            return ip;
        }

        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip.length() > 15 && ip.contains(",")) {
            // "***.***.***.***".length() = 15
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip;
    }

    /**
     * 通过请求获取对应的Ip
     *
     * @param request 请求
     * @return Ip
     */
    public static String getUserAgent(HttpServletRequest request) {

        String userAgent = "";

        if (null != request) {
            // 获取 user-agent
            userAgent = request.getHeader("user-agent");
        }
        return userAgent;
    }

    /**
     * 判断是否是未知IP
     *
     * @param ip IP地址
     * @return 是未知IP
     */
    public static boolean isUnknownIp(String ip) {
        return null == ip || ip.length() == 0 || "unknown".equalsIgnoreCase(ip);
    }
}
