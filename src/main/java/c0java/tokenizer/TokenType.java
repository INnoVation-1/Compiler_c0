package c0java.tokenizer;

public enum TokenType {
    // 'fn'
    FN_KW,
    // 'let'
    LET_KW,
    // 'const'
    CONST_KW,
    // 'as'
    AS_KW,
    // 'while'
    WHILE_KW,
    // 'if'
    IF_KW,
    // 'else'
    ELSE_KW,
    // 'return'
    RETURN_KW,
    // 'break'
    BREAK_KW,
    // 'continue'
    CONTINUE_KW,
    //
    UINT_LITERAL,
    //
    DOUBLE_LITERAL,
    //
    STRING_LITERAL,
    //
    CHAR_LITERAL,
    //
    IDENT,
    // '+'
    PLUS,
    // '-'
    MINUS,
    // '*'
    MUL,
    // '/'
    DIV,
    // '='
    ASSIGN,
    // '=='
    EQ,
    // '!='
    NEQ,
    // '<'
    LT,
    // '>'
    GT,
    // '<='
    LE,
    // '>='
    GE,
    // '('
    L_PAREN,
    // ')'
    R_PAREN,
    // '{'
    L_BRACE,
    // '}'
    R_BRACE,
    // '->'
    ARROW,
    // ','
    COMMA,
    // ':'
    COLON,
    // ';'
    SEMICOLON,
    //
    WHITE_SPACE,
    //
    COMMENT,
    //
    EOF,
    //
    ERROR,
}
