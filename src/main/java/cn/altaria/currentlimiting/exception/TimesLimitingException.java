package cn.altaria.currentlimiting.exception;

/**
 * 限流达到上限异常
 * 计次
 *
 * @author xuzhou
 * @since 2022/7/6
 */
public class TimesLimitingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TimesLimitingException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public TimesLimitingException(String message) {
        super(message);
    }
}
