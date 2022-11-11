package cn.altaria.currentlimiting.pojo;

/**
 * 限流参数信息
 *
 * @author xuzhou
 * @since 2022/11/11
 */
public class CuLimitInfo {
    /**
     * 请求标识
     */
    private String requestTag;
    /**
     * 请求Ip
     */
    private String ip;

    /**
     * 应用id
     */
    private String appId;
    /**
     * 应用密钥
     */
    private String secretKey;

    public String getRequestTag() {
        return requestTag;
    }

    public void setRequestTag(String requestTag) {
        this.requestTag = requestTag;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
