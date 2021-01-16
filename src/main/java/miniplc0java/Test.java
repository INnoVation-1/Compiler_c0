package miniplc0java;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import miniplc0java.analyser.Analyser;
import miniplc0java.error.CompileError;
import miniplc0java.error.TokenizeError;
import miniplc0java.tokenizer.StringIter;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;

import net.sourceforge.argparse4j.*;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Test {
    public static void main(String[] args) {
        try{
//            InputStream input = null;
//            String outFile = null;
//            for(int i = 0; i < args.length; i++){
//                if(args[i].equals("-l")){
//                    input = new FileInputStream(args[i + 1]);
//                }else if(args[i].equals("-o")){
//                    outFile = args[i + 1];
//                }
//            }

            String ssss = System.getProperty("user.dir");
            System.out.println(System.getProperty("user.dir"));
            InputStream input = new FileInputStream("files/input.c0");
            String outFile = "files/output.c0";

            Scanner scanner;
            scanner = new Scanner(input);
            var iter = new StringIter(scanner);
            var tokenizer = tokenize(iter);

//        var tokens = new ArrayList<Token>();
//        try {
//            while (true) {
//                var token = tokenizer.nextToken();
//                if (token.getTokenType().equals(TokenType.EOF)) {
//                    break;
//                }
//                tokens.add(token);
//            }
//        } catch (Exception e) {
//            // 遇到错误不输出，直接退出
//            System.err.println(e);
//            System.exit(0);
//            return;
//        }
//        for (Token token : tokens) {
//            System.out.println(token.toString());
//        }
            var analyzer = new Analyser(tokenizer);
            analyzer.analyse();
            analyzer.program.export(outFile);
        }catch(Exception | TokenizeError e){
            e.printStackTrace();
        }
    }



    private static Tokenizer tokenize(StringIter iter) {
        var tokenizer = new Tokenizer(iter);
        return tokenizer;
    }
}
