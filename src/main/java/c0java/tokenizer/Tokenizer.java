package c0java.tokenizer;

import c0java.error.ErrorCode;
import c0java.error.TokenizeError;
import c0java.util.DoubleLiteral;
import c0java.util.Pos;

import static c0java.tokenizer.CharCheck.isEscapeChar;

public class Tokenizer {
    private StringIter it;

    public Tokenizer(StringIter it) {
        this.it = it;
    }

    public Token nextToken() throws TokenizeError {
        it.readAll();
        skipSpaceCharacters();
        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }
        char peek = it.peekChar();
        if (peek == '_' || Character.isAlphabetic(peek)) {
            return lexIdentOrKeyWord();
        } else if (Character.isDigit(peek)) {
            return lexDigitLiteral();
        } else if (peek == '"') {
            return lexStringLiteral();
        } else if (peek == '\'') {
            return lexCharLiteral();
        } else if (CharCheck.isSignChar(peek)) {
            return lexSign();
        } else {
            throw new TokenizeError(ErrorCode.InvalidInput, it.nextPos());
        }
    }

    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }

    private Token lexIdentOrKeyWord() {
        Pos prevPos = it.nextPos();
        StringBuilder tempStringBuilder = new StringBuilder();
        tempStringBuilder.append(it.nextChar());
        while(true){
            if(Character.isAlphabetic(it.peekChar())
                    || Character.isDigit(it.peekChar())
                    || it.peekChar() == '_'){
                tempStringBuilder.append(it.nextChar());
            }else{
                String tempString = tempStringBuilder.toString();
                switch (tempString){
                    case "fn":
                        return new Token(TokenType.FN_KW, tempString, prevPos, it.currentPos());
                    case "let":
                        return new Token(TokenType.LET_KW, tempString, prevPos, it.currentPos());
                    case "const":
                        return new Token(TokenType.CONST_KW, tempString, prevPos, it.currentPos());
                    case "as":
                        return new Token(TokenType.AS_KW, tempString, prevPos, it.currentPos());
                    case "while":
                        return new Token(TokenType.WHILE_KW, tempString, prevPos, it.currentPos());
                    case "if":
                        return new Token(TokenType.IF_KW, tempString, prevPos, it.currentPos());
                    case "else":
                        return new Token(TokenType.ELSE_KW, tempString, prevPos, it.currentPos());
                    case "return":
                        return new Token(TokenType.RETURN_KW, tempString, prevPos, it.currentPos());
                    case "break":
                        return new Token(TokenType.BREAK_KW, tempString, prevPos, it.currentPos());
                    case "continue":
                        return new Token(TokenType.CONTINUE_KW, tempString, prevPos, it.currentPos());
                    default:
                        return new Token(TokenType.IDENT, tempString, prevPos, it.currentPos());
                }
            }
        }
    }

    private Token lexDigitLiteral() throws TokenizeError {
        Pos prevPos = it.nextPos();
        String integerPart = lexDigit();
        if (it.peekChar() == '.') {
            it.nextChar();
            if (Character.isDigit(it.peekChar())) {
                String flostPart = lexDigit();
                if (it.peekChar() == 'e' || it.peekChar() == 'E') {
                    it.nextChar();
                    if (Character.isDigit(it.peekChar())) {
                        String ePart = lexDigit();
                        return new Token(TokenType.DOUBLE_LITERAL,
                                new DoubleLiteral(integerPart, flostPart, ePart),
                                prevPos, it.currentPos());
                    } else {
                        throw new TokenizeError(ErrorCode.InvalidInput, it.nextPos());
                    }
                } else {
                    return new Token(TokenType.DOUBLE_LITERAL,
                            new DoubleLiteral(integerPart, flostPart),
                            prevPos, it.currentPos());
                }
            } else {
                throw new TokenizeError(ErrorCode.InvalidInput, it.nextPos());
            }
        } else {
            return new Token(TokenType.UINT_LITERAL, integerPart,
                    prevPos, it.currentPos());
        }
    }

    private String lexDigit() {
        StringBuilder tempDigit = new StringBuilder();
        while (true) {
            if (Character.isDigit(it.peekChar())) {
                tempDigit.append(it.nextChar());
            } else {
                return tempDigit.toString();
            }
        }
    }

    private Token lexStringLiteral() throws TokenizeError {
        it.nextChar();
        StringBuilder tempString = new StringBuilder();
        Pos prevPos = it.previousPos();
        while (true) {
            switch (it.peekChar()) {
                case '"':
                    it.nextChar();
                    return new Token(TokenType.STRING_LITERAL, tempString.toString(), prevPos, it.currentPos());
                case '\\':
                    tempString.append(it.nextChar());
                    if (isEscapeChar(it.peekChar())) {
                        tempString.append(it.nextChar());
                    } else {
                        throw new TokenizeError(ErrorCode.InvalidInput, it.nextPos());
                    }
                    break;
                default:
                    tempString.append(it.nextChar());
            }
        }
    }

    private Token lexCharLiteral() throws TokenizeError {
        it.nextChar();
        StringBuilder tempChar = new StringBuilder();
        Pos prevPos = it.previousPos();
        switch (it.peekChar()) {
            case '\'':
                throw new TokenizeError(ErrorCode.InvalidInput, it.nextPos());
            case '\\':
                tempChar.append(it.nextChar());
                if (isEscapeChar(it.peekChar())) {
                    tempChar.append(it.nextChar());
                    if (it.nextChar() == '\'') {
                        return new Token(TokenType.CHAR_LITERAL, tempChar.toString(), prevPos, it.currentPos());
                    } else {
                        throw new TokenizeError(ErrorCode.InvalidInput, it.currentPos());
                    }
                } else {
                    throw new TokenizeError(ErrorCode.InvalidInput, it.nextPos());
                }
            default:
                tempChar.append(it.nextChar());
                if (it.nextChar() == '\'') {
                    return new Token(TokenType.CHAR_LITERAL, tempChar.toString(), prevPos, it.currentPos());
                } else {
                    throw new TokenizeError(ErrorCode.InvalidInput, it.currentPos());
                }
        }
    }

    private Token lexSign() throws TokenizeError {
        Pos prevPos = it.currentPos();
        switch (it.nextChar()) {
            case '+':
                return new Token(TokenType.PLUS, "+", prevPos, it.currentPos());
            case '-':
                switch (it.peekChar()) {
                    case '>':
                        it.nextChar();
                        return new Token(TokenType.ARROW, "->", prevPos, it.currentPos());
                    default:
                        return new Token(TokenType.MINUS, "-", prevPos, it.currentPos());
                }

            case '*':
                return new Token(TokenType.MUL, "*", prevPos, it.currentPos());
            case '/':
                switch (it.peekChar()) {
                    case '/':
                        it.nextChar();
                        StringBuilder tempComment = new StringBuilder();
                        tempComment.append("//");
                        while(true){
                            switch(it.peekChar()){
                                case '\n':
                                    it.nextChar();
                                    return nextToken();
//                                    return new Token(TokenType.COMMENT,
//                                            tempComment.toString(),
//                                            prevPos, it.currentPos());
                                default:
                                    tempComment.append(it.nextChar());
                            }
                        }
                    default:
                        return new Token(TokenType.DIV, "/", prevPos, it.currentPos());
                }


            case '=':
                switch (it.peekChar()) {
                    case '=':
                        it.nextChar();
                        return new Token(TokenType.EQ, "==", prevPos, it.currentPos());
                    default:
                        return new Token(TokenType.ASSIGN, "=", prevPos, it.currentPos());
                }
            case '!':
                switch (it.peekChar()) {
                    case '=':
                        it.nextChar();
                        return new Token(TokenType.NEQ, "!=", prevPos, it.currentPos());
                    default:
                        throw new TokenizeError(ErrorCode.InvalidInput, prevPos);
                }
            case '<':
                switch (it.peekChar()) {
                    case '=':
                        it.nextChar();
                        return new Token(TokenType.LE, "<=", prevPos, it.currentPos());
                    default:
                        return new Token(TokenType.LT, "<", prevPos, it.currentPos());
                }
            case '>':
                switch (it.peekChar()) {
                    case '=':
                        it.nextChar();
                        return new Token(TokenType.GE, ">=", prevPos, it.currentPos());
                    default:
                        return new Token(TokenType.GT, ">", prevPos, it.currentPos());
                }
            case '(':
                return new Token(TokenType.L_PAREN, '(', prevPos, it.currentPos());
            case ')':
                return new Token(TokenType.R_PAREN, ')', prevPos, it.currentPos());
            case '{':
                return new Token(TokenType.L_BRACE, '{', prevPos, it.currentPos());
            case '}':
                return new Token(TokenType.R_BRACE, '}', prevPos, it.currentPos());
            case ',':
                return new Token(TokenType.COMMA, ',', prevPos, it.currentPos());
            case ':':
                return new Token(TokenType.COLON, ':', prevPos, it.currentPos());
            case ';':
                return new Token(TokenType.SEMICOLON, ';', prevPos, it.currentPos());
            default:
                throw new TokenizeError(ErrorCode.NoError, it.currentPos());
        }
    }
}
