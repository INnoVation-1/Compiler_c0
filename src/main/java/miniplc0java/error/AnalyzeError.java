package miniplc0java.error;

import miniplc0java.util.Pos;

public class AnalyzeError extends CompileError {
    private static final long serialVersionUID = 1L;

    ErrorCode errCode;
    Pos pos;

    @Override
    public ErrorCode getErr() {
        return errCode;
    }

    @Override
    public Pos getPos() {
        return pos;
    }

    /**
     * @param errCode
     * @param pos
     */
    public AnalyzeError(ErrorCode errCode, Pos pos) {
        this.errCode = errCode;
        this.pos = pos;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("Analyze Error: ").append(errCode).append(", at: ").append(pos).toString();
    }
}
