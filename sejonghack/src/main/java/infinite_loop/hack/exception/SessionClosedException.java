package infinite_loop.hack.exception;

import lombok.Getter;

@Getter
public class SessionClosedException extends RuntimeException {
    private final Long sessionId;
    private final String status; // e.g., SUBMITTED

    public SessionClosedException(Long sessionId, String status) {
        super("SESSION_ALREADY_SUBMITTED");
        this.sessionId = sessionId;
        this.status = status;
    }
}
