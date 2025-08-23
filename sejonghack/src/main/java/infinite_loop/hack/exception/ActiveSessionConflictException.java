package infinite_loop.hack.exception;

public class ActiveSessionConflictException extends IllegalStateException {
    private final Long sessionId;

    public ActiveSessionConflictException(Long sessionId) {
        super("SESSION_ALREADY_ACTIVE");
        this.sessionId = sessionId;
    }

    public Long getSessionId() {
        return sessionId;
    }
}
