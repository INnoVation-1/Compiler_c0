package c0java.error;

import c0java.util.Pos;

public class TokenizeError extends Throwable {
    private static final long serialVersionUID = 1L;

    private ErrorCode errCode;
    private Pos pos;

    public TokenizeError(ErrorCode errCode, Pos pos) {
        super();
        this.errCode = errCode;
        this.pos = pos;
    }

    public Pos getPos() {
        return pos;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("Tokenize Error: ").append(errCode).append(", at: ").append(pos).toString();
    }
}
