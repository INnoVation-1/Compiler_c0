package miniplc0java.error;

public enum ErrorCode {
    NoError, // Should be only used internally.
    SymbolDuplicated, InvalidType, NoMainFn, UnmatchType, InvalidCalculate,
    NoFn, NoSymbol, IntTooLong, NoReturn, InvalidAssign, ParamsError,
    StreamError, EOF, InvalidInput, InvalidIdentifier, IntegerOverflow,
    NoBegin, NoEnd, NeedIdentifier, ConstantNeedValue, NoSemicolon, InvalidVariableDeclaration, IncompleteExpression,
    NotDeclared, AssignToConstant, DuplicateDeclaration, NotInitialized, InvalidAssignment, InvalidPrint, ExpectedToken
}
