package c0java.util;

import java.util.Stack;

public class SymbolTable {
    public Stack<Symbol> symbolStack = new Stack();
    public Stack<Integer> index = new Stack();
    public int fnNum = 0;
    public int trueFnNum = 0;
    public int globalNum = 0;
    public int num = 0;

    public void pushSymbol(String name, SymbolKind kind, SymbolType type){
        symbolStack.push(new Symbol(name, kind, type, num++));
    }

    public void pushParam(String name, SymbolType type){
        if(symbolStack.size() != 0 && symbolStack.peek().kind == SymbolKind.PARAM){
            symbolStack.push(new Symbol(name, SymbolKind.PARAM, type, symbolStack.peek().pos + 1));
        }else{
            symbolStack.push(new Symbol(name, SymbolKind.PARAM, type, 0));
        }
        num++;
    }

    public void pushFn(String name){
        if(index.size() == 1){
            symbolStack.push(new Symbol(name, SymbolKind.FN, SymbolType.NONE, fnNum++));
        }else{
            symbolStack.insertElementAt(new Symbol(name, SymbolKind.FN, SymbolType.NONE, fnNum), globalNum + fnNum);
            fnNum++;
            Stack<Integer> temp = new Stack();
            for(;index.size() != 1;){
                temp.push(index.pop());
            }
            for(;temp.size() != 0;){
                index.push(temp.pop() + 1);
            }
        }
    }

    public void pushTrueFn(String name){
        symbolStack.push(new Symbol(name, SymbolKind.FN, SymbolType.NONE, trueFnNum));
        fnNum++;
        trueFnNum++;
    }

    public void pushGlobal(String name, SymbolKind kind, SymbolType type){
        symbolStack.insertElementAt(new Symbol(name, kind, type, globalNum), globalNum);
        globalNum++;
        Stack<Integer> temp = new Stack();
        for(;index.size() != 1;){
            temp.push(index.pop());
        }
        for(;temp.size() != 0;){
            index.push(temp.pop() + 1);
        }
    }

    public boolean isNowExist(String name){
        System.out.println(index.peek());
        for(int j = 0; j < symbolStack.size(); j++){
            System.out.println(symbolStack.get(j).name);
        }
        for(int i = symbolStack.size() - 1; i >= index.peek(); i--){
            if(name.equals(symbolStack.get(i).name)){
                return true;
            }
        }
        return false;
    }

    public void clearNow(){
        while(symbolStack.size() > index.peek()){
            symbolStack.pop();
        }
    }

    public boolean isExist(String name){
        for(int i = symbolStack.size() - 1; i >= 0; i--){
            if(name.equals(symbolStack.get(i).name)){
                return true;
            }
        }
        return false;
    }

    public Symbol getNowExist(String name){
        for(int i = symbolStack.size() - 1; i >= index.peek(); i--){
            if(name.equals(symbolStack.get(i).name)){
                return symbolStack.get(i);
            }
        }
        return null;
    }

    public Symbol getExist(String name){
        for(int i = symbolStack.size() - 1; i >= 0; i--){
            if(name.equals(symbolStack.get(i).name)){
                return symbolStack.get(i);
            }
        }
        return null;
    }

    public Symbol getGlobal(String name){
        for(int i = 0; i < fnNum + globalNum; i++){
            if(name.equals(symbolStack.get(i).name)){
                return symbolStack.get(i);
            }
        }
        return null;
    }

    public Symbol getFn(String fnName){
        for(Symbol s : symbolStack){
            System.out.println(s.name);
            if(fnName.equals(s.name)
                    && s.kind == SymbolKind.FN){
                return s;
            }
        }
        return null;
    }

}
