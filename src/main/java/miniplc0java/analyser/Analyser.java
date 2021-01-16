package miniplc0java.analyser;

import miniplc0java.error.*;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;
import miniplc0java.util.*;

import java.util.Stack;

public class Analyser {
    public Tokenizer tokenizer;
    public SymbolTable symbolTable;
    public Function function;
    public Function startFn = new Function();
    public Program program = new Program();
    public Symbol symbol;
    private boolean isStart = true;
    private boolean isRExpr = false;
    // int - 1, double - 2, void - 0
    private int returnType;

    /**
     * 当前偷看的 token
     */
    Token peekedToken = null;

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.symbolTable = new SymbolTable();
    }

    public void analyse() throws CompileError, TokenizeError {
        analyseProgram();
    }

    public void analyseProgram() throws CompileError, TokenizeError {
        System.out.println("startProgram");
        this.symbolTable.index.push(0);
        while (check(TokenType.FN_KW) ||
                check(TokenType.LET_KW) ||
                check(TokenType.CONST_KW)) {
            analyseItem();
        }

        //  _start
        this.symbolTable.pushFn("_start");

        startFn.name = symbolTable.symbolStack.size() - 1;

        if (this.symbolTable.isExist("main")) {
            Symbol symbolMain = symbolTable.getExist("main");
            startFn.addInstruction(new Instruction(InstructionType.u32Param,
                InstructionKind.stackalloc, Integer.toString(symbolMain.returnSlots)));
            startFn.addInstruction(new Instruction(InstructionType.u32Param,
                    InstructionKind.call,
                    Integer.toString(symbolTable.getExist("main").pos
                            + 1)));
            if(symbolMain.returnSlots != 0){
                startFn.addInstruction(new Instruction(InstructionType.u32Param,
                        InstructionKind.popn, Integer.toString(symbolMain.returnSlots)));
            }
        } else {
            throw new AnalyzeError(ErrorCode.NoMainFn, peek().getStartPos());
        }


//        startFn.addInstruction(new Instruction(InstructionType.u32Param,
//                InstructionKind.popn, "1"));

        startFn.name = program.globals.size();
        program.functions.add(0, startFn);
        program.globals.add(new Global("_start"));

        this.symbolTable.index.pop();
        System.out.println("endProgram");
        expect(TokenType.EOF);
    }

    public void analyseItem() throws CompileError, TokenizeError {
        System.out.println("startItem");
        if (check(TokenType.FN_KW)) {
            analyseFunction();
        } else {
            analyseDeclareStmt();
        }
        System.out.println("endItem");
    }

    public void analyseFunction() throws CompileError, TokenizeError {
        System.out.println("startFunction");
        //  指示当前不是_start函数
        isStart = false;
        function = new Function();
        //  清空局部变量数量
        symbolTable.num = 0;
        expect(TokenType.FN_KW);

        Token tempToken = expect(TokenType.IDENT);

        if (this.symbolTable.getFn(tempToken.getValueString()) != null) {
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration,
                    tempToken.getStartPos());
        }


        int tempPos = this.symbolTable.symbolStack.size() - 1;
        this.symbolTable.pushTrueFn(tempToken.getValueString());
        this.symbolTable.index.push(this.symbolTable.symbolStack.size());



        symbol = this.symbolTable.symbolStack.peek();

        expect(TokenType.L_PAREN);
        if (check(TokenType.CONST_KW) ||
                check(TokenType.IDENT)) {
            analyseFnParamList();
        }

        expect(TokenType.R_PAREN);
        expect(TokenType.ARROW);
        Token tempReturn = expect(TokenType.IDENT);
        if (tempReturn.getValueString().equals("void")) {
            function.returnSlots = 0;
            this.returnType = 0;
            symbol.returnSlots = 0;
            symbol.returnType = "void";
        } else if (tempReturn.getValueString().equals("int")) {
            function.returnSlots = 1;
            this.returnType = 1;
            symbol.returnSlots = 1;
            symbol.returnType = "int";
        } else if (tempReturn.getValueString().equals("double")) {
            function.returnSlots = 1;
            this.returnType = 2;
            symbol.returnSlots = 1;
            symbol.returnType = "double";
        } else {
            throw new AnalyzeError(ErrorCode.InvalidType,
                    tempReturn.getStartPos());
        }


        analyseBlockStmt();

        if(function.body.size() != 0 &&
                function.body.get(function.body.size() - 1).opcode !=
            InstructionKind.ret){
            if(function.returnSlots == 0){
                function.body.add(new Instruction(
                        InstructionType.NoParam,
                        InstructionKind.ret
                ));
            }else{
//                throw new AnalyzeError(ErrorCode.NoReturn,
//                        peek().getStartPos());
            }
        }
        if(function.body.size() == 0){
            if(function.returnSlots == 0){
                function.body.add(new Instruction(
                        InstructionType.NoParam,
                        InstructionKind.ret
                ));
            }else{
                throw new AnalyzeError(ErrorCode.NoReturn,
                        peek().getStartPos());
            }
        }

        this.symbolTable.clearNow();
        this.symbolTable.index.pop();

        this.function.name = this.symbolTable.symbolStack.size() - 1;
        this.program.functions.add(this.function);
        program.globals.add(new Global(tempToken.getValueString()));

        System.out.println("endFunction");
        isStart = true;
    }

    public String analyseExpr(boolean isOPG, boolean... isCall) throws CompileError, TokenizeError {
        System.out.println("startExpr");
        Token tempToken;
        String tempExpr = "other";
        if (check(TokenType.IDENT)) {
            tempToken = expect(TokenType.IDENT);
            if (check(TokenType.ASSIGN)) {
                //  Assign
                Symbol tempSymbol = symbolTable.getExist(tempToken.getValueString());
                tempSymbol.isInit = true;
                if(tempSymbol.kind != SymbolKind.VAR && tempSymbol.kind != SymbolKind.PARAM){
                    throw new AnalyzeError(ErrorCode.InvalidAssign, tempToken.getStartPos());
                }
                if (tempSymbol != null) {
                    int tempPos = symbolTable.symbolStack.indexOf(tempSymbol);
                    if (tempPos < symbolTable.globalNum) {
                        //  加载全局变量
                        function.body.add(new Instruction(
                                InstructionType.u32Param,
                                InstructionKind.globa,
                                Integer.toString(tempPos)
                        ));
                    } else if (tempPos >= symbolTable.globalNum + symbolTable.fnNum) {
                        //  加载局部变量
                        if(tempSymbol.kind == SymbolKind.PARAM){
                            function.body.add(new Instruction(
                                    InstructionType.u32Param,
                                    InstructionKind.arga,
                                    Integer.toString(tempPos -
                                            (symbolTable.globalNum + symbolTable.fnNum)
                                            + function.returnSlots)
                            ));
                        }else{
                            function.body.add(new Instruction(
                                    InstructionType.u32Param,
                                    InstructionKind.loca,
                                    Integer.toString(tempPos -
                                            (symbolTable.globalNum + symbolTable.fnNum
                                                    + function.paramSlots))
                            ));
                        }

                    } else {
                        //  非法调用函数
                        throw new AnalyzeError(ErrorCode.NoSymbol, tempToken.getStartPos());
                    }
                } else {
                    throw new AnalyzeError(ErrorCode.NoSymbol, tempToken.getStartPos());
                }
                expect(TokenType.ASSIGN);
                String rString = analyseExpr(false);

                switch (tempSymbol.type) {
                    case INT:
                        if (!rString.equals("int")) {
                            throw new AnalyzeError(ErrorCode.UnmatchType,
                                    tempToken.getStartPos());
                        }
                        break;
                    case DOUBLE:
                        if (!rString.equals("double")) {
                            throw new AnalyzeError(ErrorCode.UnmatchType,
                                    tempToken.getStartPos());
                        }
                        break;
                    default:
                        throw new AnalyzeError(ErrorCode.UnmatchType,
                                tempToken.getStartPos());
                }


                function.body.add(new Instruction(
                        InstructionType.NoParam,
                        InstructionKind.store64
                ));

                System.out.println("nowAssignExpr");
                tempExpr = "assign";
            } else if (check(TokenType.L_PAREN)) {
                //  Call
                expect(TokenType.L_PAREN);
                String returnType;
                switch (tempToken.getValueString()) {
                    case "getint":
                        this.symbolTable.pushFn("getint");
                        this.function.body.add(new Instruction(
                                InstructionType.u32Param,
                                InstructionKind.stackalloc,
                                Integer.toString(1)
                        ));
                        this.function.body.add(new Instruction(
                                InstructionType.u32Param,
                                InstructionKind.callname,
                                Integer.toString(symbolTable.fnNum + symbolTable.globalNum - 2)
                        ));
                        program.globals.add(new Global("getint"));
                        expect(TokenType.R_PAREN);
                        return "int";
                    case "getdouble":
                        this.symbolTable.pushFn("getdouble");
                        this.function.body.add(new Instruction(
                                InstructionType.u32Param,
                                InstructionKind.stackalloc,
                                Integer.toString(1)
                        ));
                        this.function.body.add(new Instruction(
                                InstructionType.u32Param,
                                InstructionKind.callname,
                                Integer.toString(symbolTable.fnNum + symbolTable.globalNum - 2)
                        ));
                        program.globals.add(new Global("getdouble"));
                        expect(TokenType.R_PAREN);
                        return "double";
                    case "getchar":
                        this.symbolTable.pushFn("getchar");
                        this.function.body.add(new Instruction(
                                InstructionType.u32Param,
                                InstructionKind.stackalloc,
                                Integer.toString(1)
                        ));
                        this.function.body.add(new Instruction(
                                InstructionType.u32Param,
                                InstructionKind.callname,
                                Integer.toString(symbolTable.fnNum + symbolTable.globalNum - 2)
                        ));
                        program.globals.add(new Global("getchar"));
                        expect(TokenType.R_PAREN);
                        return "int";
                    case "putint":
                        this.symbolTable.pushFn("putint");
                        this.function.body.add(new Instruction(
                                InstructionType.u32Param,
                                InstructionKind.stackalloc,
                                Integer.toString(0)
                        ));
                        returnType = analyseExpr(false);
                        if(!returnType.equals("int")){
                            throw new AnalyzeError(ErrorCode.UnmatchType,
                                    tempToken.getStartPos());
                        }
                        this.function.body.add(new Instruction(
                                InstructionType.u32Param,
                                InstructionKind.callname,
                                Integer.toString(symbolTable.fnNum + symbolTable.globalNum - 2)
                        ));
                        program.globals.add(new Global("putint"));
                        expect(TokenType.R_PAREN);
                        return "void";
                    case "putdouble":
                        this.symbolTable.pushFn("putdouble");
                        this.function.body.add(new Instruction(
                                InstructionType.u32Param,
                                InstructionKind.stackalloc,
                                Integer.toString(0)
                        ));
                        returnType = analyseExpr(false);
                        if(!returnType.equals("double")){
                            throw new AnalyzeError(ErrorCode.UnmatchType,
                                    tempToken.getStartPos());
                        }
                        this.function.body.add(new Instruction(
                                InstructionType.u32Param,
                                InstructionKind.callname,
                                Integer.toString(symbolTable.fnNum + symbolTable.globalNum - 2)
                        ));
                        program.globals.add(new Global("putdouble"));
                        expect(TokenType.R_PAREN);
                        return "void";
                    case "putchar":
                        this.symbolTable.pushFn("putchar");
                        this.function.body.add(new Instruction(
                                InstructionType.u32Param,
                                InstructionKind.stackalloc,
                                Integer.toString(0)
                        ));
                        returnType = analyseExpr(false);
                        if(!returnType.equals("int")){
                            throw new AnalyzeError(ErrorCode.UnmatchType,
                                    tempToken.getStartPos());
                        }
                        this.function.body.add(new Instruction(
                                InstructionType.u32Param,
                                InstructionKind.callname,
                                Integer.toString(symbolTable.fnNum + symbolTable.globalNum - 2)
                        ));
                        program.globals.add(new Global("putchar"));
                        expect(TokenType.R_PAREN);
                        return "void";
                    case "putstr":
                        this.symbolTable.pushFn("putstr");
                        this.function.body.add(new Instruction(
                                InstructionType.u32Param,
                                InstructionKind.stackalloc,
                                Integer.toString(0)
                        ));
                        returnType = analyseExpr(false);
                        if(!returnType.equals("int")){
                            throw new AnalyzeError(ErrorCode.UnmatchType,
                                    tempToken.getStartPos());
                        }
                        this.function.body.add(new Instruction(
                                InstructionType.u32Param,
                                InstructionKind.callname,
                                Integer.toString(symbolTable.fnNum + symbolTable.globalNum - 2)
                        ));
                        program.globals.add(new Global("putstr"));
                        expect(TokenType.R_PAREN);
                        return "void";
                    case "putln":
                        this.symbolTable.pushFn("putln");
                        this.function.body.add(new Instruction(
                                InstructionType.u32Param,
                                InstructionKind.stackalloc,
                                Integer.toString(0)
                        ));
                        this.function.body.add(new Instruction(
                                InstructionType.u32Param,
                                InstructionKind.callname,
                                Integer.toString(symbolTable.fnNum + symbolTable.globalNum - 2)
                        ));
                        program.globals.add(new Global("putln"));
                        expect(TokenType.R_PAREN);
                        return "void";
                    default:
                }
                Symbol fn = symbolTable.getFn(tempToken.getValueString());

                if(fn.requiredInit.size() != 0){
                    for(String varS : fn.requiredInit){
                        Symbol bugSymbol = this.symbolTable.getGlobal(varS);
                        if(this.symbolTable.getGlobal(varS).isInit == false){
                            throw new AnalyzeError(ErrorCode.NotInitialized,
                                    tempToken.getStartPos());
                        }
                    }
                }

                if (fn == null) {

                    throw new AnalyzeError(ErrorCode.NoFn,
                            tempToken.getStartPos());
                }

                this.function.body.add(new Instruction(
                        InstructionType.u32Param,
                        InstructionKind.stackalloc,
                        Integer.toString(fn.returnSlots)
                ));

                int num = 0;
                if (!check(TokenType.R_PAREN)) {
                    num = analyseCallParamList();
                }

                if(fn.pos + 1 == symbolTable.trueFnNum){
                    if(num != function.paramSlots){
                        throw new AnalyzeError(ErrorCode.ParamsError,
                                tempToken.getStartPos());
                    }
                }else{
                    if(num != program.functions.get(fn.pos).paramSlots){
                        throw new AnalyzeError(ErrorCode.ParamsError,
                                tempToken.getStartPos());
                    }
                }



                expect(TokenType.R_PAREN);

                this.function.body.add(new Instruction(
                        InstructionType.u32Param,
                        InstructionKind.call,
                        Integer.toString(fn.pos + 1)
                ));
                if (fn.returnSlots != 0 && isCall.length != 0 && isCall[0] == true) {
                    this.function.body.add(new Instruction(
                            InstructionType.u32Param,
                            InstructionKind.popn,
                            Integer.toString(fn.returnSlots)
                    ));
                }

                System.out.println("nowCallExpr");
                tempExpr = fn.returnType;
            }else {
                //  Indent
                Symbol tempSymbol = symbolTable.getExist(tempToken.getValueString());
                if (tempSymbol != null) {

                    int tempPos = symbolTable.symbolStack.indexOf(tempSymbol);
                    if (tempPos < symbolTable.globalNum + symbolTable.fnNum) {
                        //  加载全局变量
                        function.body.add(new Instruction(
                                InstructionType.u32Param,
                                InstructionKind.globa,
                                Integer.toString(tempPos)
                        ));
                        function.body.add(new Instruction(
                                InstructionType.NoParam,
                                InstructionKind.load64
                        ));
                    } else if (tempPos >= symbolTable.globalNum + symbolTable.fnNum) {
                        //  加载局部变量
                        if(tempSymbol.kind == SymbolKind.PARAM){
                            function.body.add(new Instruction(
                                    InstructionType.u32Param,
                                    InstructionKind.arga,
                                    Integer.toString(tempPos -
                                            (symbolTable.globalNum + symbolTable.fnNum)
                                            + function.returnSlots)
                            ));
                        }else{
                            function.body.add(new Instruction(
                                    InstructionType.u32Param,
                                    InstructionKind.loca,
                                    Integer.toString(tempPos -
                                            (symbolTable.globalNum + symbolTable.fnNum
                                                    + function.paramSlots))
                            ));
                        }
                        function.body.add(new Instruction(
                                InstructionType.NoParam,
                                InstructionKind.load64
                        ));
                    }
                    if(tempSymbol.kind == SymbolKind.FN) {
                        //  非法调用函数
                        throw new AnalyzeError(ErrorCode.NoSymbol, tempToken.getStartPos());
                    }
                    if (!tempSymbol.isInit) {
                        if (tempPos < symbolTable.globalNum + symbolTable.fnNum){
                            symbol.requiredInit.add(tempSymbol.name);
                        }else{
                            throw new AnalyzeError(ErrorCode.NotInitialized,
                                    tempToken.getStartPos());
                        }
                    }
                    switch (tempSymbol.type) {
                        case INT:
                            tempExpr = "int";
                            break;
                        case DOUBLE:
                            tempExpr = "double";
                            break;
                    }
                } else {
                    throw new AnalyzeError(ErrorCode.NoSymbol, tempToken.getStartPos());
                }

                //not complete
            }
        } else if (check(TokenType.UINT_LITERAL)) {
            System.out.println("nowUINT");
            tempToken = expect(TokenType.UINT_LITERAL);
            String binaryInt = Integer.toHexString(
                    Integer.parseInt(tempToken.getValueString())
            );
            System.out.println("ssssss" + binaryInt);
            StringBuilder zero = new StringBuilder();
            if (binaryInt.length() <= 16) {
                for (int i = 0; i < 16 - binaryInt.length(); i++) {
                    zero.append("0");
                }
                binaryInt = zero.toString() + binaryInt;
            } else {
                throw new AnalyzeError(ErrorCode.IntTooLong,
                        tempToken.getStartPos());
            }
            if (isStart) {
                startFn.body.add(new Instruction(
                        InstructionType.u64Param,
                        InstructionKind.push,
                        binaryInt
                ));
            } else {
                function.body.add(new Instruction(
                        InstructionType.u64Param,
                        InstructionKind.push,
                        binaryInt
                ));
            }
            tempExpr = "int";
        } else if (check(TokenType.DOUBLE_LITERAL)) {
            System.out.println("nowDOUBLE");
            tempToken = expect(TokenType.DOUBLE_LITERAL);
            if (isStart) {
                startFn.body.add(new Instruction(
                        InstructionType.u64Param,
                        InstructionKind.push,
                        ((DoubleLiteral) tempToken.getValue()).toString()
                ));
            } else {
                function.body.add(new Instruction(
                        InstructionType.u64Param,
                        InstructionKind.push,
                        ((DoubleLiteral) tempToken.getValue()).toString()
                ));
            }
            tempExpr = "double";
        } else if (check(TokenType.STRING_LITERAL)) {
            System.out.println("nowSTRING");
            tempToken = expect(TokenType.STRING_LITERAL);
            this.symbolTable.pushGlobal("", SymbolKind.CONST,
                    SymbolType.STRING);
            this.program.globals.add(new Global(tempToken.getValueString()
                    .replace("\\n",  (char)10+"")
                    .replace("\\\\",  "\\")
                    .replace("\\\'",  "\'")
                    .replace("\\\"",  "\"")));
            function.body.add(new Instruction(
                    InstructionType.u64Param,
                    InstructionKind.push,
                    Integer.toString(this.program.globals.size() - 1)
            ));
            tempExpr = "int";
        } else if (check(TokenType.CHAR_LITERAL)) {
            System.out.println("nowCHAR");
            tempToken = expect(TokenType.CHAR_LITERAL);
            tempExpr = "char";
        } else if (check(TokenType.L_PAREN)) {
            System.out.println("startGroupExpr");
            expect(TokenType.L_PAREN);
            tempExpr = analyseExpr(false);
            expect(TokenType.R_PAREN);
            System.out.println("endGroupExpr");
        } else if (check(TokenType.MINUS)) {
            System.out.println("startNegateExpr");
            expect(TokenType.MINUS);
            tempExpr = analyseExpr(false);
            this.function.body.add(new Instruction(
                    InstructionType.NoParam,
                    InstructionKind.negi
            ));
            System.out.println("endNegateExpr");
        }

        if (isSign() && !isOPG) {
            tempExpr = analyseOPG(tempExpr);
        } else {
        }
        System.out.println("endExpr");
        return tempExpr;
    }

    /*
// # 表达式
expr ->
      operator_expr
    | negate_expr
    | as_expr

binary_operator -> '+' | '-' | '*' | '/' | '==' | '!=' | '<' | '>' | '<=' | '>='
operator_expr -> expr binary_operator expr

as_expr -> expr 'as' IDENT

    |    | as | *  | /  | +  | -  | >  | <  | >= | <= | == | != |
    -------------------------------------------------------------
    | as | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  |
    -------------------------------------------------------------
    | *  | 0  | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  |
    -------------------------------------------------------------
    | /  | 0  | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  |
    -------------------------------------------------------------
    | +  | 0  | 0  | 0  | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  |
    -------------------------------------------------------------
    | -  | 0  | 0  | 0  | 1  | 1  | 1  | 1  | 1  | 1  | 1  | 1  |
    -------------------------------------------------------------
    | >  | 0  | 0  | 0  | 0  | 0  | 1  | 1  | 1  | 1  | 1  | 1  |
    -------------------------------------------------------------
    | <  | 0  | 0  | 0  | 0  | 0  | 1  | 1  | 1  | 1  | 1  | 1  |
    -------------------------------------------------------------
    | >= | 0  | 0  | 0  | 0  | 0  | 1  | 1  | 1  | 1  | 1  | 1  |
    -------------------------------------------------------------
    | <= | 0  | 0  | 0  | 0  | 0  | 1  | 1  | 1  | 1  | 1  | 1  |
    -------------------------------------------------------------
    | == | 0  | 0  | 0  | 0  | 0  | 1  | 1  | 1  | 1  | 1  | 1  |
    -------------------------------------------------------------
    | != | 0  | 0  | 0  | 0  | 0  | 1  | 1  | 1  | 1  | 1  | 1  |
    -------------------------------------------------------------
     */

    private int[][] priorityMatrix = {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1}
    };

    public int transferSign(TokenType tt) {
        switch (tt) {
            case AS_KW:
                return 0;
            case MUL:
                return 1;
            case DIV:
                return 2;
            case PLUS:
                return 3;
            case MINUS:
                return 4;
            case LT:
                return 5;
            case GT:
                return 6;
            case LE:
                return 7;
            case GE:
                return 8;
            case EQ:
                return 9;
            case NEQ:
                return 10;
            default:
                return -1;
        }
    }


    public String analyseOPG(String t) throws CompileError, TokenizeError {
        System.out.println("startOPG");
        Stack<TokenType> signStack = new Stack();
        Stack<Object> objectStack = new Stack();
        objectStack.push(t);
        int negateNum = 0;
        System.out.println("pushObjIdent");
        while (isExpr()) {
            if (isSign()) {
                if (signStack.empty()) {
                    System.out.println("pushSign");
                    signStack.push(next().getTokenType());
                    continue;
                }
                if (isSign(signStack.peek())
                        && peek().getTokenType() == TokenType.MINUS
                        && signStack.size() >= objectStack.size()) {
                    signStack.push(next().getTokenType());
                    continue;
                }
                while (!signStack.empty() &&
                        priorityMatrix
                                [transferSign(signStack.peek())][transferSign(peek().getTokenType())]
                                == 1) {

                    objectStack.pop();
                    objectStack.pop();
                    objectStack.push("1");
                    OPGOpcode(t, signStack.pop());

                    System.out.println("Specify!!");
                }
                if (!signStack.empty() &&
                        priorityMatrix[transferSign(signStack.peek())][transferSign(peek().getTokenType())] == 0
                ) {
                    if (objectStack.size() == signStack.size() + 1) {
                        System.out.println("pushSign:" + transferSign(signStack.peek()) + "|" + transferSign(peek().getTokenType()));
                        signStack.push(next().getTokenType());
                    } else {

                        throw new AnalyzeError(ErrorCode.InvalidInput, peek().getStartPos());
                    }
                } else if (check(TokenType.R_PAREN)) {
                    break;
                } else if (signStack.empty()) {
                    System.out.println("pushSign");
                    signStack.push(next().getTokenType());
                } else {
                    throw new AnalyzeError(ErrorCode.InvalidInput, peek().getStartPos());
                }
            } else {
                while (signStack.size() != objectStack.size()) {
                    signStack.pop();
                    negateNum++;
                }

                String tempString = analyseExpr(true);
                if (!tempString.equals(t)) {
                    throw new AnalyzeError(ErrorCode.UnmatchType,
                            peek().getStartPos());
                }
                objectStack.push("1");
                System.out.println("pushObjExpr");

                if (isStart) {
                    function = startFn;
                }
                while (negateNum > 0) {
                    if (t.equals("int")) {
                        this.function.body.add(new Instruction(
                                InstructionType.NoParam,
                                InstructionKind.negi
                        ));
                    } else if (t.equals("double")) {
                        this.function.body.add(new Instruction(
                                InstructionType.NoParam,
                                InstructionKind.negf
                        ));
                    }
                    negateNum--;
                }

            }
        }
        while (!signStack.isEmpty()) {
            objectStack.pop();
            objectStack.pop();
            objectStack.push("1");
            OPGOpcode(t, signStack.pop());
            System.out.println("Specify!!");
        }

        if (objectStack.size() != 1) {
            throw new ExpectedTokenError(TokenType.IDENT, peek());
        }
        System.out.println("endOPG");
        return t;
    }

    public boolean isSign() throws TokenizeError {
        if (check(TokenType.PLUS) || check(TokenType.MINUS) || check(TokenType.MUL) ||
                check(TokenType.DIV) || check(TokenType.EQ) || check(TokenType.NEQ) ||
                check(TokenType.LT) || check(TokenType.GT) || check(TokenType.LE) ||
                check(TokenType.GE) || check(TokenType.AS_KW)
        ) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isSign(TokenType tt) throws TokenizeError {
        if (tt == TokenType.PLUS || tt == TokenType.MINUS || tt == TokenType.MUL ||
                tt == TokenType.DIV || tt == TokenType.EQ || tt == TokenType.NEQ ||
                tt == TokenType.LT || tt == TokenType.GT || tt == TokenType.LE ||
                tt == TokenType.GE || tt == TokenType.AS_KW
        ) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isExpr() throws TokenizeError {
        if (check(TokenType.PLUS) || check(TokenType.MINUS) || check(TokenType.MUL) ||
                check(TokenType.DIV) || check(TokenType.EQ) || check(TokenType.NEQ) ||
                check(TokenType.LT) || check(TokenType.GT) || check(TokenType.LE) ||
                check(TokenType.GE) || check(TokenType.AS_KW) || check(TokenType.IDENT) ||
                check(TokenType.UINT_LITERAL) || check(TokenType.STRING_LITERAL) ||
                check(TokenType.DOUBLE_LITERAL) || check(TokenType.CHAR_LITERAL) ||
                check(TokenType.L_PAREN)
        ) {
            return true;
        } else {
            return false;
        }
    }

    public int analyseCallParamList() throws CompileError, TokenizeError {
        System.out.println("startCallParamList");
        int num = 1;
        analyseExpr(false, true);
        while (check(TokenType.COMMA)) {
            expect(TokenType.COMMA);
            analyseExpr(false, true);
            num++;
        }
        System.out.println("endCallParamList");
        return num;
    }

    public void analyseFnParamList() throws CompileError, TokenizeError {
        System.out.println("startFnParamList");
        analyseFnParam();
        while (check(TokenType.COMMA)) {
            expect(TokenType.COMMA);
            analyseFnParam();
        }
        System.out.println("endFnParamList");
    }

    public void analyseFnParam() throws CompileError, TokenizeError {
        System.out.println("startFnParam");
        if (check(TokenType.CONST_KW)) {
            expect(TokenType.CONST_KW);
        }
        Token tempToken = expect(TokenType.IDENT);
        expect(TokenType.COLON);
        Token tempType = expect(TokenType.IDENT);
        SymbolType type;
        if (tempType.getValueString().equals("int")) {
            type = SymbolType.INT;
        } else if (tempType.getValueString().equals("double")) {
            type = SymbolType.DOUBLE;
        } else {
            throw new AnalyzeError(ErrorCode.InvalidType, tempType.getStartPos());
        }
        this.symbolTable.pushParam(tempToken.getValueString(), type);
        this.symbolTable.symbolStack.peek().isInit = true;
        this.function.paramSlots++;
        System.out.println("endFnParam");
    }

    public StmtType analyseStmt() throws CompileError, TokenizeError {
        System.out.println("startStmt");
        StmtType type;
        if (check(TokenType.LET_KW) || check(TokenType.CONST_KW)) {
            analyseDeclareStmt();
            type = StmtType.DECLARE;
        } else if (check(TokenType.IF_KW)) {
            analyseIfStmt();
            type = StmtType.IF;
        } else if (check(TokenType.WHILE_KW)) {
            analyseWhileStmt();
            type = StmtType.WHILE;
        } else if (check(TokenType.BREAK_KW)) {
            analyseBreakStmt();
            type = StmtType.BREAK;
        } else if (check(TokenType.CONTINUE_KW)) {
            analyseContinueStmt();
            type = StmtType.CONTINUE;
        } else if (check(TokenType.RETURN_KW)) {
            analyseReturnStmt();
            type = StmtType.RETURN;
        } else if (check(TokenType.L_BRACE)) {
            if (this.symbolTable.symbolStack.size() + 1
                    == this.symbolTable.index.peek()) {
                type = analyseBlockStmt();
            } else {
                this.symbolTable.index.push(this.symbolTable.symbolStack.size() + 1);
                type = analyseBlockStmt();
                this.symbolTable.clearNow();
                this.symbolTable.index.pop();
            }
        } else if (check((TokenType.SEMICOLON))) {
            analyseEmptyStmt();
            type = StmtType.EMPTY;
        } else {
            analyseExprStmt();
            type = StmtType.EXPR;
        }
        System.out.println("endStmt");
        return type;
    }

    public void analyseExprStmt() throws CompileError, TokenizeError {
        System.out.println("startExprStmt");
        analyseExpr(false);
        expect(TokenType.SEMICOLON);
        System.out.println("endExprStmt");
    }

    public void analyseDeclareStmt() throws CompileError, TokenizeError {
        System.out.println("startDeclareStmt");
        boolean isGlobal;
        String tempString;
        if (check(TokenType.LET_KW)) {
            expect(TokenType.LET_KW);
            Token tempToken = expect(TokenType.IDENT);
            expect(TokenType.COLON);
            Token tempTy = expect(TokenType.IDENT);
            if (symbolTable.isNowExist(tempToken.getValueString())) {
                throw new AnalyzeError(ErrorCode.SymbolDuplicated, tempToken.getStartPos());
            } else {
                if (this.symbolTable.index.size() == 1) {
                    //  声明全局变量
                    program.globals.add(symbolTable.globalNum, new Global());
                    isGlobal = true;
                    if (tempTy.getValueString().equals("int")) {
                        symbolTable.pushGlobal(tempToken.getValueString(),
                                SymbolKind.VAR, SymbolType.INT);
                    } else if (tempTy.getValueString().equals("double")) {
                        symbolTable.pushGlobal(tempToken.getValueString(),
                                SymbolKind.VAR, SymbolType.DOUBLE);
                    } else {
                        throw new AnalyzeError(ErrorCode.InvalidType, tempToken.getStartPos());
                    }
                } else {
                    //  声明局部变量
                    function.locSlots++;
                    isGlobal = false;
                    if (tempTy.getValueString().equals("int")) {
                        symbolTable.pushSymbol(tempToken.getValueString(),
                                SymbolKind.VAR, SymbolType.INT);
                    } else if (tempTy.getValueString().equals("double")) {
                        symbolTable.pushSymbol(tempToken.getValueString(),
                                SymbolKind.VAR, SymbolType.DOUBLE);
                    } else {
                        throw new AnalyzeError(ErrorCode.InvalidType, tempToken.getStartPos());
                    }
                }
            }
            if (check(TokenType.ASSIGN)) {
                expect(TokenType.ASSIGN);
                this.symbolTable.symbolStack.peek().isInit = true;
                if (isGlobal) {
                    //  全局变量赋值
                    this.startFn.body.add(new Instruction(
                            InstructionType.u32Param,
                            InstructionKind.globa,
                            Integer.toString(symbolTable.globalNum - 1)));
                    //  指示当前在赋值表达式右部
                    isRExpr = true;
                    tempString = analyseExpr(false);
                    isRExpr = false;
                    this.startFn.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.store64));
                } else {
                    //  局部变量赋值
                    this.function.body.add(new Instruction(
                            InstructionType.u32Param,
                            InstructionKind.loca,
                            Integer.toString(function.locSlots - 1)));
                    //  指示当前在赋值表达式右部
                    isRExpr = true;
                    tempString = analyseExpr(false);
                    isRExpr = false;
                    this.function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.store64));
                }
                //  防止赋值左右部类型不一致
                if (!tempString.equals(tempTy.getValueString())) {
                    throw new AnalyzeError(ErrorCode.UnmatchType,
                            peek().getStartPos());
                }
            }
        } else {
            expect(TokenType.CONST_KW);
            Token tempToken = expect(TokenType.IDENT);
            expect(TokenType.COLON);
            Token tempTy = expect(TokenType.IDENT);

            if (symbolTable.isNowExist(tempToken.getValueString())) {
                throw new AnalyzeError(ErrorCode.SymbolDuplicated, tempToken.getStartPos());
            } else {
                if (this.symbolTable.index.size() == 1) {
                    //  声明全局常量
                    program.globals.add(symbolTable.globalNum, new Global());
                    isGlobal = true;
                    if (tempTy.getValueString().equals("int")) {
                        symbolTable.pushGlobal(tempToken.getValueString(),
                                SymbolKind.CONST, SymbolType.INT);
                    } else if (tempTy.getValueString().equals("double")) {
                        symbolTable.pushGlobal(tempToken.getValueString(),
                                SymbolKind.CONST, SymbolType.DOUBLE);
                    } else {
                        throw new AnalyzeError(ErrorCode.InvalidType, tempToken.getStartPos());
                    }
                } else {
                    //  声明局部常量
                    function.locSlots++;
                    isGlobal = false;
                    if (tempTy.getValueString().equals("int")) {
                        symbolTable.pushSymbol(tempToken.getValueString(),
                                SymbolKind.CONST, SymbolType.INT);
                    } else if (tempTy.getValueString().equals("double")) {
                        symbolTable.pushSymbol(tempToken.getValueString(),
                                SymbolKind.CONST, SymbolType.DOUBLE);
                    } else {
                        throw new AnalyzeError(ErrorCode.InvalidType, tempToken.getStartPos());
                    }
                }
            }
            expect(TokenType.ASSIGN);
            this.symbolTable.symbolStack.peek().isInit = true;
            if (isGlobal) {
                //  赋值全局常量
                this.startFn.body.add(new Instruction(
                        InstructionType.u32Param,
                        InstructionKind.globa,
                        Integer.toString(symbolTable.globalNum - 1)));
                //  指示当前在赋值表达式右部
                isRExpr = true;
                tempString = analyseExpr(false);
                isRExpr = false;
                this.startFn.body.add(new Instruction(
                        InstructionType.NoParam,
                        InstructionKind.store64));
            } else {
                //  赋值局部常量
                this.function.body.add(new Instruction(
                        InstructionType.u32Param,
                        InstructionKind.loca,
                        Integer.toString(function.locSlots - 1)));
                //  指示当前在赋值表达式右部
                isRExpr = true;
                tempString = analyseExpr(false);
                isRExpr = false;
                this.function.body.add(new Instruction(
                        InstructionType.NoParam,
                        InstructionKind.store64));
            }
            //  防止赋值左右部类型不一致
            if (!tempString.equals(tempTy.getValueString())) {
                throw new AnalyzeError(ErrorCode.UnmatchType,
                        peek().getStartPos());
            }
        }
        expect(TokenType.SEMICOLON);
        System.out.println("endDeclareStmt");
    }

    public boolean analyseIfStmt() throws CompileError, TokenizeError {
        System.out.println("startIfStmt");
        expect(TokenType.IF_KW);
        analyseExpr(false);


        Instruction brTrue = new Instruction(
                InstructionType.u32Param,
                InstructionKind.brtrue,
                "1");
        function.body.add(brTrue);

        int offset1 = function.body.size();
        Instruction br1 = new Instruction(
                InstructionType.u32Param,
                InstructionKind.br,
                "");
        function.body.add(br1);


        this.symbolTable.index.push(this.symbolTable.symbolStack.size() + 1);
        analyseBlockStmt();
        this.symbolTable.clearNow();
        this.symbolTable.index.pop();

        int offset2 = function.body.size();
        br1.param = Integer.toString(offset2 - offset1);

        if (check(TokenType.ELSE_KW)) {
            expect(TokenType.ELSE_KW);

            Instruction br2 = new Instruction(
                    InstructionType.u32Param,
                    InstructionKind.br,
                    "");
            function.body.add(br2);

            if (check(TokenType.IF_KW)) {
                analyseIfStmt();
            } else {
                this.symbolTable.index.push(this.symbolTable.symbolStack.size() + 1);
                analyseBlockStmt();
                this.symbolTable.clearNow();
                this.symbolTable.index.pop();
            }

            int offset3 = function.body.size();
            br2.param = Integer.toString(offset3 - offset2);
        }

        Instruction br3 = new Instruction(
                InstructionType.u32Param,
                InstructionKind.br,
                "0");
        function.body.add(br3);

        // Incomplete
        System.out.println("endIfStmt");
        return true;
    }

    public void analyseWhileStmt() throws CompileError, TokenizeError {
        System.out.println("startWhileStmt");
        expect(TokenType.WHILE_KW);

        int offset1 = function.body.size();
        Instruction br1 = new Instruction(
                InstructionType.u32Param,
                InstructionKind.br,
                "0");
        function.body.add(br1);

        analyseExpr(false);


        Instruction brTrue = new Instruction(
                InstructionType.u32Param,
                InstructionKind.brtrue,
                "1");
        function.body.add(brTrue);
        int offset2 = function.body.size();
        Instruction br2 = new Instruction(
                InstructionType.u32Param,
                InstructionKind.br,
                "");
        function.body.add(br2);

        this.symbolTable.index.push(this.symbolTable.symbolStack.size() + 1);
        analyseBlockStmt();

        int offset3 = function.body.size();
        Instruction br3 = new Instruction(
                InstructionType.u32Param,
                InstructionKind.br,
                Integer.toString(offset1 - offset3));
        function.body.add(br3);
        br2.param = Integer.toString(offset3 - offset2);

        this.symbolTable.clearNow();
        this.symbolTable.index.pop();
        System.out.println("endWhileStmt");
    }

    public void analyseBreakStmt() throws CompileError, TokenizeError {
        System.out.println("startBreakStmt");
        expect(TokenType.BREAK_KW);
        expect(TokenType.SEMICOLON);
        System.out.println("endBreakStmt");
    }

    public void analyseContinueStmt() throws CompileError, TokenizeError {
        System.out.println("startContinueStmt");
        expect(TokenType.CONTINUE_KW);
        expect(TokenType.SEMICOLON);
        System.out.println("endContinueStmt");
    }

    public void analyseReturnStmt() throws CompileError, TokenizeError {
        System.out.println("startReturnStmt");
        expect(TokenType.RETURN_KW);
        if (returnType != 0) {
            this.function.body.add(new Instruction(
                    InstructionType.u32Param,
                    InstructionKind.arga,
                    "0"
            ));
        } else {
            this.function.body.add(new Instruction(
                    InstructionType.NoParam,
                    InstructionKind.ret
            ));
        }
        if (!check(TokenType.SEMICOLON)) {
            String tempExpr = analyseExpr(false);
            if (tempExpr.equals("int")) {
                if (returnType != 1) {
                    throw new AnalyzeError(ErrorCode.UnmatchType,
                            peek().getStartPos());
                }
            } else if (tempExpr.equals("double")) {
                if (returnType != 2) {
                    throw new AnalyzeError(ErrorCode.UnmatchType,
                            peek().getStartPos());
                }
            } else {
                throw new AnalyzeError(ErrorCode.UnmatchType,
                        peek().getStartPos());
            }
            this.function.body.add(new Instruction(
                    InstructionType.NoParam,
                    InstructionKind.store64
            ));
            this.function.body.add(new Instruction(
                    InstructionType.NoParam,
                    InstructionKind.ret
            ));
        } else {
            if (returnType != 0) {
                throw new AnalyzeError(ErrorCode.UnmatchType,
                        peek().getStartPos());
            }
        }
        expect(TokenType.SEMICOLON);
        System.out.println("endReturnStmt");
    }

    public StmtType analyseBlockStmt() throws CompileError, TokenizeError {
        System.out.println("startBlockStmt");
        expect(TokenType.L_BRACE);

        StmtType type;
        StmtType returnType = StmtType.EMPTY;
        while (!check(TokenType.R_BRACE)) {
            type = analyseStmt();
            if (type == StmtType.RETURN) {
                returnType = type;
            }
        }

        expect(TokenType.R_BRACE);
        System.out.println("endBlockStmt");
        return returnType;
    }

    public void analyseEmptyStmt() throws CompileError, TokenizeError {
        System.out.println("startEmptyStmt");
        expect(TokenType.SEMICOLON);
        System.out.println("endEmptyStmt");
    }

    public void OPGOpcode(String t, TokenType tt) throws TokenizeError, AnalyzeError {
        if (isRExpr) {
            if (tt == TokenType.PLUS || tt == TokenType.MINUS ||
                    tt == TokenType.MUL || tt == TokenType.DIV) {

            } else {
                throw new AnalyzeError(ErrorCode.UnmatchType,
                        peek().getStartPos());
            }
        }
        if (isStart) {
            function = startFn;
        }
        if (t.equals("int")) {
            switch (tt) {
                case PLUS:
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.addi));
                    break;
                case MINUS:
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.subi));
                    break;
                case MUL:
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.muli));
                    break;
                case DIV:
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.divi));
                    break;
                case EQ:
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.cmpi));
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.not));
                    break;
                case NEQ:
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.cmpi));
                    break;
                case LT:
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.cmpi));
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.setlt));
                    break;
                case GT:
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.cmpi));
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.setgt));
                    break;
                case LE:
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.cmpi));
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.setgt));
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.not));
                    break;
                case GE:
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.cmpi));
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.setlt));
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.not));
            }
        } else if (t.equals("double")) {
            switch (tt) {
                case PLUS:
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.addf));
                    break;
                case MINUS:
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.subf));
                    break;
                case MUL:
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.mulf));
                    break;
                case DIV:
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.divf));
                    break;
                case EQ:
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.cmpf));
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.not));
                    break;
                case NEQ:
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.cmpf));
                    break;
                case LT:
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.cmpf));
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.setlt));
                    break;
                case GT:
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.cmpf));
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.setgt));
                    break;
                case LE:
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.cmpf));
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.setgt));
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.not));
                    break;
                case GE:
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.cmpf));
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.setlt));
                    function.body.add(new Instruction(
                            InstructionType.NoParam,
                            InstructionKind.not));
            }
        } else {
            System.out.println(t);
            throw new AnalyzeError((ErrorCode.InvalidCalculate),
                    peek().getStartPos());
        }
    }


    /**
     * 查看下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            peekedToken = tokenizer.nextToken();
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            return token;
        } else {
            return tokenizer.nextToken();
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     *
     * @param tt
     * @return
     * @throws TokenizeError
     */
    private boolean check(TokenType tt) throws TokenizeError {
        var token = peek();
        return token.getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     *
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    private Token nextIf(TokenType tt) throws TokenizeError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     *
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError, TokenizeError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }

}
