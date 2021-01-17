package c0java.error;

import c0java.tokenizer.Token;
import c0java.tokenizer.TokenType;
import c0java.util.Pos;

import java.util.ArrayList;
import java.util.List;

public class TokenError extends CompileError {
    private static final long serialVersionUID = 1L;

    List<TokenType> expecTokenType;
    Token token;

    @Override
    public ErrorCode getErr() {
        return c0java.error.ErrorCode.ExpectedToken;
    }

    @Override
    public Pos getPos() {
        return token.getStartPos();
    }

    public TokenError(TokenType expectedTokenType, Token token) {
        this.expecTokenType = new ArrayList<>();
        this.expecTokenType.add(expectedTokenType);
        this.token = token;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("Analyse error. Expected ").append(expecTokenType).append(" at ")
                .append(token.getStartPos()).append("got: ").append(token.toStringAlt()).toString();
    }
}
