package infinite_loop.hack.exception;

import lombok.Getter;

import java.time.Instant;

@Getter
public class DailyLimitExceededException extends RuntimeException {
    private final Instant resetsAtUtc;
    private final int limit;

    public DailyLimitExceededException(Instant resetsAtUtc, int limit) {
        super("DAILY_LIMIT_EXCEEDED");
        this.resetsAtUtc = resetsAtUtc;
        this.limit = limit;
    }
}
