package cn.altaria.currentlimiting.exception;

/**
 * 限流达到上限异常
 *
 * @author xuzhou
 * @since 2022/7/6
 */
public class CurrentLimitingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CurrentLimitingException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public CurrentLimitingException(String message) {
        super(message);
    }
}
